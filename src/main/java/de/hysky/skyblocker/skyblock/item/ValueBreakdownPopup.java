package de.hysky.skyblocker.skyblock.item;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.TextTransformer;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import io.github.moulberry.repo.data.NEUItem;
import net.azureaaron.networth.Calculation;
import net.azureaaron.networth.NetworthResult;
import net.azureaaron.networth.utils.ItemConstants;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ValueBreakdownPopup extends AbstractPopupScreen {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Function<String, Text> EMPTY = s -> Text.empty();
	private static final Function<String, Text> ITEM_NAME = s -> {
		NEUItem neuItem = NEURepoManager.getItemByNeuId(s);
		if (neuItem == null) return Text.literal(s);
		return TextTransformer.fromLegacy(neuItem.getDisplayName());
	};
	private static final LayoutAppender EMPTY_APPENDER = (r, c, l) -> {};

	private static final Map<Calculation.Type, LayoutAppender> FORMATTERS = new EnumMap<>(Map.ofEntries(
			Map.entry(Calculation.Type.STAR, new BasicListAppender(
					Text.literal("Star Upgrades"),
					ITEM_NAME
			)),
			// TODO Prestige (idk how it works)
			Map.entry(Calculation.Type.GOD_ROLL, new BasicSingleAppender(
					Text.literal("God Roll"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.SHEN_AUCTION, EMPTY_APPENDER),
			Map.entry(Calculation.Type.WINNING_BID, EMPTY_APPENDER),
			Map.entry(Calculation.Type.ENCHANTMENT, new BasicListAppender(
					Text.literal("Enchantments"),
					s -> {
						String neuId = ItemRepository.getBazaarStocks().get("ENCHANTMENT_" + s);
						if (neuId == null) return Text.literal(s);
						NEUItem neuItem = NEURepoManager.getItemByNeuId(neuId);
						if (neuItem == null) return Text.literal(s);
						return TextTransformer.fromLegacy(neuItem.getLore().getFirst());
					}
			)),
			Map.entry(Calculation.Type.SKIN, new BasicSingleAppender(
					Text.literal("Skin"),
					s -> {
						NEUItem neuItem = NEURepoManager.getItemByNeuId(s);
						if (neuItem == null) neuItem = NEURepoManager.getItemByNeuId("PET_SKIN_" + s);
						if (neuItem == null) return Text.literal(s);
						return TextTransformer.fromLegacy(neuItem.getDisplayName());
					}
			)),
			Map.entry(Calculation.Type.SILEX, new BasicSingleAppender(
					Text.literal("Silex"),
					EMPTY
			)),
			Map.entry(Calculation.Type.GOLDEN_BOUNTY, new BasicSingleAppender(
					Text.literal("Golden Bounty"),
					EMPTY
			)),
			// TODO Attributes
			Map.entry(Calculation.Type.POCKET_SACK_IN_A_SACK, new BasicSingleAppender(
					Text.literal("Pocket Sack in a sack"),
					EMPTY
			)),
			Map.entry(Calculation.Type.WOOD_SINGULARITY, new BasicSingleAppender(
					Text.literal("Wood Singularity"),
					EMPTY
			)),
			Map.entry(Calculation.Type.JALAPENO_BOOK, new BasicSingleAppender(
					Text.literal("Jalapeno Book"),
					EMPTY
			)),
			Map.entry(Calculation.Type.TRANSMISSION_TUNER, new BasicSingleAppender(
					Text.literal("Transmission Tuner"),
					EMPTY
			)),
			Map.entry(Calculation.Type.MANA_DISINTEGRATOR, new BasicSingleAppender(
					Text.literal("Mana Disintegrator"),
					EMPTY
			)),
			Map.entry(Calculation.Type.THUNDER_IN_A_BOTTLE, new BasicSingleAppender(
					Text.literal("Thunder in a Bottle"),
					EMPTY
			)),
			Map.entry(Calculation.Type.RUNE, new BasicSingleAppender(
					Text.literal("Rune"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.FUMING_POTATO_BOOK, new BasicSingleAppender(
					Text.literal("Fuming Potato Book"),
					EMPTY
			)),
			Map.entry(Calculation.Type.HOT_POTATO_BOOK, new BasicSingleAppender(
					Text.literal("Hot Potato Book"),
					EMPTY
			)),
			Map.entry(Calculation.Type.DYE, new BasicSingleAppender(
					Text.literal("Dye"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.ART_OF_WAR, new BasicSingleAppender(
					Text.literal("Art of War"),
					EMPTY
			)),
			Map.entry(Calculation.Type.ART_OF_PEACE, new BasicSingleAppender(
					Text.literal("Art of Peace"),
					EMPTY
			)),
			Map.entry(Calculation.Type.FARMING_FOR_DUMMIES, new BasicSingleAppender(
					Text.literal("Farming For Dummies"),
					EMPTY
			)),
			Map.entry(Calculation.Type.TALISMAN_ENRICHMENT, new BasicSingleAppender(
					Text.literal("Talisman Enrichment"),
					EMPTY
			)),
			Map.entry(Calculation.Type.RECOMBOBULATOR, new BasicSingleAppender(
					Text.literal("Recombobulator"),
					EMPTY
			)),
			Map.entry(Calculation.Type.GEMSTONE_SLOT, new BasicSingleAppender(
					Text.literal("Gemstone Slot"),
					EMPTY
			)),
			Map.entry(Calculation.Type.GEMSTONE, new BasicListAppender(
					Text.literal("Gemstones"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.POWER_SCROLL, new BasicSingleAppender(
					Text.literal("Power Scroll"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.REFORGE, new BasicSingleAppender(
					Text.literal("Reforge"),
					s -> {
						//noinspection UnstableApiUsage
						String neuId = ItemConstants.REFORGES.get(s);
						if (neuId == null) return Text.literal(s);
						return ITEM_NAME.apply(neuId);
					}
			)),
			Map.entry(Calculation.Type.MASTER_STAR, new BasicListAppender(
					Text.literal("Master Stars"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.WITHER_BLADE_SCROLL, new BasicListAppender(
					Text.literal("Wither Blade Scrolls"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.DRILL_PART, new BasicListAppender(
					Text.literal("Drill Parts"),
					ITEM_NAME
			)),
			Map.entry(Calculation.Type.POLARVOID_BOOK, new BasicSingleAppender(
					Text.literal("Polarvoid Book"),
					EMPTY
			)),
			Map.entry(Calculation.Type.DIVAN_POWDER_COATING, new BasicSingleAppender(
					Text.literal("Divan Powder Coating"),
					EMPTY
			)),
			Map.entry(Calculation.Type.ETHERWARP_CONDUIT, new BasicSingleAppender(
					Text.literal("Etherwarp Conduit"),
					EMPTY
			)),
			Map.entry(Calculation.Type.NEW_YEAR_CAKES, new BasicSingleAppender(
					Text.literal("New Year Cakes"),
					EMPTY
			)),
			Map.entry(Calculation.Type.PET_ITEM, new BasicSingleAppender(
					Text.literal("Pet Item"),
					ITEM_NAME
			))
	));

	private final NetworthResult networthResult;
	private final EnumMap<Calculation.Type, List<Calculation>> map;
	private ScrollableLayoutWidget scrollable;

	@Init
	public static void initClass() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof HandledScreen<?> handledScreen) {
				ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
					if (key != GLFW.GLFW_KEY_I) return;
					Slot slot = ((HandledScreenAccessor) handledScreen).getFocusedSlot();
					if (slot == null || !slot.hasStack()) return;
					NetworthResult networth = NetworthCalculator.getItemNetworth(slot.getStack());
					if (networth.price() > 0) client.setScreen(new ValueBreakdownPopup(screen, networth));
				});
			}
		});
	}

	protected ValueBreakdownPopup(Screen backgroundScreen, NetworthResult networthResult) {
		super(Text.empty(), backgroundScreen);
		this.networthResult = networthResult;

		this.map = new EnumMap<>(Calculation.Type.class);
		for (Calculation calculation : networthResult.calculations()) {
			map.computeIfAbsent(calculation.type(), ignored -> new ArrayList<>()).add(calculation);
		}
	}

	@Override
	protected void init() {
		DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
		layout.add(createTextWidget(Text.literal("Base Item Price: ").append(getCoinsText(networthResult.base(), networthResult.price())), textRenderer));
		for (Map.Entry<Calculation.Type, List<Calculation>> entry : map.entrySet()) {
			LayoutAppender appender = FORMATTERS.get(entry.getKey());
			if (appender == EMPTY_APPENDER) continue;
			layout.add(EmptyWidget.ofHeight(5));
			if (appender != null) {
				appender.appendTo(networthResult, entry.getValue(), layout);
				continue;
			}
			layout.add(createTextWidget(Text.literal(entry.getKey().toString()), textRenderer));
			for (Calculation calculation : entry.getValue()) {
				layout.add(createTextWidget(Text.literal(calculation.id() + ": " + calculation.price() + " coins"), textRenderer), p -> p.marginLeft(20));
			}
		}
		layout.add(EmptyWidget.ofHeight(10));
		layout.add(createTextWidget(Text.literal("Total: ").append(getCoinsText(networthResult.price())), textRenderer), Positioner::alignRight);
		scrollable = new ScrollableLayoutWidget(client, layout, 300);
		scrollable.setHeight(200);
		scrollable.forEachChild(this::addDrawableChild);
		super.init();
	}

	@Override
	protected void refreshWidgetPositions() {
		super.refreshWidgetPositions();
		scrollable.refreshPositions();
		scrollable.setPosition((width - scrollable.getWidth()) / 2, (height - scrollable.getHeight()) / 2);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, scrollable.getX(), scrollable.getY(), scrollable.getWidth(), scrollable.getHeight());
	}

	private static Text getCoinsText(double price)  {
		return getCoinsText(price, 0);
	}

	private static Text getCoinsText(double price, double totalPrice) {
		MutableText text = Text.literal(Formatters.FLOAT_NUMBERS.format(price) + " coins").formatted(Formatting.GOLD);
		if (totalPrice > 0) {
			text.fillStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Text.literal(Formatters.FLOAT_NUMBERS.format(price / totalPrice * 100) + "% of total price"))));
		}
		return text;
	}

	private static MultilineTextWidget createTextWidget(Text text, TextRenderer textRenderer) {
		return new MultilineTextWidget(text, textRenderer).setStyleConfig(true, s -> {});
	}

	public interface LayoutAppender {

		void appendTo(NetworthResult networthResult, List<Calculation> calculations, DirectionalLayoutWidget layout);
		default void appendCountAndPrice(Calculation calc, double totalPrice, MutableText empty) {
			if (calc.count() > 1)
				empty.append(Text.literal(" x").formatted(Formatting.GRAY)).append(Text.literal(String.valueOf(calc.count())).formatted(Formatting.YELLOW));
			empty.append(Text.literal(" (").formatted(Formatting.GRAY));
			empty.append(getCoinsText(calc.price(), totalPrice));
			empty.append(Text.literal(")").formatted(Formatting.GRAY));
		}

	}
	private record BasicSingleAppender(Text displayName, Function<String, Text> idFormatter,
									   boolean hideIfWorthNothing) implements LayoutAppender {


		private BasicSingleAppender(Text displayName, Function<String, Text> idFormatter) {
			this(displayName, idFormatter, true);
		}
		@Override
		public void appendTo(NetworthResult networthResult, List<Calculation> calculations, DirectionalLayoutWidget layout) {
			if (calculations.size() > 1) {
				LOGGER.warn("More than one calculation was found for type {}", calculations.getFirst().type());
			}
			Calculation calc = calculations.getFirst();
			if (calc.price() <= 0 && hideIfWorthNothing) return;
			MutableText empty = Text.empty();
			empty.append(displayName());
			Text apply = idFormatter.apply(calc.id());
			if (!apply.getString().isBlank()) empty.append(": ").append(apply);
			appendCountAndPrice(calc, networthResult.price(), empty);
			layout.add(createTextWidget(empty, MinecraftClient.getInstance().textRenderer));
		}

	}

	private record BasicListAppender(Text displayName, Function<String, Text> idFormatter) implements LayoutAppender {
		@Override
		public void appendTo(NetworthResult networthResult, List<Calculation> calculations, DirectionalLayoutWidget layout) {
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			layout.add(createTextWidget(displayName, textRenderer));
			double total = 0;
			for (Calculation calc : calculations) {
				MutableText empty = Text.empty();
				Text apply = idFormatter.apply(calc.id());
				empty.append(apply);
				appendCountAndPrice(calc, networthResult.price(), empty);
				total += calc.price();
				layout.add(createTextWidget(empty, textRenderer), p -> p.marginLeft(15));
			}
			layout.add(createTextWidget(Text.literal("Total: ").append(getCoinsText(total, networthResult.price())), textRenderer), p -> p.marginLeft(10));
		}
	}
}
