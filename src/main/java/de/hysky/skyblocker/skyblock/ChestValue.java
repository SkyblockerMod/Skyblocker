package de.hysky.skyblocker.skyblock;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.mixins.accessors.ScreenAccessor;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.longs.LongBooleanPair;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChestValue {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChestValue.class);
	private static final Set<String> DUNGEON_CHESTS = Set.of("Wood Chest", "Gold Chest", "Diamond Chest", "Emerald Chest", "Obsidian Chest", "Bedrock Chest");
	private static final Pattern ESSENCE_PATTERN = Pattern.compile("(?<type>[A-Za-z]+) Essence x(?<amount>[0-9]+)");
	private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (Utils.isOnSkyblock() && screen instanceof GenericContainerScreen genericContainerScreen) {
				Text title = screen.getTitle();
				String titleString = title.getString();
				if (DUNGEON_CHESTS.contains(titleString)) {
					if (SkyblockerConfigManager.get().locations.dungeons.dungeonChestProfit.enableProfitCalculator) {
						ScreenEvents.afterTick(screen).register(screen_ ->
								((ScreenAccessor) screen).setTitle(getDungeonChestProfit(genericContainerScreen.getScreenHandler(), title, titleString, client))
						);
					}
				} else if (SkyblockerConfigManager.get().general.chestValue.enableChestValue && !titleString.equals("SkyBlock Menu")) {
					Screens.getButtons(screen).add(ButtonWidget
							.builder(Text.literal("$"), buttonWidget -> {
								Screens.getButtons(screen).remove(buttonWidget);
								ScreenEvents.afterTick(screen).register(screen_ ->
										((ScreenAccessor) screen).setTitle(getChestValue(genericContainerScreen.getScreenHandler(), title, titleString))
								);
							})
							.dimensions(((HandledScreenAccessor) genericContainerScreen).getX() + ((HandledScreenAccessor) genericContainerScreen).getBackgroundWidth() - 16, ((HandledScreenAccessor) genericContainerScreen).getY() + 4, 12, 12)
							.tooltip(Tooltip.of(Text.translatable("text.autoconfig.skyblocker.option.general.chestValue.@Tooltip")))
							.build()
					);
				}
			}
		});
	}

	private static Text getDungeonChestProfit(GenericContainerScreenHandler handler, Text title, String titleString, MinecraftClient client) {
		try {
			long profit = 0;
			boolean hasIncompleteData = false, usedKismet = false;
			List<Slot> slots = handler.slots.subList(0, handler.getRows() * 9);

			//If the item stack for the "Open Reward Chest" button or the kismet button hasn't been sent to the client yet
			if (slots.get(31).getStack().isEmpty() || slots.get(50).getStack().isEmpty()) return title;

			for (Slot slot : slots) {
				ItemStack stack = slot.getStack();
				if (stack.isEmpty()) {
					continue;
				}

				String name = stack.getName().getString();
				String id = ItemTooltip.getInternalNameFromNBT(stack, false);

				//Regular item price
				if (id != null) {
					LongBooleanPair priceData = getItemPrice(id);

					if (!priceData.rightBoolean()) hasIncompleteData = true;

					//Add the item price to the profit
					profit += priceData.leftLong() * stack.getCount();

					continue;
				}

				//Essence price
				if (name.contains("Essence") && SkyblockerConfigManager.get().locations.dungeons.dungeonChestProfit.includeEssence) {
					Matcher matcher = ESSENCE_PATTERN.matcher(name);

					if (matcher.matches()) {
						String type = matcher.group("type");
						int amount = Integer.parseInt(matcher.group("amount"));

						LongBooleanPair priceData = getItemPrice(("ESSENCE_" + type).toUpperCase());

						if (!priceData.rightBoolean()) hasIncompleteData = true;

						//Add the price of the essence to the profit
						profit += priceData.leftLong() * amount;

						continue;
					}
				}

				//Determine the cost of the chest
				if (name.contains("Open Reward Chest")) {
					String foundString = searchLoreFor(stack, client, "Coins");

					//Incase we're searching the free chest
					if (!StringUtils.isBlank(foundString)) {
						profit -= Integer.parseInt(foundString.replaceAll("[^0-9]", ""));
					}

					continue;
				}

				//Determine if a kismet was used or not
				if (name.contains("Reroll Chest")) {
					usedKismet = !StringUtils.isBlank(searchLoreFor(stack, client, "You already rerolled a chest!"));
				}
			}

			if (SkyblockerConfigManager.get().locations.dungeons.dungeonChestProfit.includeKismet && usedKismet) {
				LongBooleanPair kismetPriceData = getItemPrice("KISMET_FEATHER");

				if (!kismetPriceData.rightBoolean()) hasIncompleteData = true;

				profit -= kismetPriceData.leftLong();
			}

			return Text.literal(titleString).append(getProfitText(profit, hasIncompleteData));
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profit Calculator] Failed to calculate dungeon chest profit! ", e);
		}

		return title;
	}

	private static Text getChestValue(GenericContainerScreenHandler handler, Text title, String titleString) {
		try {
			long value = 0;
			boolean hasIncompleteData = false;
			List<Slot> slots = handler.slots.subList(0, handler.getRows() * 9);

			for (Slot slot : slots) {
				ItemStack stack = slot.getStack();
				if (stack.isEmpty()) {
					continue;
				}

				String id = ItemTooltip.getInternalNameFromNBT(stack, false);

				if (id != null) {
					LongBooleanPair priceData = getItemPrice(id);

					if (!priceData.rightBoolean()) hasIncompleteData = true;

					value += priceData.leftLong() * stack.getCount();
				}
			}

			return Text.literal(titleString).append(getValueText(value, hasIncompleteData));
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Value Calculator] Failed to calculate dungeon chest value! ", e);
		}

		return title;
	}

	/**
	 * @return An {@link LongBooleanPair} with the {@code left long} representing the item's price, and the {@code right boolean} indicating if the price
	 * was based on complete data.
	 */
	private static LongBooleanPair getItemPrice(String id) {
		JsonObject bazaarPrices = TooltipInfoType.BAZAAR.getData();
		JsonObject lbinPrices = TooltipInfoType.LOWEST_BINS.getData();

		if (bazaarPrices == null || lbinPrices == null) return LongBooleanPair.of(0L, false);

		if (bazaarPrices.has(id)) {
			JsonObject item = bazaarPrices.get(id).getAsJsonObject();
			boolean isPriceNull = item.get("sellPrice").isJsonNull();

			return LongBooleanPair.of(isPriceNull ? 0L : (long) item.get("sellPrice").getAsDouble(), !isPriceNull);
		}

		if (lbinPrices.has(id)) {
			return LongBooleanPair.of((long) lbinPrices.get(id).getAsDouble(), true);
		}

		return LongBooleanPair.of(0L, false);
	}

	/**
	 * Searches for a specific string of characters in the name and lore of an item
	 */
	private static String searchLoreFor(ItemStack stack, MinecraftClient client, String searchString) {
		return ItemUtils.getLoreLineIf(stack, line -> line.contains(searchString));
	}

	static Text getProfitText(long profit, boolean hasIncompleteData) {
		SkyblockerConfig.DungeonChestProfit config = SkyblockerConfigManager.get().locations.dungeons.dungeonChestProfit;
		return Text.literal((profit > 0 ? " +" : ' ') + FORMATTER.format(profit) + " Coins").formatted(hasIncompleteData ? config.incompleteColor : (Math.abs(profit) < config.neutralThreshold) ? config.neutralColor : (profit > 0) ? config.profitColor : config.lossColor);
	}

	static Text getValueText(long value, boolean hasIncompleteData) {
		SkyblockerConfig.ChestValue config = SkyblockerConfigManager.get().general.chestValue;
		return Text.literal(' ' + FORMATTER.format(value) + " Coins").formatted(hasIncompleteData ? config.incompleteColor : config.color);
	}
}
