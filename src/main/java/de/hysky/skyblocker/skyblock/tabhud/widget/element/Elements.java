package de.hysky.skyblocker.skyblock.tabhud.widget.element;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public class Elements {
	public static Element iconTextComponent() {
		return iconTextComponent(null, null);
	}

	public static Element iconTextComponent(@Nullable FlexibleItemStack icon, @Nullable Component text) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.displayIcons) {
			return new IcoTextElement(icon, text);
		} else {
			return new PlainTextElement(text);
		}
	}

	public static Element iconFatTextComponent() {
		return iconFatTextComponent(null, null, null);
	}

	public static Element iconFatTextComponent(@Nullable FlexibleItemStack icon, @Nullable Component line1, @Nullable Component line2) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.displayIcons) {
			return new IcoFatTextElement(icon, line1, line2);
		} else {
			return new PlainTextElement(line1, line2);
		}
	}

	public static Element progressComponent() {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case FANCY -> new ProgressElement();
			case null, default -> iconTextComponent();
		};
	}

	/**
	 * Returns a progress element based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static Element progressComponent(FlexibleItemStack icon, Component description, @Range(from = 0, to = 100) float percent) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case FANCY -> new ProgressElement(icon, description, percent);
			case null, default -> iconTextComponent(icon, appendColon(description).append(Component.literal(percent + "%").withColor(ColorUtils.percentToColor(percent))));
		};
	}

	/**
	 * Returns a progress element based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static Element progressComponent(FlexibleItemStack icon, Component description, @Range(from = 0, to = 100) float percent, int color) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case FANCY -> new ProgressElement(icon, description, percent, color);
			case null, default -> iconTextComponent(icon, appendColon(description).append(Component.literal(percent + "%").withColor(color)));
		};
	}

	/**
	 * Returns a progress element based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static Element progressComponent(FlexibleItemStack icon, Component description, Component bar, @Range(from = 0, to = 100) float percent) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case FANCY -> new ProgressElement(icon, description, bar, percent);
			case null, default -> iconTextComponent(icon, appendColon(description).append(bar.copy().withColor(ColorUtils.percentToColor(percent))));
		};
	}

	/**
	 * Returns a progress element based on the configured style.
	 *
	 * @param percent the percentage from 0 to 100
	 */
	public static Element progressComponent(FlexibleItemStack icon, Component description, Component bar, @Range(from = 0, to = 100) float percent, int color) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.style) {
			case FANCY -> new ProgressElement(icon, description, bar, percent, color);
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
