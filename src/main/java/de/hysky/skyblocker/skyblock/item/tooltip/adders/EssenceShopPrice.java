package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RegexUtils;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EssenceShopPrice extends SimpleTooltipAdder {
	private static final Pattern ESSENCE_PATTERN = Pattern.compile("Cost (?<amount>[\\d,]+) (?<type>[A-Za-z]+) Essence");
	private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getInstance(Locale.US);
	private static final String[] ESSENCE_TYPES = {"WITHER", "SPIDER", "UNDEAD", "DRAGON", "GOLD", "DIAMOND", "ICE", "CRIMSON"};
	private static final Object2LongArrayMap<String> ESSENCE_PRICES = new Object2LongArrayMap<>(ESSENCE_TYPES, new long[8]);

	public EssenceShopPrice(int priority) {
		super("\\S+ Essence Shop", priority);
	}

	public static void refreshEssencePrices(Object2ObjectMap<String, BazaarProduct> data) {
		for (String essenceType : ESSENCE_TYPES) {
			BazaarProduct product = data.get("ESSENCE_" + essenceType);

			if (product != null) {
				OptionalDouble sellPrice = product.sellPrice();

				if (sellPrice.isPresent()) {
					ESSENCE_PRICES.put(essenceType, (long) sellPrice.getAsDouble());
				}
			}
		}
	}

	//Todo: maybe move the price value right after the essence amount ex: "1,500 Wither Essence (645k coins)"
	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		String lore = ItemUtils.concatenateLore(lines);
		Matcher essenceMatcher = ESSENCE_PATTERN.matcher(lore);
		OptionalLong cost = RegexUtils.getLongFromMatcher(essenceMatcher);
		if (cost.isEmpty()) return;

		String type = essenceMatcher.group("type");
		long priceData = ESSENCE_PRICES.getLong(type.toUpperCase(Locale.ROOT));
		if (priceData == 0) return; //Default value for getLong is 0 if no value exists for that key

		lines.add(Text.empty()
				.append(Text.literal("Essence Cost:      ").formatted(Formatting.AQUA))
				.append(Text.literal(DECIMAL_FORMAT.format(priceData * cost.getAsLong()) + " coins").formatted(Formatting.DARK_AQUA))
				.append(Text.literal(" (").formatted(Formatting.GRAY))
				.append(Text.literal(DECIMAL_FORMAT.format(priceData) + " each").formatted(Formatting.GRAY))
				.append(Text.literal(")").formatted(Formatting.GRAY))
		);
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemTooltip.showEssenceCost;
	}
}
