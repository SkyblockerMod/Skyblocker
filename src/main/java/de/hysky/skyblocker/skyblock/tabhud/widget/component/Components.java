package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.ColorUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Range;

public class Components {
	public static Component iconTextComponent(ItemStack icon, Text text) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.displayIcons) {
			return new IcoTextComponent(icon, text);
		} else {
			return new PlainTextComponent(text);
		}
	}

	public static Component iconTextComponent() {
		return iconTextComponent(null, null);
	}

	public static Component iconFatTextComponent(ItemStack icon, Text line1, Text line2) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.displayIcons) {
			return new IcoFatTextComponent(icon, line1, line2);
		} else {
			return new PlainTextComponent(line1, line2);
		}
	}

	public static Component iconFatTextComponent() {
		return iconFatTextComponent(null, null, null);
	}

	public static Component progressComponent(ItemStack icon, Text description, Text bar, @Range(from = 0, to = 100) float percent, int color) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style == UIAndVisualsConfig.TabHudStyle.FANCY) {
			return new ProgressComponent(icon, description, bar, percent, color);
		} else {
			return iconTextComponent(icon, appendColon(description).append(bar.copy().withColor(color)));
		}
	}

	public static Component progressComponent(ItemStack icon, Text description, Text bar, @Range(from = 0, to = 100) float percent) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style == UIAndVisualsConfig.TabHudStyle.FANCY) {
			return new ProgressComponent(icon, description, bar, percent);
		} else {
			return iconTextComponent(icon, appendColon(description).append(bar.copy().withColor(ColorUtils.percentToColor(percent))));
		}
	}

	public static Component progressComponent(ItemStack icon, Text description, @Range(from = 0, to = 100) float percent, int color) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style == UIAndVisualsConfig.TabHudStyle.FANCY) {
			return new ProgressComponent(icon, description, percent, color);
		} else {
			return iconTextComponent(icon, appendColon(description).append(Text.literal(percent + "%").withColor(color)));
		}
	}

	public static Component progressComponent(ItemStack icon, Text description, @Range(from = 0, to = 100) float percent) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style == UIAndVisualsConfig.TabHudStyle.FANCY) {
			return new ProgressComponent(icon, description, percent);
		} else {
			return iconTextComponent(icon, appendColon(description).append(Text.literal(percent + "%").withColor(ColorUtils.percentToColor(percent))));
		}
	}

	public static Component progressComponent() {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style == UIAndVisualsConfig.TabHudStyle.FANCY) {
			return new ProgressComponent();
		} else {
			return iconTextComponent(null, null);
		}
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
