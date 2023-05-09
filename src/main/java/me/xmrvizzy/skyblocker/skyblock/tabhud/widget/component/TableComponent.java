package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

// widget component that consists of a grid of other components
// grid cols are separated by lines

// FIXME: table isn't wide enough sometimes
// FIXME: dividers drift when there are >2 cols
public class TableComponent extends Component {

    private Component[][] comps;
    private int color;
    private int tw, th;
    private int cellW, cellH;

    public TableComponent(int w, int h, int col) {
        comps = new Component[w][h];
        color = 0xff000000 | col;
        tw = w;
        th = h;
    }

    public void addToCell(int x, int y, Component c) {
        this.comps[x][y] = c;

        // are tables still too wide?
        this.cellW = Math.max(this.cellW, c.width + PAD_S);

        // assume all rows are equally high so overwriting doesn't matter
        // if this wasn't the case, drawing would need more math
        // not doing any of that if it's not needed
        this.cellH = c.height;

        this.width = this.cellW * this.tw;
        this.height = (this.cellH + PAD_S) * this.th - PAD_S;

    }

    @Override
    public void render(MatrixStack ms, int xpos, int ypos) {
        for (int x = 0; x < tw; x++) {
            for (int y = 0; y < th; y++) {
                if (comps[x][y] != null) {
                    comps[x][y].render(ms, xpos + x * cellW + x * PAD_L, ypos + y * cellH + y * PAD_S);
                }
            }
            if (x != tw - 1) {
                DrawableHelper.fill(ms, xpos + ((x + 1) * (cellW + PAD_S)) - 1, ypos + 1,
                        xpos + ((x + 1) * (cellW + PAD_S)), ypos + this.height - 1, this.color);
            }
        }
    }

}
