package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

/**
 * Component that consists of an icon and a line of text.
 */
public class IcoTextComponent extends Component {

	private ItemStack ico;
	private Text text;

	public IcoTextComponent(ItemStack ico, Text txt) {
		this.ico = (ico == null) ? Ico.BARRIER : ico;
		this.text = txt;

		if (txt == null) {
			this.ico = Ico.BARRIER;
			this.text = Text.literal("No data").formatted(Formatting.GRAY);
		}

		this.width = ICO_DIM + PAD_L + txtRend.getWidth(this.text);
		this.height = ICO_DIM;
	}

	public IcoTextComponent() {
		this(null, null);
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(x, y);
		context.getMatrices().scale((float) ICO_DIM / 16);
		context.drawItem(ico, 0, 0);
		context.getMatrices().popMatrix();
		context.drawText(txtRend, text, x + ICO_DIM + PAD_L, y + 2, Colors.WHITE, false);
	}
}
