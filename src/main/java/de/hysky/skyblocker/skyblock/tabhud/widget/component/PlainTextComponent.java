package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

/**
 * Component that consists of a line of text.
 */
public class PlainTextComponent extends Component {

	private Text text;

	public PlainTextComponent(Text txt) {
		this.text = txt;

		if (txt == null) {
			this.text = Text.literal("No data").formatted(Formatting.GRAY);
		}

		this.width = PAD_S + txtRend.getWidth(this.text); // looks off without padding
		this.height = txtRend.fontHeight;
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		context.drawText(txtRend, text, x + PAD_S, y, Colors.WHITE, false);
	}

}
