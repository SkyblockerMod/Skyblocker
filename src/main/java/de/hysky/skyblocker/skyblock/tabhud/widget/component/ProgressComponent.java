package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.ColorUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;


/**
 * Component that consists of an icon, some text and a progress bar.
 * The progress bar either shows the fill percentage or custom text.
 * NOTICE: pcnt is 0-100, not 0-1!
 */
public class ProgressComponent extends Component {

	private static final int BAR_WIDTH = 100;
	private static final int BAR_HEIGHT = txtRend.fontHeight + 3;
	private static final int ICO_OFFS = 4;
	private static final int COL_BG_BAR = 0xf0101010;

	private final ItemStack ico;
	private final Text desc, bar;
	private final float pcnt;
	private final int color;
	private final boolean colorIsBright;
	private final int barW;

	/**
	 * @see Components#progressComponent(ItemStack, Text, Text, float)
	 */
	public ProgressComponent(@Nullable ItemStack ico, @Nullable Text description, @Nullable Text bar, float percent, int color) {
		if (description == null || bar == null) {
			this.ico = Ico.BARRIER;
			this.desc = Text.literal("No data").formatted(Formatting.GRAY);
			this.bar = Text.literal("---").formatted(Formatting.GRAY);
			this.pcnt = 100f;
			this.color = 0xff000000 | Formatting.DARK_GRAY.getColorValue();
		} else {
			this.ico = (ico == null) ? Ico.BARRIER : ico;
			this.desc = description;
			this.bar = bar;
			this.pcnt = Math.clamp(percent, 0f, 100f);
			this.color = 0xff000000 | color;
		}

		this.barW = BAR_WIDTH;
		this.width = ICO_DIM + PAD_L + Math.max(this.barW, txtRend.getWidth(this.desc));
		this.height = txtRend.fontHeight + PAD_S + 2 + txtRend.fontHeight + 2;
		this.colorIsBright = ColorUtils.isBright(this.color);
	}

	/**
	 * @see Components#progressComponent(ItemStack, Text, Text, float)
	 */
	public ProgressComponent(@Nullable ItemStack ico, @Nullable Text description, @Nullable Text bar, float percent) {
		this(ico, description, bar, percent, ColorUtils.percentToColor(percent));
	}

	/**
	 * @see Components#progressComponent(ItemStack, Text, float)
	 */
	public ProgressComponent(@Nullable ItemStack ico, @Nullable Text description, float percent, int color) {
		this(ico, description, Text.of(percent + "%"), percent, color);
	}

	/**
	 * @see Components#progressComponent(ItemStack, Text, float)
	 */
	public ProgressComponent(@Nullable ItemStack ico, @Nullable Text description, float percent) {
		this(ico, description, percent, ColorUtils.percentToColor(percent));
	}

	public ProgressComponent() {
		this(null, null, null, 100, 0);
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		context.drawItem(ico, x, y + ICO_OFFS);
		context.drawText(txtRend, desc, x + ICO_DIM + PAD_L, y, Colors.WHITE, false);

		int barX = x + ICO_DIM + PAD_L;
		int barY = y + txtRend.fontHeight + PAD_S;
		int endOffsX = ((int) (this.barW * (this.pcnt / 100f)));
		context.fill(barX + endOffsX, barY, barX + this.barW, barY + BAR_HEIGHT, COL_BG_BAR);
		context.fill(barX, barY, barX + endOffsX, barY + BAR_HEIGHT, this.color);

		int textWidth = txtRend.getWidth(bar);
		// Only turn text dark when it is wider than the filled bar and the filled bar is bright.
		// The + 4 is because the text is indented 3 pixels and 1 extra pixel to the right as buffer.
		boolean textDark = endOffsX >= textWidth + 4 && this.colorIsBright;
		context.drawText(txtRend, bar, barX + 3, barY + 2, textDark ? Colors.BLACK : Colors.WHITE, !textDark);
	}
}
