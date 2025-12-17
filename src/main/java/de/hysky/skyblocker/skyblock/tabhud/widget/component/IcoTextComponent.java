package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

/**
 * Component that consists of an icon and a line of text.
 */
class IcoTextComponent extends Component {
	private ItemStack ico;
	private @Nullable Text text;

	IcoTextComponent(@Nullable ItemStack ico, @Nullable Text txt) {
		this.ico = (ico == null) ? Ico.BARRIER : ico;

		if (txt != null) {
			this.text = txt;
		} else {
			this.ico = Ico.BARRIER;
			this.text = Text.literal("No data").formatted(Formatting.GRAY);
		}

		int iconDim = ICO_DIM.get();
		this.width = iconDim + PAD_L + txtRend.getWidth(this.text);
		this.height = iconDim;
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		int offset = SkyblockerConfigManager.get().uiAndVisuals.tabHud.compactWidgets ? 2 : 4;
		renderIcon(context, ico, x, y);
		context.drawText(txtRend, text, x + ICO_DIM.get() + PAD_L, y + offset, Colors.WHITE, false);
	}
}
