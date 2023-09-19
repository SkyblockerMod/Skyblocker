package me.xmrvizzy.skyblocker.skyblock.dungeon;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntBooleanPair;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.mixin.accessor.ScreenAccessor;
import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonChestProfit {
	private static final Logger LOGGER = LoggerFactory.getLogger(DungeonChestProfit.class);
	private static final Pattern ESSENCE_PATTERN = Pattern.compile("(?<type>[A-Za-z]+) Essence x(?<amount>[0-9]+)");
	private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> ScreenEvents.afterTick(screen).register(screen1 -> {
			if (Utils.isOnSkyblock() && screen instanceof GenericContainerScreen genericContainerScreen && genericContainerScreen.getScreenHandler().getType() == ScreenHandlerType.GENERIC_9X6) {
				((ScreenAccessor) screen).setTitle(getChestProfit(genericContainerScreen.getScreenHandler(), screen.getTitle(), client));
			}
		}));
	}

	public static Text getChestProfit(GenericContainerScreenHandler handler, Text title, MinecraftClient client) {
		try {
			if (SkyblockerConfig.get().locations.dungeons.dungeonChestProfit.enableProfitCalculator && isDungeonChest(title.getString())) {
				int profit = 0;
				boolean hasIncompleteData = false, usedKismet = false;
				List<Slot> slots = handler.slots.subList(0, handler.getRows() * 9);

				//If the item stack for the "Open Reward Chest" button or the kismet button hasn't been sent to the client yet
				if (slots.get(31).getStack().isEmpty() || slots.get(50).getStack().isEmpty()) return title;

				for (Slot slot : slots) {
					ItemStack stack = slot.getStack();

					if (!stack.isEmpty()) {
						String name = stack.getName().getString();
						String id = PriceInfoTooltip.getInternalNameFromNBT(stack, false);

						//Regular item price
						if (id != null) {
							IntBooleanPair priceData = getItemPrice(id);

							if (!priceData.rightBoolean()) hasIncompleteData = true;

							//Add the item price to the profit
							profit += priceData.leftInt();

							continue;
						}

						//Essence price
						if (name.contains("Essence") && SkyblockerConfig.get().locations.dungeons.dungeonChestProfit.includeEssence) {
							Matcher matcher = ESSENCE_PATTERN.matcher(name);

							if (matcher.matches()) {
								String type = matcher.group("type");
								int amount = Integer.parseInt(matcher.group("amount"));

								IntBooleanPair priceData = getItemPrice(("ESSENCE_" + type).toUpperCase());

								if (!priceData.rightBoolean()) hasIncompleteData = true;

								//Add the price of the essence to the profit
								profit += priceData.leftInt() * amount;

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
				}

				if (SkyblockerConfig.get().locations.dungeons.dungeonChestProfit.includeKismet && usedKismet) {
					IntBooleanPair kismetPriceData = getItemPrice("KISMET_FEATHER");

					if (!kismetPriceData.rightBoolean()) hasIncompleteData = true;

					profit -= kismetPriceData.leftInt();
				}

				return Text.literal(title.getString()).append(getProfitText(profit, hasIncompleteData));
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profit Calculator] Failed to calculate dungeon chest profit! ", e);
		}

		return title;
	}

	/**
	 * @return An {@link IntBooleanPair} with the {@code left int} representing the item's price, and the {@code right boolean} indicating if the price
	 * was based on complete data.
	 */
	private static IntBooleanPair getItemPrice(String id) {
		JsonObject bazaarPrices = PriceInfoTooltip.getBazaarPrices();
		JsonObject lbinPrices = PriceInfoTooltip.getLBINPrices();

		if (bazaarPrices == null || lbinPrices == null) return IntBooleanPair.of(0, false);

		if (bazaarPrices.has(id)) {
			JsonObject item = bazaarPrices.get(id).getAsJsonObject();
			boolean isPriceNull = item.get("sellPrice").isJsonNull();

			return IntBooleanPair.of(isPriceNull ? 0 : (int) item.get("sellPrice").getAsDouble(), !isPriceNull);
		}

		if (lbinPrices.has(id)) {
			return IntBooleanPair.of((int) lbinPrices.get(id).getAsDouble(), true);
		}

		return IntBooleanPair.of(0, false);
	}

	/**
	 * Searches for a specific string of characters in the name and lore of an item
	 */
	private static String searchLoreFor(ItemStack stack, MinecraftClient client, String searchString) {
		return stack.getTooltip(client.player, TooltipContext.BASIC).stream().map(Text::getString).filter(line -> line.contains(searchString)).findAny().orElse(null);
	}

	private static boolean isDungeonChest(String name) {
		return name.equals("Wood Chest") || name.equals("Gold Chest") || name.equals("Diamond Chest") || name.equals("Emerald Chest") || name.equals("Obsidian Chest") || name.equals("Bedrock Chest");
	}

	private static Text getProfitText(int profit, boolean hasIncompleteData) {
		SkyblockerConfig.DungeonChestProfit config = SkyblockerConfig.get().locations.dungeons.dungeonChestProfit;
		return getProfitText(profit, hasIncompleteData, config.neutralThreshold, config.neutralColor.formatting, config.profitColor.formatting, config.lossColor.formatting, config.incompleteColor.formatting);
	}

	static Text getProfitText(int profit, boolean hasIncompleteData, int neutralThreshold, Formatting neutralColor, Formatting profitColor, Formatting lossColor, Formatting incompleteColor) {
		return Text.literal((profit > 0 ? " +" : " ") + FORMATTER.format(profit)).formatted(hasIncompleteData ? incompleteColor : (Math.abs(profit) < neutralThreshold) ? neutralColor : (profit > 0) ? profitColor : lossColor);
	}
}
