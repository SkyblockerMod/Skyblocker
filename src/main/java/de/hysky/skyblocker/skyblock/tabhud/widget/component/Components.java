package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class Components {
	public static Component progressComponent(ItemStack icon, Text description, float percent) {
		return SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isFancy() ? new ProgressComponent(icon, description, percent) : new PlainTextComponent(appendColon(description).append(Text.literal(percent + "%").withColor(ColorUtils.percentToColor(percent))));
	}

	public static Component progressComponent(ItemStack icon, Text description, float percent, int color) {
		return SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isFancy() ? new ProgressComponent(icon, description, percent, color) : new PlainTextComponent(appendColon(description).append(Text.literal(percent + "%").withColor(color)));
	}

	public static Component progressComponent(ItemStack icon, Text description, Text bar, float percent) {
		return SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isFancy() ? new ProgressComponent(icon, description, bar, percent) : new PlainTextComponent(appendColon(description).append(bar.copy().withColor(ColorUtils.percentToColor(percent))));
	}

	public static Component progressComponent(ItemStack icon, Text description, Text bar, float percent, int color) {
		return SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isFancy() ? new ProgressComponent(icon, description, bar, percent, color) : new PlainTextComponent(appendColon(description).append(bar.copy().withColor(color)));
	}

	/**
	 * Returns a copy of the given text ending with ": ".
	 */
	private static MutableText appendColon(Text text) {
		String string = text.getString();
		if (string.endsWith(": ")) {
			return text.copy();
		} else if (string.endsWith(":")) {
			return text.copy().append(" ");
		} else {
			return text.copy().append(": ");
		}
	}
}
