package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.gui.DrawContext;

/**
 * Meta-Component that consists of a grid of other components
 * Grid cols are separated by lines.
 */
public class TableComponent extends Component {

	private final Component[][] comps;
	private final int color;
	private final int cols, rows;
	private int cellW, cellH;

	public TableComponent(int w, int h, int col) {
		comps = new Component[w][h];
		color = 0xff000000 | col;
		cols = w;
		rows = h;
	}

	public void addToCell(int x, int y, Component c) {
		this.comps[x][y] = c;

		// pad extra to add a vertical line later
		this.cellW = Math.max(this.cellW, c.width + PAD_S + PAD_L);
		this.cellH = Math.max(c.height + PAD_S, cellH);

		this.width = this.cellW * this.cols;
		this.height = (this.cellH * this.rows) - PAD_S / 2;

	}

	@Override
	public void render(DrawContext context, int xpos, int ypos) {
		for (int x = 0; x < cols; x++) {
			for (int y = 0; y < rows; y++) {
				Component comp = comps[x][y];
				if (comp != null) {
					comp.render(context, xpos + (x * cellW), ypos + y * cellH + (cellH / 2 - comp.height / 2));
				}
			}
			// add a line before the col if we're not drawing the first one
			if (x != 0) {
				int lineX1 = xpos + (x * cellW) - PAD_S - 1;
				int lineX2 = xpos + (x * cellW) - PAD_S;
				int lineY1 = ypos + 1;
				int lineY2 = ypos + this.height - PAD_S - 1; // not sure why but it looks correct
				context.fill(lineX1, lineY1, lineX2, lineY2, this.color);
			}
		}
	}

}
