package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.ColorUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


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

	public ProgressComponent(ItemStack ico, Text d, Text b, float pcnt, int color) {
		if (d == null || b == null) {
			this.ico = Ico.BARRIER;
			this.desc = Text.literal("No data").formatted(Formatting.GRAY);
			this.bar = Text.literal("---").formatted(Formatting.GRAY);
			this.pcnt = 100f;
			this.color = 0xff000000 | Formatting.DARK_GRAY.getColorValue();
		} else {
			this.ico = (ico == null) ? Ico.BARRIER : ico;
			this.desc = d;
			this.bar = b;
			this.pcnt = Math.clamp(pcnt, 0f, 100f);
			this.color = 0xff000000 | color;
		}

		this.barW = BAR_WIDTH;
		this.width = ICO_DIM + PAD_L + Math.max(this.barW, txtRend.getWidth(this.desc));
		this.height = txtRend.fontHeight + PAD_S + 2 + txtRend.fontHeight + 2;
		this.colorIsBright = ColorUtils.isBright(this.color);
	}

	public ProgressComponent(ItemStack ico, Text text, float pcnt, int color) {
		this(ico, text, Text.of(pcnt + "%"), pcnt, color);
	}

	public ProgressComponent() {
		this(null, null, null, 100, 0);
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		context.drawItem(ico, x, y + ICO_OFFS);
		context.drawText(txtRend, desc, x + ICO_DIM + PAD_L, y, 0xffffffff, false);

		int barX = x + ICO_DIM + PAD_L;
		int barY = y + txtRend.fontHeight + PAD_S;
		int endOffsX = ((int) (this.barW * (this.pcnt / 100f)));
		context.fill(barX + endOffsX, barY, barX + this.barW, barY + BAR_HEIGHT, COL_BG_BAR);
		context.fill(barX, barY, barX + endOffsX, barY + BAR_HEIGHT, this.color);

		int textWidth = txtRend.getWidth(bar);
		// Only turn text dark when it is wider than the filled bar and the filled bar is bright.
		// The + 4 is because the text is indented 3 pixels and 1 extra pixel to the right as buffer.
		boolean textDark = endOffsX >= textWidth + 4 && this.colorIsBright;
		context.drawText(txtRend, bar, barX + 3, barY + 2, textDark ? 0xff000000 : 0xffffffff, !textDark);
	}
}
