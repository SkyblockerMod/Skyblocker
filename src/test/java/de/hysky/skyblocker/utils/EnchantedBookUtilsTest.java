package de.hysky.skyblocker.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class EnchantedBookUtilsTest {
	@Test
	void testGetApiIdByName() {
		Assertions.assertEquals("ENCHANTMENT_CASTER_6", EnchantedBookUtils.getApiIdByName(Component.literal("Caster VI")));
		Assertions.assertEquals("ENCHANTMENT_CASTER_6", EnchantedBookUtils.getApiIdByName(order("SELL ", "Caster VI", false)));
		Assertions.assertEquals("ENCHANTMENT_TURBO_WHEAT_5", EnchantedBookUtils.getApiIdByName(order("BUY ", "Turbo-Wheat V", false)));
		Assertions.assertEquals("ENCHANTMENT_ULTIMATE_CHIMERA_5", EnchantedBookUtils.getApiIdByName(order("BUY ", "Chimera V", true)));
	}

	private static Component order(String type, String enchant, boolean ultimate) {
		return Component.empty()
				.append(Component.literal(type).withStyle(Style.EMPTY.withBold(true)))
				.append(Component.literal(enchant).withStyle(Style.EMPTY.withBold(ultimate)));
	}
}
