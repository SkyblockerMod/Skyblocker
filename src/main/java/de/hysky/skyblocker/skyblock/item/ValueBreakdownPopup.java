package de.hysky.skyblocker.skyblock.item;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.TextTransformer;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import io.github.moulberry.repo.data.NEUItem;
import net.azureaaron.networth.Calculation;
import net.azureaaron.networth.NetworthResult;
import net.azureaaron.networth.utils.ItemConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ValueBreakdownPopup extends AbstractPopupScreen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final KeyMapping KEY_BINDING = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.skyblocker.valueBreadownPopup",
			GLFW.GLFW_KEY_I,
			SkyblockerMod.KEYBINDING_CATEGORY
	));

	private static final Function<String, Component> EMPTY = s -> Component.empty();
	private static final Function<String, Component> ITEM_NAME = s -> {
		NEUItem neuItem = NEURepoManager.getItemByNeuId(s);
		if (neuItem == null) return Component.literal(s);
		return TextTransformer.fromLegacy(neuItem.getDisplayName());
	};
	private static final LayoutAppender EMPTY_APPENDER = (r, c, l) -> {};

	private static final Map<Calculation.Type, LayoutAppender> FORMATTERS = new EnumMap<>(Map.ofEntries(
			Map.entry(Calculation.Type.STAR, new BasicListAppender(
					Component.literal("Star Upgrades"),
					ITEM_NAME
			)),
			// TODO Prestige (idk how it works)
			Map.entry(Calculation.Type.GOD_ROLL, new BasicSingleAppender(
					Component.literal("God Roll"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.SHEN_AUCTION, EMPTY_APPENDER),
			Map.entry(Calculation.Type.WINNING_BID, EMPTY_APPENDER),
			Map.entry(Calculation.Type.ENCHANTMENT, new BasicListAppender(
					Component.literal("Enchantments"),
					s -> {
						String neuId = ItemRepository.getBazaarStocks().get("ENCHANTMENT_" + s);
						if (neuId == null) return Component.literal(s);
						NEUItem neuItem = NEURepoManager.getItemByNeuId(neuId);
						if (neuItem == null) return Component.literal(s);
						return TextTransformer.fromLegacy(neuItem.getLore().getFirst());
					}
			)),
			Map.entry(Calculation.Type.SKIN, new BasicSingleAppender(
					Component.literal("Skin"),
					s -> {
						NEUItem neuItem = NEURepoManager.getItemByNeuId(s);
						if (neuItem == null) neuItem = NEURepoManager.getItemByNeuId("PET_SKIN_" + s);
						if (neuItem == null) return Component.literal(s);
						return TextTransformer.fromLegacy(neuItem.getDisplayName());
					}
			)),
			Map.entry(Calculation.Type.SILEX, new BasicSingleAppender(
					Component.literal("Silex"),
					EMPTY
			)),
			Map.entry(Calculation.Type.GOLDEN_BOUNTY, new BasicSingleAppender(
					Component.literal("Golden Bounty"),
					EMPTY
			)),
			// TODO Attributes
			Map.entry(Calculation.Type.POCKET_SACK_IN_A_SACK, new BasicSingleAppender(
					Component.literal("Pocket Sack in a sack"),
					EMPTY
			)),
			Map.entry(Calculation.Type.WOOD_SINGULARITY, new BasicSingleAppender(
					Component.literal("Wood Singularity"),
					EMPTY
			)),
			Map.entry(Calculation.Type.JALAPENO_BOOK, new BasicSingleAppender(
					Component.literal("Jalapeno Book"),
					EMPTY
			)),
			Map.entry(Calculation.Type.TRANSMISSION_TUNER, new BasicSingleAppender(
					Component.literal("Transmission Tuner"),
					EMPTY
			)),
			Map.entry(Calculation.Type.MANA_DISINTEGRATOR, new BasicSingleAppender(
					Component.literal("Mana Disintegrator"),
					EMPTY
			)),
			Map.entry(Calculation.Type.THUNDER_IN_A_BOTTLE, new BasicSingleAppender(
					Component.literal("Thunder in a Bottle"),
					EMPTY
			)),
			Map.entry(Calculation.Type.RUNE, new BasicSingleAppender(
					Component.literal("Rune"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.FUMING_POTATO_BOOK, new BasicSingleAppender(
					Component.literal("Fuming Potato Book"),
					EMPTY
			)),
			Map.entry(Calculation.Type.HOT_POTATO_BOOK, new BasicSingleAppender(
					Component.literal("Hot Potato Book"),
					EMPTY
			)),
			Map.entry(Calculation.Type.DYE, new BasicSingleAppender(
					Component.literal("Dye"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.ART_OF_WAR, new BasicSingleAppender(
					Component.literal("Art of War"),
					EMPTY
			)),
			Map.entry(Calculation.Type.ART_OF_PEACE, new BasicSingleAppender(
					Component.literal("Art of Peace"),
					EMPTY
			)),
			Map.entry(Calculation.Type.FARMING_FOR_DUMMIES, new BasicSingleAppender(
					Component.literal("Farming For Dummies"),
					EMPTY
			)),
			Map.entry(Calculation.Type.TALISMAN_ENRICHMENT, new BasicSingleAppender(
					Component.literal("Talisman Enrichment"),
					EMPTY
			)),
			Map.entry(Calculation.Type.RECOMBOBULATOR, new BasicSingleAppender(
					Component.literal("Recombobulator"),
					EMPTY
			)),
			Map.entry(Calculation.Type.GEMSTONE_SLOT, new BasicSingleAppender(
					Component.literal("Gemstone Slot"),
					EMPTY
			)),
			Map.entry(Calculation.Type.GEMSTONE, new BasicListAppender(
					Component.literal("Gemstones"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.POWER_SCROLL, new BasicSingleAppender(
					Component.literal("Power Scroll"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.REFORGE, new BasicSingleAppender(
					Component.literal("Reforge"),
					s -> {
						//noinspection UnstableApiUsage
						String neuId = ItemConstants.REFORGES.get(s);
						if (neuId == null) return Component.literal(s);
						return ITEM_NAME.apply(neuId);
					}
			)),
			Map.entry(Calculation.Type.MASTER_STAR, new BasicListAppender(
					Component.literal("Master Stars"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.WITHER_BLADE_SCROLL, new BasicListAppender(
					Component.literal("Wither Blade Scrolls"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.DRILL_PART, new BasicListAppender(
					Component.literal("Drill Parts"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.POLARVOID_BOOK, new BasicSingleAppender(
					Component.literal("Polarvoid Book"),
					EMPTY
			)),
			Map.entry(Calculation.Type.DIVAN_POWDER_COATING, new BasicSingleAppender(
					Component.literal("Divan Powder Coating"),
					EMPTY
			)),
			Map.entry(Calculation.Type.ETHERWARP_CONDUIT, new BasicSingleAppender(
					Component.literal("Etherwarp Conduit"),
					EMPTY
			)),
			Map.entry(Calculation.Type.NEW_YEAR_CAKES, new BasicSingleAppender(
					Component.literal("New Year Cakes"),
					EMPTY
			)),
			Map.entry(Calculation.Type.PET_ITEM, new BasicSingleAppender(
					Component.literal("Pet Item"),
					ITEM_NAME
			))
	));

	private final NetworthResult networthResult;
	private final EnumMap<Calculation.Type, List<Calculation>> map;
	private ScrollableLayout scrollable;

	@Init
	public static void initClass() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!Utils.isOnSkyblock()) return;
			if (screen instanceof AbstractContainerScreen<?> handledScreen) {
				ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key) -> {
					if (!KEY_BINDING.matches(key)) return;
					Slot slot = ((AbstractContainerScreenAccessor) handledScreen).getFocusedSlot();
					if (slot == null || !slot.hasItem()) return;
					NetworthResult networth = NetworthCalculator.getItemNetworth(slot.getItem());
					if (networth.price() > 0) client.setScreen(new ValueBreakdownPopup(screen, networth));
				});
			}
		});
	}

	protected ValueBreakdownPopup(Screen backgroundScreen, NetworthResult networthResult) {
		super(Component.translatable("skyblocker.valueBreakdownPopup"), backgroundScreen);
		this.networthResult = networthResult;

		this.map = new EnumMap<>(Calculation.Type.class);
		for (Calculation calculation : networthResult.calculations()) {
			map.computeIfAbsent(calculation.type(), ignored -> new ArrayList<>()).add(calculation);
		}
	}

	@Override
	protected void init() {
		LinearLayout layout = LinearLayout.vertical();
		layout.addChild(createTextWidget(Component.translatable("skyblocker.valueBreakdownPopup.baseItemPrice", getCoinsText(networthResult.base(), networthResult.price())), font));
		for (Map.Entry<Calculation.Type, List<Calculation>> entry : map.entrySet()) {
			LayoutAppender appender = FORMATTERS.get(entry.getKey());
			if (appender == EMPTY_APPENDER) continue;
			layout.addChild(SpacerElement.height(5));
			if (appender != null) {
				appender.appendTo(networthResult, entry.getValue(), layout);
				continue;
			}
			layout.addChild(createTextWidget(Component.literal(entry.getKey().toString()), font));
			for (Calculation calculation : entry.getValue()) {
				layout.addChild(createTextWidget(Component.literal(calculation.id() + ": ").append(getCoinsText(calculation.price())), font), p -> p.paddingLeft(20));
			}
		}
		layout.addChild(SpacerElement.height(10));
		layout.addChild(createTextWidget(Component.translatable("skyblocker.valueBreakdownPopup.total", getCoinsText(networthResult.price())), font), LayoutSettings::alignHorizontallyRight);
		scrollable = new ScrollableLayout(minecraft, layout, 300);
		scrollable.setMaxHeight(200);
		scrollable.visitWidgets(this::addRenderableWidget);
		super.init();
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		scrollable.arrangeElements();
		scrollable.setPosition((width - scrollable.getWidth()) / 2, (height - scrollable.getHeight()) / 2);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredString(font, title, width / 2, 15, -1);
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, scrollable.getX(), scrollable.getY(), scrollable.getWidth(), scrollable.getHeight());
	}

	private static Component getCoinsText(double price) {
		return getCoinsText(price, 0);
	}

	private static Component getCoinsText(double price, double totalPrice) {
		MutableComponent text = Component.translatable("skyblocker.valueBreakdownPopup.coins", Formatters.FLOAT_NUMBERS.format(price)).withStyle(ChatFormatting.GOLD);
		if (totalPrice > 0) {
			text.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Component.translatable("skyblocker.valueBreakdownPopup.totalPricePercent", Formatters.FLOAT_NUMBERS.format(price / totalPrice * 100)))));
		}
		return text;
	}

	private static MultiLineTextWidget createTextWidget(Component text, Font textRenderer) {
		MultiLineTextWidget widget = new MultiLineTextWidget(text, textRenderer);
		widget.setComponentClickHandler(s -> {});

		return widget;
	}

	public interface LayoutAppender {
		void appendTo(NetworthResult networthResult, List<Calculation> calculations, LinearLayout layout);

		default void appendCountAndPrice(Calculation calc, double totalPrice, MutableComponent empty) {
			if (calc.count() > 1)
				empty.append(Component.literal(" x").withStyle(ChatFormatting.GRAY)).append(Component.literal(String.valueOf(calc.count())).withStyle(ChatFormatting.YELLOW));
			empty.append(Component.literal(" (").withStyle(ChatFormatting.GRAY));
			empty.append(getCoinsText(calc.price(), totalPrice));
			empty.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
		}

	}

	private record BasicSingleAppender(Component displayName, Function<String, Component> idFormatter, boolean hideIfWorthNothing) implements LayoutAppender {
		private BasicSingleAppender(Component displayName, Function<String, Component> idFormatter) {
			this(displayName, idFormatter, true);
		}

		@Override
		public void appendTo(NetworthResult networthResult, List<Calculation> calculations, LinearLayout layout) {
			if (calculations.size() > 1) {
				LOGGER.warn("More than one calculation was found for type {}", calculations.getFirst().type());
			}
			Calculation calc = calculations.getFirst();
			if (calc.price() <= 0 && hideIfWorthNothing) return;
			MutableComponent empty = Component.empty();
			empty.append(displayName());
			Component apply = idFormatter.apply(calc.id());
			if (!apply.getString().isBlank()) empty.append(": ").append(apply);
			appendCountAndPrice(calc, networthResult.price(), empty);
			layout.addChild(createTextWidget(empty, Minecraft.getInstance().font));
		}

	}

	private record BasicListAppender(Component displayName, Function<String, Component> idFormatter) implements LayoutAppender {
		@Override
		public void appendTo(NetworthResult networthResult, List<Calculation> calculations, LinearLayout layout) {
			Font textRenderer = Minecraft.getInstance().font;
			layout.addChild(createTextWidget(displayName, textRenderer));
			double total = 0;
			for (Calculation calc : calculations) {
				MutableComponent empty = Component.empty();
				Component apply = idFormatter.apply(calc.id());
				empty.append(apply);
				appendCountAndPrice(calc, networthResult.price(), empty);
				total += calc.price();
				layout.addChild(createTextWidget(empty, textRenderer), p -> p.paddingLeft(15));
			}
			layout.addChild(createTextWidget(Component.translatable("skyblocker.valueBreakdownPopup.total", getCoinsText(total, networthResult.price())), textRenderer), p -> p.paddingLeft(10));
		}
	}
}
