package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
import de.hysky.skyblocker.skyblock.bazaar.BazaarOrderTracker;
import de.hysky.skyblocker.skyblock.bazaar.ReorderHelper;
import de.hysky.skyblocker.skyblock.chocolatefactory.ChocolateFactorySolver;
import de.hysky.skyblocker.skyblock.dungeon.CroesusProfit;
import de.hysky.skyblocker.skyblock.dwarven.fossil.FossilSolver;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.AccessoryTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.AvgBinTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.BazaarPriceTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.BitsHelper;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.ColorTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.CraftPriceTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.DateCalculatorTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.DungeonQualityTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.EssenceShopPrice;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.EstimatedItemValueTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.EvolvingItemProgressTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.HuntingBoxPriceTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.LBinTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.LineSmoothener;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.MotesTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.MuseumTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.NpcPriceTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.ObtainedDateTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.StackingEnchantProgressTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.SupercraftReminder;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.TrueHexDisplay;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.TrueHexDyeScreenDisplay;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.ContainerMatcher;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.jspecify.annotations.Nullable;

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
			BazaarOrderTracker.INSTANCE,
			new StackingEnchantProgressTooltip(0), //Would be best to have after the lore but the tech doesn't exist for that
			new EvolvingItemProgressTooltip(0),
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
			new HuntingBoxPriceTooltip(15),
			CroesusProfit.INSTANCE, // priority = 16
	};
	private static List<TooltipAdder> currentScreenAdders = new ArrayList<>();

	private TooltipManager() {
	}

	@Init
	public static void init() {
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
			if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> handledScreen) {
				addToTooltip(((AbstractContainerScreenAccessor) handledScreen).getFocusedSlot(), stack, lines);
			} else {
				addToTooltip(null, stack, lines);
			}
		});
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			onScreenChange(screen);
			ScreenEvents.remove(screen).register(ignored -> currentScreenAdders = List.of());
		});
	}

	private static void onScreenChange(Screen screen) {
		currentScreenAdders = Arrays.stream(adders)
				.filter(ContainerMatcher::isEnabled)
				.filter(adder -> adder.test(screen))
				.sorted(Comparator.comparingInt(TooltipAdder::getPriority))
				.toList();
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
	 */
	private static List<Component> addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		if (!Utils.isOnSkyblock()) return lines;
		for (TooltipAdder adder : currentScreenAdders) {
			adder.addToTooltip(focusedSlot, stack, lines);
		}
		return lines;
	}
}
