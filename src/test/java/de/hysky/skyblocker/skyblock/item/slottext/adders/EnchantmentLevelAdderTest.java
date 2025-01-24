package de.hysky.skyblocker.skyblock.item.slottext.adders;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class EnchantmentLevelAdderTest {
	@Test
	void testEnchantmentAbbreviations() {
		checkForDuplicates(EnchantmentLevelAdder.ENCHANTMENT_ABBREVIATIONS);
	}

	@Test
	void testUltimateEnchantmentAbbreviations() {
		checkForDuplicates(EnchantmentLevelAdder.ULTIMATE_ENCHANTMENT_ABBREVIATIONS);
	}

	private void checkForDuplicates(Map<String, String> abbreviations) {
		if (abbreviations.size() == abbreviations.values().stream().distinct().count()) return;
		Map<String, String> names = new Object2ObjectOpenHashMap<>();
		for (var entry : abbreviations.entrySet()) {
			var put = names.put(entry.getValue(), entry.getKey());
			if (put != null) {
				Assertions.fail("Duplicate abbreviation: %s for %s and %s".formatted(entry.getValue(), put, entry.getKey()));
			}
		}
	}
}
