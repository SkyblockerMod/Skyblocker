package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CompactDamageTest {
	Map<String, Formatting> MODIFIERS = Util.make(new HashMap<>(), map -> {
		map.put("❤", Formatting.LIGHT_PURPLE);
		map.put("+", Formatting.YELLOW);
		map.put("⚔", Formatting.GOLD);
	});

	@BeforeAll
	public static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();

		// Make crits cyan
		SkyblockerConfigManager.get().uiAndVisuals.compactDamage.critDamageGradientStart = new Color(Colors.CYAN);
		SkyblockerConfigManager.get().uiAndVisuals.compactDamage.critDamageGradientEnd = new Color(Colors.CYAN);
	}

	private static ArmorStandEntity createEntityWithName(Text text) {
		ArmorStandEntity entity = new ArmorStandEntity(EntityType.ARMOR_STAND, null);
		entity.setInvisible(true);
		entity.setCustomNameVisible(true);
		entity.setCustomName(text);
		return entity;
	}

	private static Text getCompactText(ArmorStandEntity entity) {
		CompactDamage.compactDamage(entity);
		return entity.getCustomName();
	}

	private static void testCompact(Text inputText, Text expectedText) {
		Text outputText = getCompactText(createEntityWithName(inputText));
		Assertions.assertEquals(expectedText, outputText);
	}

	/**
	 * Create damage text similar to how Hypixel does it
	 */
	private static Text makeInputText(String damageStr, Boolean isCrit, String modifierSymbol, Formatting modifierColor) {
		MutableText text = Text.empty();
		if (isCrit) {
			damageStr = "✧%s✧".formatted(damageStr);
			for (char chr : damageStr.toCharArray()) {
				// Hypixel would add color here, but since it will be discarded, there is no need for us to add it.
				text.append(String.valueOf(chr));
			}
		} else {
			text.append(damageStr).styled(style -> style.withColor(TextColor.fromFormatting(Formatting.GRAY)));
		}

		if (modifierSymbol != null) {
			text.append(Text.literal(modifierSymbol).formatted(modifierColor));
		}

		return text;
	}

	private static Text makeInputText(String damageStr, Boolean isCrit) {
		return makeInputText(damageStr, isCrit, null, null);
	}

	private static MutableText splitString(String string) {
		MutableText text = Text.empty();
		for (char chr : string.toCharArray()) {
			// do not ask why it is -0x100...
			text.append(Text.literal(String.valueOf(chr)).withColor(Colors.CYAN - 0x100));
		}
		return text;
	}

	@Test
	public void testBasicNoCrit() {
		testCompact(makeInputText("3,825", false), Text.literal("3.8k").withColor(Colors.WHITE));
	}

	@Test
	public void testBasicCrit() {
		testCompact(makeInputText("7,214", true), splitString("✧7.2k✧"));
	}

	@Test
	public void testModifiersNoCrit() {
		for (var pair : MODIFIERS.entrySet()) {
			testCompact(makeInputText("133,972", false, pair.getKey(), pair.getValue()),
					Text.literal("134.0k").withColor(Colors.WHITE).append(Text.literal(pair.getKey()).formatted(pair.getValue())));
		}
	}

	@Test
	public void testModifiersCrit() {
		for (var pair : MODIFIERS.entrySet()) {
			testCompact(makeInputText("4,949", true, pair.getKey(), pair.getValue()),
					splitString("✧4.9k✧").append(Text.literal(pair.getKey()).formatted(pair.getValue())));
		}
	}
}
