package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Component that draws a line with optional text across a widget box.
 * Can be used to separate information visible in a widget into different categories.
 */
public class SeparatorComponent extends Component {
	private final Text text;
	private final int textWidth;

	public SeparatorComponent(Text text) {
		this.text = text;
		this.textWidth = text != null ? txtRend.getWidth(text) : 0;
		this.height = txtRend.fontHeight;
		this.width = textWidth + 4;
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		int parentWidth = this.getParent().getWidth();
		if (text != null && !text.equals(Text.of(""))) {
			context.fill(x - (ComponentBasedWidget.BORDER_SZE_E / 2), y + this.height / 2, x + 2, y + this.height / 2 + 1, 0xff55ffff);
			context.drawText(txtRend, text, x + 4, y, 0xff55ffff, false);
			context.fill(x + textWidth + 2 + 4, y + this.height / 2, x + parentWidth - ComponentBasedWidget.BORDER_SZE_E - ComponentBasedWidget.BORDER_SZE_W + 2, y + this.height / 2 + 1, 0xff55ffff);
		} else {
			context.fill(x - 2, y + this.height / 2, x + parentWidth - ComponentBasedWidget.BORDER_SZE_E - ComponentBasedWidget.BORDER_SZE_W + 2, y + this.height / 2 + 1, 0xff55ffff);
		}
	}
}
