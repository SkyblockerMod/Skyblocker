package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Component that renders a line of text surrounded by a colored border.
 */
public class BoxedTextComponent extends PlainTextComponent {
	private final int borderColor;

	public BoxedTextComponent(Text text, int borderColor) {
		super(text);
		this.borderColor = 0xff000000 | borderColor;
		this.width += 4;
		this.height += 8;
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		context.drawBorder(x, y, this.width, this.height, this.borderColor);
		super.render(context, x + 2, y + 4);
	}
}
