package de.hysky.skyblocker.skyblock.item.slottext.adders;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimaps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class EnchantmentLevelAdderTest {
	@Test
	void testEnchantmentAbbreviations() {
		checkForDuplicates(EnchantmentAbbreviationAdder.ENCHANTMENT_ABBREVIATIONS);
	}

	@Test
	void testUltimateEnchantmentAbbreviations() {
		checkForDuplicates(EnchantmentAbbreviationAdder.ULTIMATE_ENCHANTMENT_ABBREVIATIONS);
	}

	private void checkForDuplicates(Map<String, String> abbreviations) {
		if (abbreviations.size() == abbreviations.values().stream().distinct().count()) return;
		abbreviations.entrySet().stream().collect(Multimaps.toMultimap(Map.Entry::getValue, Map.Entry::getKey, ArrayListMultimap::create))
				.asMap().entrySet().stream().filter(e -> e.getValue().size() > 1)
				.map(e -> "Duplicate abbreviations: %s for %s".formatted(e.getKey(), e.getValue()))
				.forEach(Assertions::fail);
	}
}
