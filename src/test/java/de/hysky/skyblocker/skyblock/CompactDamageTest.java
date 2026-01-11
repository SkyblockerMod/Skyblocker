package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import static de.hysky.skyblocker.skyblock.CompactDamage.baseTenDigits;
import static de.hysky.skyblocker.skyblock.CompactDamage.formatToPrecision;
import static de.hysky.skyblocker.skyblock.CompactDamage.prettifyDamageNumber;

public class CompactDamageTest {
	Map<String, ChatFormatting> MODIFIERS = Util.make(new HashMap<>(), map -> {
		map.put("❤", ChatFormatting.LIGHT_PURPLE);
		map.put("+", ChatFormatting.YELLOW);
		map.put("⚔", ChatFormatting.GOLD);
	});

	@BeforeAll
	public static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		// Make crits black
		SkyblockerConfigManager.get().uiAndVisuals.compactDamage.critDamageGradientStart = new Color(CommonColors.BLACK);
		SkyblockerConfigManager.get().uiAndVisuals.compactDamage.critDamageGradientEnd = new Color(CommonColors.BLACK);
	}

	private static ArmorStand createEntityWithName(Component text) {
		ArmorStand entity = new ArmorStand(EntityType.ARMOR_STAND, null);
		entity.setInvisible(true);
		entity.setCustomNameVisible(true);
		entity.setCustomName(text);
		return entity;
	}

	private static Component getCompactText(ArmorStand entity, int maxPrecision) {
		SkyblockerConfigManager.get().uiAndVisuals.compactDamage.maxPrecision = maxPrecision;
		CompactDamage.compactDamage(entity);
		return entity.getCustomName();
	}

	private static void testCompact(Component inputText, int maxPrecision, Component expectedText) {
		Component outputText = getCompactText(createEntityWithName(inputText), maxPrecision);
		Assertions.assertEquals(expectedText, outputText);
	}

	/**
	 * Create damage text similar to how Hypixel does it
	 */
	private static Component makeInputText(String damageStr, boolean isCrit, String modifierSymbol, ChatFormatting modifierColor) {
		MutableComponent text = Component.empty();
		if (isCrit) {
			damageStr = "✧%s✧".formatted(damageStr);
			for (char chr : damageStr.toCharArray()) {
				// Hypixel would add color here, but since it will be discarded, there is no need for us to add it.
				text.append(String.valueOf(chr));
			}
		} else {
			text.append(damageStr).withStyle(style -> style.withColor(TextColor.fromLegacyFormat(ChatFormatting.GRAY)));
		}

		if (modifierSymbol != null) {
			text.append(Component.literal(modifierSymbol).withStyle(modifierColor));
		}

		return text;
	}

	private static Component makeInputText(String damageStr, boolean isCrit) {
		return makeInputText(damageStr, isCrit, null, null);
	}

	private static MutableComponent splitString(String string) {
		MutableComponent text = Component.empty();
		for (char chr : string.toCharArray()) {
			text.append(Component.literal(String.valueOf(chr)).withColor(CommonColors.BLACK));
		}
		return text;
	}

	@Test
	public void testBasicNoCrit() {
		testCompact(makeInputText("3,825", false), 4, Component.literal("3.825k").withColor(CommonColors.WHITE));
		testCompact(makeInputText("3,825", false), 6, Component.literal("3.825k").withColor(CommonColors.WHITE));
		testCompact(makeInputText("9,995", false), 3, Component.literal("10.0k").withColor(CommonColors.WHITE));
		testCompact(makeInputText("179,481,824,995", false), 6, Component.literal("179.482B").withColor(CommonColors.WHITE));
	}

	@Test
	public void testBasicCrit() {
		testCompact(makeInputText("7,214", true), 4, splitString("✧7.214k✧"));
		testCompact(makeInputText("3,825", true), 4, splitString("✧3.825k✧"));
		testCompact(makeInputText("9,995", true), 3, splitString("✧10.0k✧"));
		testCompact(makeInputText("179,481,824,995", true), 6, splitString("✧179.482B✧"));

	}

	@Test
	public void testModifiersNoCrit() {
		for (var pair : MODIFIERS.entrySet()) {
			testCompact(makeInputText("133,972", false, pair.getKey(), pair.getValue()),
					3,
					Component.literal("134k").withColor(CommonColors.WHITE).append(Component.literal(pair.getKey()).withStyle(pair.getValue())));
			testCompact(makeInputText("99,972", false, pair.getKey(), pair.getValue()),
					9,
					Component.literal("99.972k").withColor(CommonColors.WHITE).append(Component.literal(pair.getKey()).withStyle(pair.getValue())));
			testCompact(makeInputText("179,999,999,995", false, pair.getKey(), pair.getValue()),
					9,
					Component.literal("180.000000B").withColor(CommonColors.WHITE).append(Component.literal(pair.getKey()).withStyle(pair.getValue())));
		}
	}

	@Test
	public void testModifiersCrit() {
		for (var pair : MODIFIERS.entrySet()) {
			testCompact(makeInputText("4,949", true, pair.getKey(), pair.getValue()),
					3,
					splitString("✧4.95k✧").append(Component.literal(pair.getKey()).withStyle(pair.getValue())));
			testCompact(makeInputText("491,529", true, pair.getKey(), pair.getValue()),
					2,
					splitString("✧490k✧").append(Component.literal(pair.getKey()).withStyle(pair.getValue())));
			testCompact(makeInputText("35,518,885,661,733", true, pair.getKey(), pair.getValue()),
					6,
					splitString("✧35.5189T✧").append(Component.literal(pair.getKey()).withStyle(pair.getValue())));
		}
	}

	@Test
	public void testBaseTenDigits() {
		Assertions.assertEquals(1, baseTenDigits(4));
		// mathematically determined to be the funniest & most popular number
		Assertions.assertEquals(2, baseTenDigits(68));
		Assertions.assertEquals(2, baseTenDigits(99));
		Assertions.assertEquals(3, baseTenDigits(100));
		Assertions.assertEquals(3, baseTenDigits(101));
		Assertions.assertEquals(8, baseTenDigits(99_999_999));
		Assertions.assertEquals(9, baseTenDigits(100_000_000));
		Assertions.assertEquals(9, baseTenDigits(100_000_001));

	}

	@Test
	public void testFormatToPrecision() {
		Assertions.assertEquals("103.6", formatToPrecision(103.632d, 4));
		Assertions.assertEquals("103.600", formatToPrecision(103.6001d, 6));
		Assertions.assertEquals("9000.001", formatToPrecision(9000.00149d, 7));
		Assertions.assertEquals("9000", formatToPrecision(9000.00149d, 3));
		Assertions.assertEquals("9000", formatToPrecision(9001d, 3));
		Assertions.assertEquals("9001", formatToPrecision(9001d, 4));
	}

	@Test
	public void testPrettify() {
		Assertions.assertEquals("999", prettifyDamageNumber(999, 9));
		Assertions.assertEquals("100", prettifyDamageNumber(95, 1));
		Assertions.assertEquals("95", prettifyDamageNumber(95, 2));
		Assertions.assertEquals("300", prettifyDamageNumber(253, 1));
		Assertions.assertEquals("1.0k", prettifyDamageNumber(996, 2));
		Assertions.assertEquals("68.68k", prettifyDamageNumber(68_682, 4));
		Assertions.assertEquals("1.00M", prettifyDamageNumber(999_999, 3));
		Assertions.assertEquals("999.999k", prettifyDamageNumber(999_999, 7));
		Assertions.assertEquals("999.999k", prettifyDamageNumber(999_999, 1000));
		Assertions.assertEquals("99.999999M", prettifyDamageNumber(99_999_999, 1000));
		Assertions.assertEquals("100.0000M", prettifyDamageNumber(99_999_999, 7));
	}
}
