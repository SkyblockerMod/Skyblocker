package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

/**
 * Component that consists of an icon and two lines of text
 */
public class IcoFatTextComponent extends Component {

	private static final int ICO_OFFS = 1;

	private ItemStack ico;
	private Text line1, line2;

	public IcoFatTextComponent(ItemStack ico, Text l1, Text l2) {
		this.ico = (ico == null) ? Ico.BARRIER : ico;
		this.line1 = l1;
		this.line2 = l2;

		if (l1 == null || l2 == null) {
			this.ico = Ico.BARRIER;
			this.line1 = Text.literal("No data").formatted(Formatting.GRAY);
			this.line2 = Text.literal("No data").formatted(Formatting.GRAY);
		}

		this.width = ICO_DIM + PAD_L + Math.max(txtRend.getWidth(this.line1), txtRend.getWidth(this.line2));
		this.height = txtRend.fontHeight + PAD_S + txtRend.fontHeight;
	}

	public IcoFatTextComponent() {
		this(null, null, null);
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		context.drawItem(ico, x, y + ICO_OFFS);
		context.drawText(txtRend, line1, x + ICO_DIM + PAD_L, y, Colors.WHITE, false);
		context.drawText(txtRend, line2, x + ICO_DIM + PAD_L, y + txtRend.fontHeight + PAD_S, Colors.WHITE, false);
	}

}
