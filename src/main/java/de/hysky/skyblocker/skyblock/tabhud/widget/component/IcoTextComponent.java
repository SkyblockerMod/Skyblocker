package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

/**
 * Component that consists of an icon and a line of text.
 */
class IcoTextComponent extends Component {
	private ItemStack ico;
	private Text text;

	IcoTextComponent(ItemStack ico, Text txt) {
		this.ico = (ico == null) ? Ico.BARRIER : ico;
		this.text = txt;

		if (txt == null) {
			this.ico = Ico.BARRIER;
			this.text = Text.literal("No data").formatted(Formatting.GRAY);
		}

		int iconDim = ICO_DIM.get();
		this.width = iconDim + PAD_L + txtRend.getWidth(this.text);
		this.height = iconDim;
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		int iconDim = ICO_DIM.get();
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(x, y);
		context.getMatrices().scale((float) iconDim / 16);
		context.drawItem(ico, 0, 0);
		context.getMatrices().popMatrix();

		int offset = SkyblockerConfigManager.get().uiAndVisuals.tabHud.compactWidgets ? 2 : 4;
		context.drawText(txtRend, text, x + iconDim + PAD_L, y + offset, Colors.WHITE, false);
	}
}
