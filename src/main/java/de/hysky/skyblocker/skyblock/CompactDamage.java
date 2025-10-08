package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.OkLabColor;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CompactDamage {
	// Capture Group 1: Crit, Overload Crit
	// Capture Group 2: Ring of Love, Tara Armor, End Stone Sword
	private static final Pattern DAMAGE_PATTERN = Pattern.compile("([✧✯]?)[\\d,]+[✧✯]?([❤+⚔]?)");

	private CompactDamage() {
	}

	public static void compactDamage(ArmorStandEntity entity) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.compactDamage.enabled) return;
		if (!entity.isInvisible() || !entity.hasCustomName() || !entity.isCustomNameVisible()) return;
		Text customName = entity.getCustomName();
		if (customName == null) return;

		String customNameStringified = customName.getString();
		Matcher matcher = DAMAGE_PATTERN.matcher(customNameStringified);
		if (!matcher.matches()) return;
		List<Text> siblings = customName.getSiblings();
		if (siblings.isEmpty()) return;

		MutableText prettierCustomName;
		if (siblings.size() == 1) { // Plain non-crit, no modifier damage
			Text text = siblings.getFirst();
			String dmg = text.getString().replace(",", "");
			if (!NumberUtils.isParsable(dmg)) return; //Sanity check
			String prettifiedDmg = prettifyDamageNumber(Long.parseLong(dmg));
			int color;
			if (text.getStyle().getColor() != null) {
				if (text.getStyle().getColor() == TextColor.fromFormatting(Formatting.GRAY)) {
					color = SkyblockerConfigManager.get().uiAndVisuals.compactDamage.normalDamageColor.getRGB() & 0x00FFFFFF;
				} else color = text.getStyle().getColor().getRgb();
			} else color = SkyblockerConfigManager.get().uiAndVisuals.compactDamage.normalDamageColor.getRGB() & 0x00FFFFFF;
			prettierCustomName = Text.literal(prettifiedDmg).setStyle(customName.getStyle()).withColor(color);
		} else { // Multi-styled damage
			boolean isCrit = !matcher.group(1).isEmpty();
			int symbolsToRemoveFront = isCrit ? 1 : 0;
			int symbolsToRemoveEnd = symbolsToRemoveFront;
			if (!matcher.group(2).isEmpty()) symbolsToRemoveEnd += 1;

			// If there are crit symbols: they are the first and last* sibling.
			// * There can be other additional siblings added after the second crit sibling.
			String dmg = siblings.subList(symbolsToRemoveFront, siblings.size() - symbolsToRemoveEnd)
			                     .stream()
			                     .map(Text::getString)
			                     .reduce("", String::concat) //Concatenate all the siblings to get the dmg number
			                     .replace(",", "");

			if (!NumberUtils.isParsable(dmg)) return; //Sanity check
			String dmgSymbol = matcher.group(1);
			String prettifiedDmg = dmgSymbol + prettifyDamageNumber(Long.parseLong(dmg)) + dmgSymbol;
			prettierCustomName = Text.empty();
			int length = prettifiedDmg.length();
			for (int i = 0; i < length; i++) {
				prettierCustomName.append(Text.literal(prettifiedDmg.substring(i, i + 1)).withColor(
						OkLabColor.interpolate(
								SkyblockerConfigManager.get().uiAndVisuals.compactDamage.critDamageGradientStart.getRGB() & 0x00FFFFFF,
								SkyblockerConfigManager.get().uiAndVisuals.compactDamage.critDamageGradientEnd.getRGB() & 0x00FFFFFF,
								i / (length - 1.0f)
						)
				));
			}

			// Readd the additional symbol, if present
			if (!matcher.group(2).isEmpty()) prettierCustomName.append(Text.literal(matcher.group(2)).setStyle(siblings.getLast().getStyle()));
			prettierCustomName.setStyle(customName.getStyle());
		}

		entity.setCustomName(prettierCustomName);
	}

	private static String prettifyDamageNumber(long damage) {
		if (damage < 1_000) return String.valueOf(damage);
		if (damage < 1_000_000) return format(damage / 1_000.0) + "k";
		if (damage < 1_000_000_000) return format(damage / 1_000_000.0) + "M";
		if (damage < 1_000_000_000_000L) return format(damage / 1_000_000_000.0) + "B";
		return format(damage / 1_000_000_000_000.0) + "T"; //This will probably never be reached
	}

	private static String format(double number) {
		return ("%." + SkyblockerConfigManager.get().uiAndVisuals.compactDamage.precision + "f").formatted(number);
	}
}
