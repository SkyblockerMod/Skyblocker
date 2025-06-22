package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Component that consists of an icon and two lines of text
 */
public class SeparatorComponent extends Component {

	private final Text text;
	private final int textWidth;
	private final int minimalWidth;

	public SeparatorComponent(Text text) {
		this(text, 0);
	}

	public SeparatorComponent(Text text, int minimalWidth) {
		this.text = text;
		this.textWidth = txtRend.getWidth(text);
		this.height = txtRend.fontHeight;
		this.minimalWidth = minimalWidth;
		this.width = Math.max(this.minimalWidth, textWidth + 4);
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		if(text != null && !text.equals(Text.of(""))) {
			context.fill(x - 2, y + this.height / 2, x + 2, y + this.height / 2 + 1, 0xff55ffff);
			context.drawText(txtRend, text, x + 4, y, 0xff55ffff, false);
			context.fill(x + 6 + textWidth, y + this.height / 2, x + width + 10, y + this.height / 2 +1, 0xff55ffff);
		} else {
			context.fill(x - 2, y + this.height / 2, x + width, y + this.height / 2 +1, 0xff55ffff);
		}

	}

}
