package de.hysky.skyblocker.skyblock.tabhud.widget.element;

import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Element that draws a line with optional text across a widget box.
 * Can be used to separate information visible in a widget into different categories.
 */
public class SeparatorElement extends Element {
	private final @Nullable Component text;
	private final int textWidth;

	public SeparatorElement(@Nullable Component text) {
		this.text = text;
		this.textWidth = text != null ? txtRend.width(text) : 0;
		this.height = txtRend.lineHeight;
		this.width = textWidth + 4;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y) {
		int parentWidth = this.getParent().getWidth();
		if (text != null && !text.equals(Component.nullToEmpty(""))) {
			graphics.fill(x - (ElementBasedWidget.BORDER_SZE_E / 2), y + this.height / 2, x + 2, y + this.height / 2 + 1, 0xFF55FFFF);
			graphics.text(txtRend, text, x + 4, y, 0xFF55FFFF, false);
			graphics.fill(x + textWidth + 2 + 4, y + this.height / 2, x + parentWidth - ElementBasedWidget.BORDER_SZE_E - ElementBasedWidget.BORDER_SZE_W + 2, y + this.height / 2 + 1, 0xFF55FFFF);
		} else {
			graphics.fill(x - 2, y + this.height / 2, x + parentWidth - ElementBasedWidget.BORDER_SZE_E - ElementBasedWidget.BORDER_SZE_W + 2, y + this.height / 2 + 1, 0xFF55FFFF);
		}
	}
}
