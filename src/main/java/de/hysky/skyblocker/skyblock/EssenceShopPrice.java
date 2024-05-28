package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.RegexUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.longs.LongBooleanPair;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.item.TooltipType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EssenceShopPrice {
	private static final Logger LOGGER = LoggerFactory.getLogger(EssenceShopPrice.class);
	private static final Set<String> ESSENCE_SHOPS = Set.of("Dragon Essence Shop", "Spider Essence Shop", "Crimson Essence Shop", "Ice Essence Shop", "Gold Essence Shop", "Diamond Essence Shop", "Undead Essence Shop", "Wither Essence Shop");
	private static final Pattern ESSENCE_PATTERN = Pattern.compile("Cost (?<amount>[0-9,]+) (?<type>[A-Za-z]+) Essence");
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

	public EssenceShopPrice() {
		ItemTooltipCallback.EVENT.register(EssenceShopPrice::handleTooltip);
	}

	private static void handleTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> lines) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.showEssenceCost) return;
		if (!(MinecraftClient.getInstance().currentScreen instanceof GenericContainerScreen screen) || !ESSENCE_SHOPS.contains(screen.getTitle().getString())) return;

		String lore = Utils.concatenateLore(lines);
		Matcher essenceMatcher = ESSENCE_PATTERN.matcher(lore);
		OptionalLong cost = RegexUtils.getLongFromMatcher(essenceMatcher);
		long costValue = 0;
		if (cost.isPresent()) {
			String type = essenceMatcher.group("type");
			int amount = Integer.parseInt(essenceMatcher.group("amount").replace(",", ""));

			LongBooleanPair priceData = ChestValue.getItemPrice(("ESSENCE_" + type).toUpperCase());

			priceData.rightBoolean();

			costValue += priceData.leftLong() * amount;
			addPriceInfoToLore(lines, costValue);
		}
	}

	private static void addPriceInfoToLore(List<Text> lines, long cost) {
			lines.add(Text.empty()
					.append(Text.literal("Price: ").formatted(Formatting.GRAY))
					.append(Text.literal(DECIMAL_FORMAT.format(cost) + " coins").formatted(Formatting.GOLD)));
	}
}
