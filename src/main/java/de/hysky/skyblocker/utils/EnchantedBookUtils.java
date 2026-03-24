package de.hysky.skyblocker.utils;

import de.hysky.skyblocker.annotations.Init;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Map;

public final class EnchantedBookUtils {
	private static final Map<String, String> API_ID_OVERRIDES = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());

	@Init
	public static void init() {
		NEURepoManager.runAsyncAfterLoad(() -> {
			API_ID_OVERRIDES.clear();
			NEURepoManager.forEachItem(item -> {
				String itemId = item.getSkyblockItemId();
				if (!itemId.contains(";")) return;
				if (!item.getMinecraftItemId().equals("minecraft:enchanted_book")) return;

				String actualId = NEURepoManager.getConstants().getBazaarStocks().getBazaarStockOrDefault(itemId);
				if (itemId.equals(actualId)) return; // not in bazaar stocks
				String guessId = getApiIdByName(TextTransformer.fromLegacy(item.getLore().getFirst()));
				if (guessId.equals(actualId)) return; // not renamed
				API_ID_OVERRIDES.put(guessId, actualId);
			});
		});
	}

	public static String getApiIdByName(Component enchantName) {
		String name = enchantName.getString().toUpperCase(Locale.ENGLISH);
		name = name.replace("BUY ", "").replace("SELL ", "").replace("'", "").replace("-", "_");

		String[] parts = name.split(" ");
		if (parts.length == 0) return "";
		if (RomanNumerals.isValidRomanNumeral(parts[parts.length - 1])) {
			parts[parts.length - 1] = String.valueOf(RomanNumerals.romanToDecimal(parts[parts.length - 1]));
		}
		boolean isUltimate = !enchantName.getSiblings().isEmpty() && enchantName.getSiblings().getLast().getStyle().isBold();
		String id = "ENCHANTMENT_" + (isUltimate ? "ULTIMATE_" : "") + String.join("_", parts);
		return API_ID_OVERRIDES.getOrDefault(id, id);
	}
}
