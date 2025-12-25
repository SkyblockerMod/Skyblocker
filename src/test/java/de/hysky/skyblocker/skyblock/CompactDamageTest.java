package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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

		// Make crits cyan
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

	private static Component getCompactText(ArmorStand entity) {
		CompactDamage.compactDamage(entity);
		return entity.getCustomName();
	}

	private static void testCompact(Component inputText, Component expectedText) {
		Component outputText = getCompactText(createEntityWithName(inputText));
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
		testCompact(makeInputText("3,825", false), Component.literal("3.8k").withColor(CommonColors.WHITE));
	}

	@Test
	public void testBasicCrit() {
		testCompact(makeInputText("7,214", true), splitString("✧7.2k✧"));
	}

	@Test
	public void testModifiersNoCrit() {
		for (var pair : MODIFIERS.entrySet()) {
			testCompact(makeInputText("133,972", false, pair.getKey(), pair.getValue()),
					Component.literal("134.0k").withColor(CommonColors.WHITE).append(Component.literal(pair.getKey()).withStyle(pair.getValue())));
		}
	}

	@Test
	public void testModifiersCrit() {
		for (var pair : MODIFIERS.entrySet()) {
			testCompact(makeInputText("4,949", true, pair.getKey(), pair.getValue()),
					splitString("✧4.9k✧").append(Component.literal(pair.getKey()).withStyle(pair.getValue())));
		}
	}
}
