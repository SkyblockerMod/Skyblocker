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
	private final int[] colWidths;
	private final int[] rowBorders;
	private final int[] rowBaseHeights;
	private final int[] rowHeights;
	private final boolean drawLines;

	private static final int EXTRA_PAD = PAD_L * 2 - PAD_S;

	public TableComponent(int w, int h, int col, boolean drawLines) {
		comps = new Component[w][h];
		this.color = drawLines && col != 0 ? 0xff000000 | col : 0;
		this.drawLines = drawLines && col != 0;
		cols = w;
		rows = h;
		colWidths = new int[w];
		rowBorders = new int[h];
		rowBaseHeights = new int[h];
		rowHeights = new int[h];
	}

	public void addToCell(int x, int y, Component c) {
		this.comps[x][y] = c;

		// widen the first column on both sides
		int pad = x == 0 ? PAD_L * 2 : PAD_L;
		colWidths[x] = Math.max(colWidths[x], c.width + pad);

		int baseH = c.height + PAD_S;
		if (baseH > rowBaseHeights[y]) {
			rowBaseHeights[y] = baseH;
			rowHeights[y] = baseH + (rowBorders[y] != 0 ? EXTRA_PAD : 0);
		} else if (rowBorders[y] != 0) {
			// ensure padded height is at least base + EXTRA_PAD
			rowHeights[y] = Math.max(rowHeights[y], rowBaseHeights[y] + EXTRA_PAD);
		} else {
			rowHeights[y] = Math.max(rowHeights[y], rowBaseHeights[y]);
		}

		recalcDimensions();

	}

	/**
	 * Outline a row with the specified color. Pass {@code 0} to clear.
	 */
	public void setRowBorder(int row, int borderColor) {
		if (row >= 0 && row < rowBorders.length) {
			boolean hadBorder = rowBorders[row] != 0;
			rowBorders[row] = borderColor == 0 ? 0 : 0xff000000 | borderColor;
			boolean hasBorder = rowBorders[row] != 0;

			if (hasBorder && !hadBorder) {
				rowHeights[row] = Math.max(rowHeights[row], rowBaseHeights[row] + EXTRA_PAD);
			} else if (!hasBorder && hadBorder) {
				rowHeights[row] = rowBaseHeights[row];
			}
			recalcDimensions();
		}
	}

	@Override
	public void render(DrawContext context, int xpos, int ypos) {
		int yOff = 0;
		for (int y = 0; y < rows; y++) {
			int col = rowBorders[y];
			if (col != 0) {
				// shift slightly so the border does not clash with the widget outline
				context.drawBorder(xpos, ypos + yOff, this.width + PAD_S, rowHeights[y], col);
			}
			yOff += rowHeights[y];
		}

		int xOff = 0;
		for (int x = 0; x < cols; x++) {
			// draw separator before column except the first
			if (drawLines && x != 0) {
				int lineX1 = xpos + xOff - PAD_S - 1;
				int lineX2 = xpos + xOff - PAD_S;
				int lineY1 = ypos + 1;
				int lineY2 = ypos + this.height - PAD_S - 1;
				context.fill(lineX1, lineY1, lineX2, lineY2, this.color);
			}
			yOff = 0;
			for (int y = 0; y < rows; y++) {
				Component comp = comps[x][y];
				if (comp != null) {
					// indent the first column only when a border is drawn
					int pad = x == 0 && rowBorders[y] != 0 ? PAD_L / 2 : 0;
					// shift down so the component is vertically centered within the row border
					comp.render(context, xpos + xOff + pad, ypos + yOff + (rowHeights[y] / 2 - comp.height / 2 + 1));
				}
				yOff += rowHeights[y];
			}
			xOff += colWidths[x];
		}
	}

	private void recalcDimensions() {
		int totalW = 0;
		for (int w : colWidths) totalW += w;
		this.width = totalW;

		int totalH = -PAD_S / 2;
		for (int h : rowHeights) totalH += h;
		this.height = totalH;
	}
}
