package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

// widget component that consists of an icon, some text and a progress bar
// progress bar either shows percentage or custom text
// NOTICE: pcnt is 0-100, not 0-1!

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

    public ProgressComponent(ItemStack ico, Text desc, Text bar, float pcnt, int color) {
        this.ico = ico;
        this.desc = desc;
        this.bar = bar;
        this.color = 0xff000000 | color;
        this.pcnt = pcnt;

        this.barW = BAR_WIDTH;
        this.width = ICO_DIM + PAD_L + Math.max(this.barW, txtRend.getWidth(desc));
        this.height = txtRend.fontHeight + PAD_S + 2 + txtRend.fontHeight + 2;
    }

    public ProgressComponent(ItemStack ico, Text text, float pcnt, int color) {
        this(ico, text, Text.of(pcnt + "%"), pcnt, color);
    }

    @Override
    public void render(MatrixStack ms, int x, int y) {
        itmRend.renderGuiItemIcon(ms, ico, x, y + ICO_OFFS);
        txtRend.draw(ms, desc, x + ICO_DIM + PAD_L, y, 0xffffffff);

        int barX = x + ICO_DIM + PAD_L;
        int barY = y + txtRend.fontHeight + PAD_S;
        DrawableHelper.fill(ms, barX, barY, barX + this.barW, barY + BAR_HEIGHT, COL_BG_BAR);
        DrawableHelper.fill(ms, barX, barY, barX + ((int) (this.barW * (this.pcnt / 100f))), barY + BAR_HEIGHT,
                this.color);
        txtRend.drawWithShadow(ms, bar, barX + 3, barY + 2, 0xffffffff);
    }
}
