package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
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

    private ItemStack ico;
    private Text desc, bar;
    private float pcnt;
    private int color;
    private int barW;

    public ProgressComponent(ItemStack ico, Text d, Text b, float pcnt, int color) {
        this.ico = (ico == null) ? Ico.BARRIER : ico;
        this.desc = d;
        this.bar = b;
        this.color = 0xff000000 | color;
        this.pcnt = pcnt;

        if (d == null || b == null) {
            this.ico = Ico.BARRIER;
            this.desc = Text.literal("No data").formatted(Formatting.GRAY);
            this.bar = Text.literal("---").formatted(Formatting.GRAY);
            this.pcnt = 100f;
            this.color = 0xff000000 | Formatting.DARK_GRAY.getColorValue();
        }

        this.barW = BAR_WIDTH;
        this.width = ICO_DIM + PAD_L + Math.max(this.barW, txtRend.getWidth(this.desc));
        this.height = txtRend.fontHeight + PAD_S + 2 + txtRend.fontHeight + 2;
    }

    public ProgressComponent(ItemStack ico, Text text, float pcnt, int color) {
        this(ico, text, Text.of(pcnt + "%"), pcnt, color);
    }

    public ProgressComponent() {
        this(null, null, null, 100, 0);
    }

    @Override
    public void render(MatrixStack ms, int x, int y) {
        itmRend.renderGuiItemIcon(ms, ico, x, y + ICO_OFFS);
        txtRend.draw(ms, desc, x + ICO_DIM + PAD_L, y, 0xffffffff);

        int barX = x + ICO_DIM + PAD_L;
        int barY = y + txtRend.fontHeight + PAD_S;
        int endOffsX = ((int) (this.barW * (this.pcnt / 100f)));
        DrawableHelper.fill(ms, barX + endOffsX, barY, barX + this.barW, barY + BAR_HEIGHT, COL_BG_BAR);
        DrawableHelper.fill(ms, barX, barY, barX + endOffsX, barY + BAR_HEIGHT,
                this.color);
        txtRend.drawWithShadow(ms, bar, barX + 3, barY + 2, 0xffffffff);
    }
}
