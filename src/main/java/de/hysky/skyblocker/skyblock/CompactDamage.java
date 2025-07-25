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
import java.util.regex.Pattern;


public class CompactDamage {
	private static final Pattern DAMAGE_PATTERN = Pattern.compile("[✧✯]?[\\d,]+[✧✯]?❤?");

	private CompactDamage() {
	}

	public static void compactDamage(ArmorStandEntity entity) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.compactDamage.enabled) return;
		if (!entity.isInvisible() || !entity.hasCustomName() || !entity.isCustomNameVisible()) return;
		Text customName = entity.getCustomName();
		String customNameStringified = customName.getString();
		if (!DAMAGE_PATTERN.matcher(customNameStringified).matches()) return;
		List<Text> siblings = customName.getSiblings();
		if (siblings.isEmpty()) return;

		MutableText prettierCustomName;
		if (siblings.size() == 1) { //Non-crit damage
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
			prettierCustomName = Text.literal("").append(Text.literal(prettifiedDmg).setStyle(customName.getStyle()).withColor(color));
		} else { //Crit damage
			boolean wasDoubled = customNameStringified.contains("❤"); //Ring of love ability adds a heart to the end of the damage string
			int entriesToRemove = wasDoubled ? 2 : 1;

			String dmg = siblings.subList(1, siblings.size() - entriesToRemove) //First and last sibling are the crit symbols and maybe heart
					.stream()
					.map(Text::getString)
					.reduce("", String::concat) //Concatenate all the siblings to get the dmg number
					.replace(",", "");

			if (!NumberUtils.isParsable(dmg)) return; //Sanity check
			String dmgSymbol = customNameStringified.charAt(0) != '✯' ? "✧" : "✯"; //Mega Crit ability from the Overload enchantment
			String prettifiedDmg = dmgSymbol + prettifyDamageNumber(Long.parseLong(dmg)) + dmgSymbol;
			prettierCustomName = Text.literal("");
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

			if (wasDoubled) prettierCustomName.append(Text.literal("❤").formatted(Formatting.LIGHT_PURPLE));

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
