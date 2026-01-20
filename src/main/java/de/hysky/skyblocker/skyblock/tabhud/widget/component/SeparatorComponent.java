package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Component that draws a line with optional text across a widget box.
 * Can be used to separate information visible in a widget into different categories.
 */
public class SeparatorComponent extends Component {
	private final net.minecraft.network.chat.Component text;
	private final int textWidth;

	public SeparatorComponent(net.minecraft.network.chat.Component text) {
		this.text = text;
		this.textWidth = text != null ? txtRend.width(text) : 0;
		this.height = txtRend.lineHeight;
		this.width = textWidth + 4;
	}

	@Override
	public void render(GuiGraphics context, int x, int y) {
		int parentWidth = this.getParent().getWidth();
		if (text != null && !text.equals(net.minecraft.network.chat.Component.nullToEmpty(""))) {
			context.fill(x - (ComponentBasedWidget.BORDER_SZE_E / 2), y + this.height / 2, x + 2, y + this.height / 2 + 1, 0xFF55FFFF);
			context.drawString(txtRend, text, x + 4, y, 0xFF55FFFF, false);
			context.fill(x + textWidth + 2 + 4, y + this.height / 2, x + parentWidth - ComponentBasedWidget.BORDER_SZE_E - ComponentBasedWidget.BORDER_SZE_W + 2, y + this.height / 2 + 1, 0xFF55FFFF);
		} else {
			context.fill(x - 2, y + this.height / 2, x + parentWidth - ComponentBasedWidget.BORDER_SZE_E - ComponentBasedWidget.BORDER_SZE_W + 2, y + this.height / 2 + 1, 0xFF55FFFF);
		}
	}
}
