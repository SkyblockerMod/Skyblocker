package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Component that consists of 1 or 2 lines of text.
 */
public class PlainTextComponent extends Component {
	private final List<Text> lines = new ArrayList<>();

	public PlainTextComponent(Text txt) {
		lines.add(txt == null ? Text.literal("No data").formatted(Formatting.GRAY) : txt);

		this.width = PAD_L + txtRend.getWidth(txt); // looks off without padding
		this.height = txtRend.fontHeight;
	}

	public PlainTextComponent(Text line1, Text line2) {
		lines.add(line1 == null ? Text.literal("No data").formatted(Formatting.GRAY) : line1);
		lines.add(line2 == null ? Text.literal("No data").formatted(Formatting.GRAY) : line2);

		this.width = PAD_L + Math.max(txtRend.getWidth(line1), txtRend.getWidth(line2));
		this.height = (txtRend.fontHeight * 2) + PAD_S;
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		int yOffset = 0;
		for (Text line : lines) {
			context.drawText(txtRend, line, x + PAD_L, y + yOffset, Colors.WHITE, false);
			yOffset += txtRend.fontHeight + PAD_S;
		}
	}
}
