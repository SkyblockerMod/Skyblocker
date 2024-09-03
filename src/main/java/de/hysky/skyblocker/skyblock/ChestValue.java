package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.mixins.accessors.ScreenAccessor;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChestValue {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChestValue.class);
	private static final Set<String> DUNGEON_CHESTS = Set.of("Wood Chest", "Gold Chest", "Diamond Chest", "Emerald Chest", "Obsidian Chest", "Bedrock Chest");
	private static final Pattern ESSENCE_PATTERN = Pattern.compile("(?<type>[A-Za-z]+) Essence x(?<amount>[0-9]+)");
	private static final Pattern MINION_PATTERN = Pattern.compile("Minion (I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII)$");
	private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (Utils.isOnSkyblock() && screen instanceof GenericContainerScreen genericContainerScreen) {
				Text title = screen.getTitle();
				String titleString = title.getString();
				if (DUNGEON_CHESTS.contains(titleString)) {
					if (SkyblockerConfigManager.get().dungeons.dungeonChestProfit.enableProfitCalculator) {
						ScreenEvents.afterTick(screen).register(ignored -> {
							Text dungeonChestProfit = getDungeonChestProfit(genericContainerScreen.getScreenHandler());
							if (dungeonChestProfit != null)
								addValueToContainer(genericContainerScreen, dungeonChestProfit, title);
						});
					}
				} else if (SkyblockerConfigManager.get().uiAndVisuals.chestValue.enableChestValue && !titleString.equals("SkyBlock Menu")) {
					boolean minion = MINION_PATTERN.matcher(title.getString().trim()).find();
					Screens.getButtons(screen).add(ButtonWidget
							.builder(Text.literal("$"), buttonWidget -> {
								Screens.getButtons(screen).remove(buttonWidget);
								ScreenEvents.afterTick(screen).register(ignored -> {
									Text chestValue = getChestValue(genericContainerScreen.getScreenHandler(), minion);
									if (chestValue != null) {
										addValueToContainer(genericContainerScreen, chestValue, title);
									}
								});

							})
							.dimensions(((HandledScreenAccessor) genericContainerScreen).getX() + ((HandledScreenAccessor) genericContainerScreen).getBackgroundWidth() - 16, ((HandledScreenAccessor) genericContainerScreen).getY() + 4, 12, 12)
							.tooltip(minion ? Tooltip.of(Text.translatable("skyblocker.config.general.minionValue.@Tooltip")) : Tooltip.of(Text.translatable("skyblocker.config.general.chestValue.@Tooltip")))
							.build()
					);
				}
			}
		});
	}

	private static @Nullable Text getDungeonChestProfit(GenericContainerScreenHandler handler) {
		try {
			double profit = 0;
			boolean hasIncompleteData = false, usedKismet = false;
			List<Slot> slots = handler.slots.subList(0, handler.getRows() * 9);

			//If the item stack for the "Open Reward Chest" button or the kismet button hasn't been sent to the client yet
			if (slots.get(31).getStack().isEmpty() || slots.get(50).getStack().isEmpty()) return null;

			for (Slot slot : slots) {
				ItemStack stack = slot.getStack();
				if (stack.isEmpty()) {
					continue;
				}

				String name = stack.getName().getString();
				String skyblockApiId = stack.getSkyblockApiId();

				//Regular item price
				if (!skyblockApiId.isEmpty()) {
					DoubleBooleanPair priceData = ItemUtils.getItemPrice(skyblockApiId);

					if (!priceData.rightBoolean()) hasIncompleteData = true;

					//Add the item price to the profit
					profit += priceData.leftDouble() * stack.getCount();

					continue;
				}

				//Essence price
				if (name.contains("Essence") && SkyblockerConfigManager.get().dungeons.dungeonChestProfit.includeEssence) {
					Matcher matcher = ESSENCE_PATTERN.matcher(name);

					if (matcher.matches()) {
						String type = matcher.group("type");
						int amount = Integer.parseInt(matcher.group("amount"));

						DoubleBooleanPair priceData = ItemUtils.getItemPrice(("ESSENCE_" + type).toUpperCase());

						if (!priceData.rightBoolean()) hasIncompleteData = true;

						//Add the price of the essence to the profit
						profit += priceData.leftDouble() * amount;

						continue;
					}
				}

				//Determine the cost of the chest
				if (name.contains("Open Reward Chest")) {
					String foundString = searchLoreFor(stack, "Coins");

					//Incase we're searching the free chest
					if (!StringUtils.isBlank(foundString)) {
						profit -= Integer.parseInt(foundString.replaceAll("[^0-9]", ""));
					}

					continue;
				}

				//Determine if a kismet was used or not
				if (name.contains("Reroll Chest")) {
					usedKismet = !StringUtils.isBlank(searchLoreFor(stack, "You already rerolled a chest!"));
				}
			}

			if (SkyblockerConfigManager.get().dungeons.dungeonChestProfit.includeKismet && usedKismet) {
				DoubleBooleanPair kismetPriceData = ItemUtils.getItemPrice("KISMET_FEATHER");

				if (!kismetPriceData.rightBoolean()) hasIncompleteData = true;

				profit -= kismetPriceData.leftDouble();
			}

			return getProfitText((long) profit, hasIncompleteData);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profit Calculator] Failed to calculate dungeon chest profit! ", e);
		}

		return null;
	}

	private static @Nullable Text getChestValue(GenericContainerScreenHandler handler, boolean minion) {
		try {
			double value = 0;
			boolean hasIncompleteData = false;
			List<Slot> slots = minion ? getMinionSlots(handler) : handler.slots.subList(0, handler.getRows() * 9);

			for (Slot slot : slots) {
				ItemStack stack = slot.getStack();
				if (stack.isEmpty()) {
					continue;
				}
				String coinsLine;
				if (minion && slot.id == 28 && stack.isOf(Items.HOPPER) && (coinsLine = ItemUtils.getLoreLineIf(stack, s -> s.contains("Held Coins:"))) != null) {
					String source = coinsLine.split(":")[1];
					try {
						value += DecimalFormat.getNumberInstance(java.util.Locale.US).parse(source.trim()).doubleValue();
					} catch (ParseException e) {
						LOGGER.warn("[Skyblocker] Failed to parse {}", source);
					}
					continue;
				}

				String id = stack.getSkyblockApiId();

				if (!id.isEmpty()) {
					DoubleBooleanPair priceData = ItemUtils.getItemPrice(id);

					if (!priceData.rightBoolean()) hasIncompleteData = true;

					value += NetworthCalculator.getItemNetworth(stack).price();
				}
			}

			return getValueText((long) value, hasIncompleteData);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Value Calculator] Failed to calculate dungeon chest value! ", e);
		}

		return null;
	}

	private static @NotNull List<Slot> getMinionSlots(GenericContainerScreenHandler handler) {
		return handler.slots.subList(0, handler.getRows() * 9).stream().filter(slot -> {
			int x = slot.id % 9;
			int y = slot.id / 9;
			return x > 2 && x < 8 && y > 1 && y < 5 || slot.id == 28;
		}).toList();
	}

	/**
	 * Searches for a specific string of characters in the name and lore of an item
	 */
	private static String searchLoreFor(ItemStack stack, String searchString) {
		return ItemUtils.getLoreLineIf(stack, line -> line.contains(searchString));
	}

	static Text getProfitText(long profit, boolean hasIncompleteData) {
		DungeonsConfig.DungeonChestProfit config = SkyblockerConfigManager.get().dungeons.dungeonChestProfit;
		return Text.literal((profit > 0 ? " +" : ' ') + FORMATTER.format(profit) + " Coins").formatted(hasIncompleteData ? config.incompleteColor : (Math.abs(profit) < config.neutralThreshold) ? config.neutralColor : (profit > 0) ? config.profitColor : config.lossColor);
	}

	static Text getValueText(long value, boolean hasIncompleteData) {
		UIAndVisualsConfig.ChestValue config = SkyblockerConfigManager.get().uiAndVisuals.chestValue;
		return Text.literal(' ' + FORMATTER.format(value) + " Coins").formatted(hasIncompleteData ? config.incompleteColor : config.color);
	}

	private static void addValueToContainer(GenericContainerScreen genericContainerScreen, Text chestValue, Text title) {
		Screens.getButtons(genericContainerScreen).removeIf(clickableWidget -> clickableWidget instanceof ChestValueTextWidget);
		int backgroundWidth = ((HandledScreenAccessor) genericContainerScreen).getBackgroundWidth();
		int y = ((HandledScreenAccessor) genericContainerScreen).getY();
		int x = ((HandledScreenAccessor) genericContainerScreen).getX();
		((ScreenAccessor) genericContainerScreen).setTitle(Text.empty());
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		int chestValueWidth = Math.min(textRenderer.getWidth(chestValue), Math.max((backgroundWidth - 8) / 2 - 2, backgroundWidth - 8 - textRenderer.getWidth(title)));

		TextWidget chestValueWidget = new ChestValueTextWidget(chestValueWidth, textRenderer.fontHeight, chestValue, textRenderer);
		chestValueWidget.setPosition(x + backgroundWidth - chestValueWidget.getWidth() - 4, y + 6);
		Screens.getButtons(genericContainerScreen).add(chestValueWidget);

		ChestValueTextWidget chestTitleWidget = new ChestValueTextWidget(backgroundWidth - 8 - chestValueWidth - 2, textRenderer.fontHeight, title.copy().fillStyle(Style.EMPTY.withColor(4210752)), textRenderer);
		chestTitleWidget.shadow = false;
		chestTitleWidget.setPosition(x + 8, y + 6);
		Screens.getButtons(genericContainerScreen).add(chestTitleWidget);
	}

	private static class ChestValueTextWidget extends TextWidget {

		public boolean shadow = true;

		public ChestValueTextWidget(int width, int height, Text message, TextRenderer textRenderer) {
			super(width, height, message, textRenderer);
			alignLeft();
		}

		@Override
		public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			draw(context, getTextRenderer(), getMessage(), getX(), getRight());
		}

		// Yoinked from ClickableWidget
		protected void draw(
				DrawContext context, TextRenderer textRenderer, Text text, int startX, int endX
		) {
			int i = textRenderer.getWidth(text);
			int k = endX - startX;
			if (i > k) {
				int l = i - k;
				double d = (double) Util.getMeasuringTimeMs() / 600.0;
				double e = Math.max((double) l * 0.5, 3.0);
				double f = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d / e)) / 2.0 + 0.5;
				double g = MathHelper.lerp(f, 0.0, l);
				context.enableScissor(startX, getY(), endX, getY() + textRenderer.fontHeight);
				context.drawText(textRenderer, text, startX - (int) g, getY(), -1, shadow);
				context.disableScissor();
			} else {
				context.drawText(textRenderer, text, startX, getY(), -1, shadow);
			}
		}
	}
}
