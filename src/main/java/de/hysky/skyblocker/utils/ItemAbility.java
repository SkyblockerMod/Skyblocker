package de.hysky.skyblocker.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ItemAbility(String name, Activation activation, OptionalInt manaCost, OptionalInt soulflowCost, OptionalInt cooldown) {
	private static final Pattern ABILITY_NAME_PATTERN = Pattern.compile("Ability: (.+) " + "(" + String.join("|", Arrays.stream(Activation.values()).map(Activation::toString).toArray(String[]::new)) + ")");
	private static final Pattern MANA_COST_PATTERN = Pattern.compile("Mana Cost: (\\d+)");
	private static final Pattern SOULFLOW_COST_PATTERN = Pattern.compile("Soulflow Cost: (\\d+)");
	private static final Pattern COOLDOWN_PATTERN = Pattern.compile("Cooldown: ([0-9]+\\.?[0-9]*)s");
	public static List<ItemAbility> getAbilities(ItemStack stack) {
		List<String> strings = stack.skyblocker$getLoreStrings();
		List<ItemAbility> abilities = new ArrayList<>(2); // items rarely have more than 2
		String name = null;
		int manaCost = -1;
		int soulflowCost = -1;
		int cooldown = -1;
		Activation activation = null;
		for (String string : strings) {
			string = Formatting.strip(string).trim();
			Matcher matcher = ABILITY_NAME_PATTERN.matcher(string);
			if (matcher.matches()) {
				// add previous ability to list
				if (name != null) {
					abilities.add(new ItemAbility(name, activation, positiveOnly(manaCost), positiveOnly(soulflowCost), positiveOnly(cooldown)));
				}
				// reset values
				name = matcher.group(1);
				manaCost = -1;
				soulflowCost = -1;
				cooldown = -1;
				activation = Activation.of(matcher.group(2));
			}
			if (name == null) continue;
			// Mana
			if (manaCost < 0) {
				matcher = MANA_COST_PATTERN.matcher(string);
				if (matcher.matches()) {
					manaCost = NumberUtils.toInt(matcher.group(1), -1);
					continue;
				}
			}
			// Soulflow
			if (soulflowCost < 0) {
				matcher = SOULFLOW_COST_PATTERN.matcher(string);
				if (matcher.matches()) {
					soulflowCost = NumberUtils.toInt(matcher.group(1), -1);
					continue;
				}
			}
			// Cooldown
			if (cooldown < 0) {
				matcher = COOLDOWN_PATTERN.matcher(string);
				if (matcher.matches()) {
					cooldown = (int) (NumberUtils.toFloat(matcher.group(1), -1) * 20); // multiply by 20 to convert to ticks.
					continue;
				}
			}

		}
		return abilities;
	}

	private static OptionalInt positiveOnly(int value) {
		return value < 0 ? OptionalInt.empty() : OptionalInt.of(value);
	}

	public enum Activation {
		RIGHT_CLICK,
		LEFT_CLICK,
		SNEAK_RIGHT_CLICK,
		SNEAK_LEFT_CLICK;



		@Override
		public String toString() {
			return name().replace('_', ' ');
		}

		public static Activation of(String name) {
			for (Activation value : Activation.values()) {
				if (value.toString().equals(name)) return value;
			}
			return RIGHT_CLICK;
		}
	}
}
