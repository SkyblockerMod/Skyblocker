package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.OkLabColor;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.VisibleForTesting;


public class CompactDamage {
	// Capture Group 1: Crit, Overload Crit
	// Capture Group 2: Ring of Love, Tara Armor, End Stone Sword, Voodoo Doll, Guardian Pet
	private static final Pattern DAMAGE_PATTERN = Pattern.compile("([✧✯]?)[\\d,]+[✧✯]?([❤+⚔☄♞]?)");

	private CompactDamage() {
	}

	public static void compactDamage(ArmorStand entity) {
		UIAndVisualsConfig.CompactDamage config = SkyblockerConfigManager.get().uiAndVisuals.compactDamage;

		if (!config.enabled) return;
		if (!entity.isInvisible() || !entity.hasCustomName() || !entity.isCustomNameVisible()) return;

		Component customName = entity.getCustomName();
		if (customName == null) return;

		String customNameString = customName.getString();
		Matcher matcher = DAMAGE_PATTERN.matcher(customNameString);
		if (!matcher.matches()) return;
		List<Component> siblings = customName.getSiblings();
		if (siblings.isEmpty()) return;

		final boolean isCrit = !matcher.group(1).isEmpty();
		final String dmg = customNameString.replaceAll("\\D", "");
		if (!NumberUtils.isParsable(dmg)) return; //Sanity check
		final TextColor textColor = siblings.getFirst().getStyle().getColor();

		MutableComponent prettierCustomName = Component.empty();

		String prettifiedDmg = prettifyDamageNumber(Long.parseLong(dmg), config.maxPrecision);

		if (isCrit) {
			String dmgSymbol = matcher.group(1);
			prettifiedDmg = dmgSymbol + prettifiedDmg + dmgSymbol;
			int length = prettifiedDmg.length();
			for (int i = 0; i < length; i++) {
				prettierCustomName.append(Component.literal(prettifiedDmg.substring(i, i + 1)).withColor(
						OkLabColor.interpolate(
								config.critDamageGradientStart.getRGB() & 0x00_FF_FF_FF,
								config.critDamageGradientEnd.getRGB() & 0x00_FF_FF_FF,
								i / (length - 1.0f)
						)
				));
			}
			prettierCustomName.setStyle(customName.getStyle());
		} else {
			int color;
			if (textColor == null || textColor == TextColor.fromLegacyFormat(ChatFormatting.GRAY)) {
				color = config.normalDamageColor.getRGB() & 0x00_FF_FF_FF;
			} else {
				color = textColor.getValue();
			}
			prettierCustomName = Component.literal(prettifiedDmg).setStyle(customName.getStyle()).withColor(color);
		}
		// Add the additional symbol back, if present
		if (!matcher.group(2).isEmpty()) prettierCustomName.append(Component.literal(matcher.group(2)).setStyle(siblings.getLast().getStyle()));
		entity.skyblocker$setCustomName(prettierCustomName);
	}

	/// We want precision to signify the *number of significant digits*, not the number of digits after the decimal.
	/// For example:
	/// 123,456,789 (precision 3) -> 123M
	/// 12,345 (precision 4) -> 1.234k
	@VisibleForTesting
	static String prettifyDamageNumber(final long damage, final int maxPrecision) {
		long targetDamage = damage;
		int targetPrecision = maxPrecision;
		// First, round `damage` to `precision` places
		// Otherwise inputs like `999,999, 3` will display as `1000k` instead of `1.00m`

		int usedPrecision = baseTenDigits(targetDamage);
		if (usedPrecision > targetPrecision) {
			double powerToRoundTo = powersOfTen[usedPrecision - maxPrecision];
			targetDamage = (long) (Math.round((double) targetDamage / powerToRoundTo) * powerToRoundTo);
		} else if (targetPrecision > usedPrecision) {
			// We don't want to ever display more decimal points than needed. For example,
			// 999_999 with precision 7 should still display as 999.999k, not 999.9990k
			targetPrecision = usedPrecision;
		}

		if (targetDamage < 1_000L) return String.valueOf(targetDamage);
		if (targetDamage < 1_000_000L) return formatToPrecision(targetDamage / 1_000.0, targetPrecision) + "k";
		if (targetDamage < 1_000_000_000L) return formatToPrecision(targetDamage / 1_000_000.0, targetPrecision) + "M";
		if (targetDamage < 1_000_000_000_000L) return formatToPrecision(targetDamage / 1_000_000_000.0, targetPrecision) + "B";
		if (targetDamage < 1_000_000_000_000_000L) return formatToPrecision(targetDamage / 1_000_000_000_000.0, targetPrecision) + "T";
		// surely this will never happen :clueless:
		return formatToPrecision(targetDamage / 1_000_000_000_000_000.0, targetPrecision) + "Q";
	}

	@VisibleForTesting
	static String formatToPrecision(double number, int precision) {
		int usedPrecision = baseTenDigits((int) number);
		int remainingPrecision = precision - usedPrecision;
		if (remainingPrecision <= 0) {
			long powerToRoundTo = powersOfTen[usedPrecision - precision];
			return String.valueOf((Math.round(number / powerToRoundTo) * powerToRoundTo));
		}
		return ("%." + remainingPrecision + "f").formatted(number);
	}

	private static int baseTwoDigits(long x) {
		return 64 - Long.numberOfLeadingZeros(x);
	}

	/// A lowball guess for the number of base 10 digits, given the number of base 2 digits
	/// Off by 1 at most
	/// The guess for an n-digit base 2 number is the correct answer for the smallest number with
	/// n digits in base 2.
	private static final int[] guesses = new int[]{
			0,  1,  1,  1,  1,  2,  2,  2,
			3,  3,  3,  4,  4,  4,  4,  5,
			5,  5,  6,  6,  6,  7,  7,  7,
			7,  8,  8,  8,  9,  9,  9,  10,
			10, 10, 10, 11, 11, 11, 12, 12,
			12, 13, 13, 13, 13, 14, 14, 14,
			15, 15, 15, 16, 16, 16, 16, 17,
			17, 17, 18, 18, 18, 19, 19, 19,
	};

	private static final long[] powersOfTen = new long[]{
			1L,                   10L,                100L,
			1000L,                10000L,             100000L,
			1000000L,             10000000L,          100000000L,
			1000000000L,          10000000000L,       100000000000L,
			1000000000000L,       10000000000000L,    100000000000000L,
			1000000000000000L,    10000000000000000L, 100000000000000000L,
			1000000000000000000L,
	};

	/// Equivalent to floor(log10(x)) + 1
	/// https://stackoverflow.com/a/25934909
	@VisibleForTesting
	static int baseTenDigits(long x) {
		int guess = guesses[baseTwoDigits(x)];
		return guess + ((x >= powersOfTen[guess]) ? 1 : 0);
	}
}
