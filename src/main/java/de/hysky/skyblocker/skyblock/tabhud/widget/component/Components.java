package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Range;

public class Components {
	/**
	 * Returns a progress component based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static Component progressComponent(ItemStack icon, Text description, @Range(from = 0, to = 100) float percent) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case MINIMAL, SIMPLE -> new PlainTextComponent(appendColon(description).append(Text.literal(percent + "%").withColor(ColorUtils.percentToColor(percent))));
			case CLASSIC -> new IcoTextComponent(icon, appendColon(description).append(Text.literal(percent + "%").withColor(ColorUtils.percentToColor(percent))));
			case FANCY -> new ProgressComponent(icon, description, percent);
		};
	}

	/**
	 * Returns a progress component based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static Component progressComponent(ItemStack icon, Text description, @Range(from = 0, to = 100) float percent, int color) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case MINIMAL, SIMPLE -> new PlainTextComponent(appendColon(description).append(Text.literal(percent + "%").withColor(color)));
			case CLASSIC -> new IcoTextComponent(icon, appendColon(description).append(Text.literal(percent + "%").withColor(color)));
			case FANCY -> new ProgressComponent(icon, description, percent, color);
		};
	}

	/**
	 * Returns a progress component based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static Component progressComponent(ItemStack icon, Text description, Text bar, @Range(from = 0, to = 100) float percent) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case MINIMAL, SIMPLE -> new PlainTextComponent(appendColon(description).append(bar.copy().withColor(ColorUtils.percentToColor(percent))));
			case CLASSIC -> new IcoTextComponent(icon, appendColon(description).append(bar.copy().withColor(ColorUtils.percentToColor(percent))));
			case FANCY -> new ProgressComponent(icon, description, bar, percent);
		};
	}

	/**
	 * Returns a progress component based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static Component progressComponent(ItemStack icon, Text description, Text bar, @Range(from = 0, to = 100) float percent, int color) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case MINIMAL, SIMPLE -> new PlainTextComponent(appendColon(description).append(bar.copy().withColor(color)));
			case CLASSIC -> new IcoTextComponent(icon, appendColon(description).append(bar.copy().withColor(color)));
			case FANCY -> new ProgressComponent(icon, description, bar, percent, color);
		};
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
