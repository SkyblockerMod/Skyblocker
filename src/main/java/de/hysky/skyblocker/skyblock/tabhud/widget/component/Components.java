package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Range;

public class Components {
	public static Component iconTextComponent() {
		return iconTextComponent(null, null);
	}

	public static Component iconTextComponent(ItemStack icon, Component text) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.displayIcons) {
			return new IcoTextComponent(icon, text);
		} else {
			return new PlainTextComponent(text);
		}
	}

	public static de.hysky.skyblocker.skyblock.tabhud.widget.component.Component iconFatTextComponent() {
		return iconFatTextComponent(null, null, null);
	}

	public static de.hysky.skyblocker.skyblock.tabhud.widget.component.Component iconFatTextComponent(ItemStack icon, Component line1, Component line2) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.displayIcons) {
			return new IcoFatTextComponent(icon, line1, line2);
		} else {
			return new PlainTextComponent(line1, line2);
		}
	}

	public static de.hysky.skyblocker.skyblock.tabhud.widget.component.Component progressComponent() {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case FANCY -> new ProgressComponent();
			case null, default -> iconTextComponent();
		};
	}

	/**
	 * Returns a progress component based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static de.hysky.skyblocker.skyblock.tabhud.widget.component.Component progressComponent(ItemStack icon, Component description, @Range(from = 0, to = 100) float percent) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case FANCY -> new ProgressComponent(icon, description, percent);
			case null, default -> iconTextComponent(icon, appendColon(description).append(Component.literal(percent + "%").withColor(ColorUtils.percentToColor(percent))));
		};
	}

	/**
	 * Returns a progress component based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static de.hysky.skyblocker.skyblock.tabhud.widget.component.Component progressComponent(ItemStack icon, Component description, @Range(from = 0, to = 100) float percent, int color) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case FANCY -> new ProgressComponent(icon, description, percent, color);
			case null, default -> iconTextComponent(icon, appendColon(description).append(Component.literal(percent + "%").withColor(color)));
		};
	}

	/**
	 * Returns a progress component based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static de.hysky.skyblocker.skyblock.tabhud.widget.component.Component progressComponent(ItemStack icon, Component description, Component bar, @Range(from = 0, to = 100) float percent) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case FANCY -> new ProgressComponent(icon, description, bar, percent);
			case null, default -> iconTextComponent(icon, appendColon(description).append(bar.copy().withColor(ColorUtils.percentToColor(percent))));
		};
	}

	/**
	 * Returns a progress component based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static de.hysky.skyblocker.skyblock.tabhud.widget.component.Component progressComponent(ItemStack icon, Component description, Component bar, @Range(from = 0, to = 100) float percent, int color) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case FANCY -> new ProgressComponent(icon, description, bar, percent, color);
			case null, default -> iconTextComponent(icon, appendColon(description).append(bar.copy().withColor(color)));
		};
	}

	/**
	 * Returns a copy of the given text ending with ": ".
	 */
	private static MutableComponent appendColon(Component text) {
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
