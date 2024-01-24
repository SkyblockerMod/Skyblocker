package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class TextIcoTextComponent extends Component {

	private Text textBefore;
	private final ItemStack ico;
	private Text textAfter;

	public TextIcoTextComponent(@Nullable Text textBefore, ItemStack ico, @Nullable Text textAfter) {
		this.textBefore = textBefore == null ? Text.literal("") : textBefore; //Support null values so that multiple formats can be achieved with this class
		this.ico = (ico == null) ? Ico.BARRIER : ico;
		this.textAfter = textAfter == null ? Text.literal("") : textAfter;

		this.width = ICO_DIM + (PAD_S*3) + txtRend.getWidth(this.textBefore) + txtRend.getWidth(this.textAfter);
		if (textBefore == null || textAfter == null) this.width -= PAD_S;
		if (textBefore == null && textAfter == null) this.width -= PAD_S;
		this.height = ICO_DIM;
	}

	public TextIcoTextComponent(Text textBefore, ItemStack ico) {
		this(textBefore, ico, null);
	}

	public TextIcoTextComponent(ItemStack ico, Text textAfter) {
		this(null, ico, textAfter);
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		if (textBefore != null) context.drawText(txtRend, textBefore, x + PAD_S, y + 5, 0xffffffff, false);
		int icoX = x + PAD_S + txtRend.getWidth(textBefore) + PAD_S;
		if (textBefore == null) icoX -= PAD_S;
		context.drawItem(ico, icoX, y);
		if (textAfter != null) context.drawText(txtRend, textAfter, icoX + ICO_DIM + PAD_S, y + 5, 0xffffffff, false);
	}
}
