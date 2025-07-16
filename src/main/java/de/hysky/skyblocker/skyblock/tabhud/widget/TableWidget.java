package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.text.MutableText;

import java.util.List;
import java.util.Set;

/**
 * Generic widget that arranges rows of components in equal width columns.
 * Implement {@link #buildRows()} to supply the table contents.
 */
public abstract class TableWidget extends ComponentBasedWidget {

	private final int columns;
	private final int lineColor;
	private final boolean drawLines;

	protected TableWidget(MutableText title, int colorValue, String internalId, int columns, int lineColor, boolean drawLines) {
		super(title, colorValue, internalId);
		this.columns = columns;
		this.lineColor = lineColor;
		this.drawLines = drawLines;
	}

	protected TableWidget(MutableText title, int colorValue, String internalId, int columns, int lineColor, boolean drawLines, Set<Location> availableIn) {
		super(title, colorValue, internalId, availableIn);
		this.columns = columns;
		this.lineColor = lineColor;
		this.drawLines = drawLines;
	}

	protected TableWidget(MutableText title, int colorValue, String internalId, int columns, int lineColor) {
		this(title, colorValue, internalId, columns, lineColor, true);
	}

	protected TableWidget(MutableText title, int colorValue, String internalId, int columns, boolean drawLines) {
		this(title, colorValue, internalId, columns, 0xffffffff, drawLines);
	}

	protected TableWidget(MutableText title, int colorValue, String internalId, int columns) {
		this(title, colorValue, internalId, columns, 0xffffffff, true);
	}

	/**
	 * Container class describing a single table row.
	 */
	public static class Row {
		public final List<Component> cells;
		public final int borderColor;

		public Row(List<Component> cells, int borderColor) {
			this.cells = cells;
			this.borderColor = borderColor;
		}
	}

	/**
	 * Called every update to create the rows for the table.
	 *
	 * @return list of rows to render
	 */
	protected abstract List<Row> buildRows();

	@Override
	public void updateContent() {
		List<Row> rows = buildRows();
		TableComponent table = new TableComponent(columns, rows.size(), lineColor, drawLines);
		for (int y = 0; y < rows.size(); y++) {
			Row row = rows.get(y);
			for (int x = 0; x < Math.min(columns, row.cells.size()); x++) {
				table.addToCell(x, y, row.cells.get(x));
			}
			if (row.borderColor != 0) {
				table.setRowBorder(y, row.borderColor);
			}
		}
		addComponent(table);
	}

	@Override
	protected List<Component> getConfigComponents() {
		List<Row> rows = buildRows();
		TableComponent table = new TableComponent(columns, rows.size(), lineColor, drawLines);
		for (int y = 0; y < rows.size(); y++) {
			Row row = rows.get(y);
			for (int x = 0; x < Math.min(columns, row.cells.size()); x++) {
				table.addToCell(x, y, row.cells.get(x));
			}
			if (row.borderColor != 0) {
				table.setRowBorder(y, row.borderColor);
			}
		}
		return List.of(table);
	}
}
