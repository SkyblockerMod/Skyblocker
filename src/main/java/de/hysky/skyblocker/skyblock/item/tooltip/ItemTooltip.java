package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.events.ItemPriceUpdateEvent;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.CraftPriceTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.info.DataTooltipInfoType;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemTooltip {
	public static final Logger LOGGER = LoggerFactory.getLogger(ItemTooltip.class.getName());
	private static final Minecraft client = Minecraft.getInstance();
	public static final java.util.function.Supplier<GeneralConfig.ItemTooltip> config = () -> SkyblockerConfigManager.get().general.itemTooltip;
	private static volatile boolean sentNullWarning = false;

	/**
	 * @deprecated Use {@link ItemStack#getNeuName()} instead
	 */
	@Deprecated(since = "1.22.0", forRemoval = true)
	public static String getNeuName(String id, String apiId) {
		LOGGER.error("[Skyblocker Item Tooltip] ItemTooltip.getNeuName is deprecated and will not work. Use ItemStack#getNeuName instead.");
		return "";
	}

	public static void nullWarning() {
		if (!sentNullWarning && client.player != null) {
			LOGGER.warn(Constants.PREFIX.get().append(Component.translatable("skyblocker.itemTooltip.nullMessage").withStyle(ChatFormatting.RED)).getString());
			sentNullWarning = true;
		}
	}

	/**
	 * Generates a formatted text message for displaying coin prices in tooltips, where the price is given per item.
	 *
	 * @param price the price of a single item
	 * @param count the number of items being priced
	 */
	public static Component getCoinsMessage(double price, int count) {
		return getCoinsMessage(price, count, false);
	}

	/**
	 * Generates a formatted text message for displaying coin prices in tooltips.
	 *
	 * @param preCounted Whether the count is already factored into the price. False if the price is per item, true if the price is the total price for the given count.
	 */
	public static Component getCoinsMessage(double price, int count, boolean preCounted) {
		// Format the price string once
		String priceString = String.format(Locale.ENGLISH, "%1$,.1f", preCounted ? price / count : price);

		// If count is 1, return a simple message
		if (count == 1) {
			return Component.literal(priceString + " Coins").withStyle(ChatFormatting.DARK_AQUA);
		}

		// If count is greater than 1, include the "each" information
		String priceStringTotal = String.format(Locale.ENGLISH, "%1$,.1f", preCounted ? price : price * count);

		return Component.literal(priceStringTotal + " Coins ").withStyle(ChatFormatting.DARK_AQUA)
				.append(Component.literal("(" + priceString + " each)").withStyle(ChatFormatting.GRAY));
	}

	// If these options is true beforehand, the client will get first data of these options while loading.
	// After then, it will only fetch the data if it is on Skyblock.
	public static int minute = 0;

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(() -> {
			if (!Utils.isOnSkyblock() && 0 < minute) {
				sentNullWarning = false;
				return;
			}

			if (++minute % 60 == 0) {
				sentNullWarning = false;
			}

			CompletableFuture.allOf(Stream.of(TooltipInfoType.NPC, TooltipInfoType.BAZAAR, TooltipInfoType.LOWEST_BINS, TooltipInfoType.ONE_DAY_AVERAGE, TooltipInfoType.THREE_DAY_AVERAGE, TooltipInfoType.MOTES, TooltipInfoType.MUSEUM, TooltipInfoType.COLOR, TooltipInfoType.ACCESSORIES)
					.map(DataTooltipInfoType.class::cast)
					.map(DataTooltipInfoType::downloadIfEnabled)
					.toArray(CompletableFuture[]::new)
			).thenRun(ItemPriceUpdateEvent.ON_PRICE_UPDATE.invoker()::onPriceUpdate
			).exceptionally(e -> {
				LOGGER.error("[Skyblocker] Encountered unknown error while downloading tooltip data", e);
				return null;
			});

			CraftPriceTooltip.clearCache();
		}, 1200, true);
	}
}
