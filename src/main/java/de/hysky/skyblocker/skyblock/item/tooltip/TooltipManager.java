package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.bazaar.ReorderHelper;
import de.hysky.skyblocker.skyblock.chocolatefactory.ChocolateFactorySolver;
import de.hysky.skyblocker.skyblock.dwarven.fossil.FossilSolver;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.*;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class TooltipManager {
	private static final TooltipAdder[] adders = new TooltipAdder[]{
			new LineSmoothener(), // Applies before anything else
			new TrueHexDisplay(),
			new TrueHexDyeScreenDisplay(),
			new SupercraftReminder(),
			ChocolateFactorySolver.INSTANCE,
			BitsHelper.INSTANCE,
			new FossilSolver(),
			new ReorderHelper(),
			new StackingEnchantProgressTooltip(0), //Would be best to have after the lore but the tech doesn't exist for that
			new NpcPriceTooltip(1),
			new BazaarPriceTooltip(2),
			new LBinTooltip(3),
			new AvgBinTooltip(4),
			new EssenceShopPrice(5),
			new CraftPriceTooltip(6),
			new EstimatedItemValueTooltip(7),
			new DungeonQualityTooltip(8),
			new MotesTooltip(9),
			new ObtainedDateTooltip(10),
			new MuseumTooltip(11),
			new ColorTooltip(12),
			new AccessoryTooltip(13),
			new DateCalculatorTooltip(14),
			new HuntingBoxPriceTooltip(15)
	};
	private static final ArrayList<TooltipAdder> currentScreenAdders = new ArrayList<>();
	private static boolean disabledForCurrentScreen = false;

	private TooltipManager() {
	}

	@Init
	public static void init() {
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
			if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> handledScreen) {
				addToTooltip(((HandledScreenAccessor) handledScreen).getFocusedSlot(), stack, lines);
			} else {
				addToTooltip(null, stack, lines);
			}
		});
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			onScreenChange(screen);
			if (screen instanceof GenericContainerScreen containerScreen &&
					SkyblockerConfigManager.get().uiAndVisuals.showTooltipToggleButton) {
				addToggleButton(containerScreen);
			}
			ScreenEvents.remove(screen).register(ignored -> currentScreenAdders.clear());
		});
	}

	private static void onScreenChange(Screen screen) {
		currentScreenAdders.clear();
		disabledForCurrentScreen = false;
		if (screen instanceof GenericContainerScreen containerScreen &&
				SkyblockerConfigManager.get().uiAndVisuals.tooltipBlacklist.contains(screen.getTitle().getString())) {
			disabledForCurrentScreen = true;
			return;
		}
		for (TooltipAdder adder : adders) {
			if (adder.isEnabled() && adder.test(screen)) {
				currentScreenAdders.add(adder);
			}
		}
		currentScreenAdders.sort(Comparator.comparingInt(TooltipAdder::getPriority));
	}

	private static void addToggleButton(GenericContainerScreen screen) {
		String title = screen.getTitle().getString();
		boolean disabled = SkyblockerConfigManager.get().uiAndVisuals.tooltipBlacklist.contains(title);

		Text message = Text.translatable("skyblocker.ui.tooltipToggle.button")
				.setStyle(Style.EMPTY.withColor(disabled ? Formatting.RED : Formatting.GREEN));

		int width = MinecraftClient.getInstance().textRenderer.getWidth(message) + 6;
		int height = 20;
		int x = ((HandledScreenAccessor) screen).getX() - width - 6;
		int y = ((HandledScreenAccessor) screen).getY();

		ButtonWidget button = ButtonWidget.builder(message, b -> {
					SkyblockerConfigManager.update(cfg -> {
						Set<String> set = cfg.uiAndVisuals.tooltipBlacklist;
						if (!set.add(title)) {
							set.remove(title);
						}
					});
					onScreenChange(screen);
					boolean nowDisabled = SkyblockerConfigManager.get().uiAndVisuals.tooltipBlacklist.contains(title);
					Text newMessage = Text.translatable("skyblocker.ui.tooltipToggle.button")
							.setStyle(Style.EMPTY.withColor(nowDisabled ? Formatting.RED : Formatting.GREEN));
					b.setMessage(newMessage);
					int newWidth = MinecraftClient.getInstance().textRenderer.getWidth(newMessage) + 6;
					b.setWidth(newWidth);
					b.setX(((HandledScreenAccessor) screen).getX() - newWidth - 6);
					b.setTooltip(Tooltip.of(Text.translatable(nowDisabled ? "skyblocker.ui.tooltipToggle.disabled" : "skyblocker.ui.tooltipToggle.enabled")));
				})
				.dimensions(x, y, width, height)
				.tooltip(Tooltip.of(Text.translatable(disabled ? "skyblocker.ui.tooltipToggle.disabled" : "skyblocker.ui.tooltipToggle.enabled")))
				.build();

		Screens.getButtons(screen).add(button);
	}

	/**
	 * <p>Adds additional text from all adders that are applicable to the current screen.
	 * This method is run on each tooltip render, so don't do any heavy calculations here.</p>
	 *
	 * <p>If you want to add info to the tooltips of multiple items, consider using a switch statement with {@code focusedSlot.getIndex()}</p>
	 *
	 * @param focusedSlot The slot that is currently focused by the cursor.
	 * @param stack       The stack to render the tooltip for.
	 * @param lines       The tooltip lines of the focused item. This includes the display name, as it's a part of the tooltip (at index 0).
	 * @return The lines list itself after all adders have added their text.
	 * @deprecated This method is public only for the sake of the mixin. Don't call directly, not that there is any point to it.
	 */
	@Deprecated
	public static List<Text> addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		if (!Utils.isOnSkyblock() || disabledForCurrentScreen) return lines;
		for (TooltipAdder adder : currentScreenAdders) {
			adder.addToTooltip(focusedSlot, stack, lines);
		}
		return lines;
	}
}
