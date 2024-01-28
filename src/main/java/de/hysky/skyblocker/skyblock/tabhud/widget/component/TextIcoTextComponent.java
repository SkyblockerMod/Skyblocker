package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class TextIcoTextComponent extends Component {

	private final Text textBefore;
	private final ItemStack ico;
	private final Text textAfter;

	/**Creates a {@link TextIcoTextComponent} with a {@link Text} before and after the {@link ItemStack}
	 * @param textBefore The {@link Text} to display before the {@link ItemStack}. Can be null.
	 * @param ico The {@link ItemStack} to display. Will turn into a {@link Ico#BARRIER} if null. Use {@link PlainTextComponent} if you only want to display {@link Text} instead.
	 * @param textAfter The {@link Text} to display after the {@link ItemStack}. Can be null.
	 */
	public TextIcoTextComponent(@Nullable Text textBefore, ItemStack ico, @Nullable Text textAfter) {
		this.textBefore = textBefore == null ? Text.literal("") : textBefore; //Support null values so that multiple formats can be achieved with this class
		this.ico = (ico == null) ? Ico.BARRIER : ico;
		this.textAfter = textAfter == null ? Text.literal("") : textAfter;

		this.width = ICO_DIM + (PAD_S*3) + txtRend.getWidth(this.textBefore) + txtRend.getWidth(this.textAfter);
		if (textBefore == null || textAfter == null) this.width -= PAD_S;
		if (textBefore == null && textAfter == null) this.width -= PAD_S;
		this.height = ICO_DIM;
	}

	/**Convenience constructor for creating a {@link TextIcoTextComponent} with only a {@link Text} before the {@link ItemStack}
	 * @param textBefore The {@link Text} to display before the {@link ItemStack}.
	 * @param ico The {@link ItemStack} to display.
	 */
	public TextIcoTextComponent(Text textBefore, ItemStack ico) {
		this(textBefore, ico, null);
	}

	/**Convenience constructor for creating a {@link TextIcoTextComponent} with only a {@link Text} after the {@link ItemStack}
	 * @param ico The {@link ItemStack} to display.
	 * @param textAfter The {@link Text} to display after the {@link ItemStack}.
	 */
	public TextIcoTextComponent(ItemStack ico, Text textAfter) {
		this(null, ico, textAfter);
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		if (textBefore != null) context.drawText(txtRend, textBefore, x + PAD_S, y + 5, 0xffffffff, false);
		int icoX = x + PAD_S + txtRend.getWidth(textBefore) + PAD_S;
		if (textBefore == null) icoX -= PAD_S; //Move it back so that there's no extra padding
		context.drawItem(ico, icoX, y);
		if (textAfter != null) context.drawText(txtRend, textAfter, icoX + ICO_DIM + PAD_S, y + 5, 0xffffffff, false);
	}
}
