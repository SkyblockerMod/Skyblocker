package de.hysky.skyblocker.utils.render.gui;

import java.util.Collection;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

public abstract class SearchableGridWidget extends AbstractContainerWidget {
	private static final int TEXT_FIELD_HEIGHT = 20;

	private final List<AbstractWidget> filteredWidgets = new ObjectArrayList<>();

	private final LinearLayout layoutWidget = LinearLayout.vertical();
	private final EditBox searchField;
	private final WidgetsContainer widgetsContainer;

	private final int expectedWidgetWidth;
	private final int maxPerRow;
	private final boolean packed;
	private final boolean spaceElementsOut;

	/// A grid of searchable widgets
	/// @param expectedWidgetWidth The expected width of each widget in the grid. This class may place multiple grid widgets in the same row.
	/// @param maxPerRow The maximum number of widgets to place in a single row. This class may place fewer widgets in a row if the width of this grid is too small.
	/// @param packed If true, the width of this grid will be reduced to fit as many widgets as possible in a row tightly.
	/// If false, the width of this grid will be the same as the width passed to the constructor, and there may be space between widgets in the grid.
	/// @param spaceElementsOut If true, the widgets in the grid will be spaced out to fill the entire width of the grid.
	/// If false, the widgets will be placed next to each other with no space between them.
	/// This parameter is essentially ignored if packed is true.
	public SearchableGridWidget(int x, int y, int width, int height, Component message, int expectedWidgetWidth, int maxPerRow, boolean packed, boolean spaceElementsOut) {
		super(x, y, width, height, message, AbstractScrollArea.defaultSettings(8));
		searchField = new EditBox(Minecraft.getInstance().font, width, TEXT_FIELD_HEIGHT, Component.translatable("gui.recipebook.search_hint"));
		searchField.setHint(Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
		searchField.setResponder(this::filterInternal);
		this.expectedWidgetWidth = expectedWidgetWidth;
		this.maxPerRow = maxPerRow;
		this.packed = packed;
		this.spaceElementsOut = spaceElementsOut;

		widgetsContainer = new WidgetsContainer();
		layoutWidget.addChild(searchField);
		layoutWidget.addChild(widgetsContainer);
		layoutWidget.arrangeElements();
		layoutWidget.setPosition(x, y);

		setWidth(getWidth()); // Trigger width calculation for packed grids
	}

	/// @see SearchableGridWidget#SearchableGridWidget(int, int, int, int, Component, int, int, boolean, boolean)
	public SearchableGridWidget(int x, int y, int width, int height, Component message, int expectedWidgetWidth, int maxPerRow, boolean packed) {
		this(x, y, width, height, message, expectedWidgetWidth, maxPerRow, packed, false);
	}

	/// @see SearchableGridWidget#SearchableGridWidget(int, int, int, int, Component, int, int, boolean, boolean)
	public SearchableGridWidget(int x, int y, int width, int height, Component message, int expectedWidgetWidth, boolean packed) {
		this(x, y, width, height, message, expectedWidgetWidth, Integer.MAX_VALUE, packed);
	}

	/// You probably want packed to be true.
	/// @see SearchableGridWidget#SearchableGridWidget(int, int, int, int, Component, int, int, boolean, boolean)
	public SearchableGridWidget(int x, int y, int width, int height, Component message, int expectedWidgetWidth) {
		this(x, y, width, height, message, expectedWidgetWidth, false);
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		layoutWidget.setX(x);
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		layoutWidget.setY(y);
	}

	@Override
	public void setWidth(int width) {
		if (packed) {
			int perRow = Math.min((width - AbstractScrollArea.SCROLLBAR_WIDTH) / expectedWidgetWidth, maxPerRow);
			int newWidth = perRow * expectedWidgetWidth + AbstractScrollArea.SCROLLBAR_WIDTH;
			setX(getX() + (width - newWidth) / 2);
			width = newWidth;
		}
		super.setWidth(width);
		searchField.setWidth(width);
		widgetsContainer.setWidth(width);
		layoutWidget.arrangeElements();
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		widgetsContainer.setHeight(height - TEXT_FIELD_HEIGHT);
		layoutWidget.arrangeElements();
	}

	public void setSearch(String search) {
		searchField.setValue(search);
	}

	public void refreshSearch() {
		searchField.setValue(searchField.getValue());
	}

	public void setScrollAmount(double amount) {
		widgetsContainer.setScrollAmount(amount);
	}

	public double getScrollAmount() {
		return widgetsContainer.scrollAmount();
	}

	/**
	 * @return the grid's rectangle. Does not include the search bar.
	 */
	public ScreenRectangle getGridRectangle() {
		return widgetsContainer.getRectangle();
	}

	private void filterInternal(String input) {
		Collection<? extends AbstractWidget> widgets = filterWidgets(input);
		filteredWidgets.clear();
		filteredWidgets.addAll(widgets);
		widgetsContainer.recreateGrid();
	}

	protected abstract Collection<? extends AbstractWidget> filterWidgets(String input);

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of(searchField, widgetsContainer);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		searchField.extractRenderState(context, mouseX, mouseY, deltaTicks);
		widgetsContainer.extractRenderState(context, mouseX, mouseY, deltaTicks);
	}

	private class WidgetsContainer extends AbstractContainerWidget {
		protected GridLayout grid = new GridLayout();

		private WidgetsContainer() {
			super(0, 0, SearchableGridWidget.this.getWidth(), SearchableGridWidget.this.getHeight() - TEXT_FIELD_HEIGHT, Component.literal("Grid"), AbstractScrollArea.defaultSettings(8));
		}

		@Override
		public void setX(int x) {
			super.setX(x);
			grid.setX(x);
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			grid.setY(y);
		}

		@Override
		public void setWidth(int width) {
			super.setWidth(width);
			recreateGrid();
		}

		@Override
		public void setHeight(int height) {
			super.setHeight(height);
			recreateGrid();
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return filteredWidgets;
		}

		@Override
		protected int contentHeight() {
			return grid.getHeight();
		}

		@Override
		protected double scrollRate() {
			return SearchableGridWidget.this.scrollRate();
		}

		@Override
		public void setScrollAmount(double scrollY) {
			super.setScrollAmount(scrollY);
			grid.setY(getY() - (int) scrollAmount());
		}

		private boolean isVisible(AbstractWidget widget) {
			return widget.getBottom() >= getY() && widget.getY() < getBottom();
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
			context.enableScissor(getX(), getY(), getRight(), getBottom());
			for (AbstractWidget widget : filteredWidgets) {
				if (isVisible(widget)) widget.extractRenderState(context, mouseX, mouseY, deltaTicks);
			}
			extractScrollbar(context, mouseX, mouseY);
			context.disableScissor();
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}

		protected void recreateGrid() {
			GridLayout newGrid = new GridLayout();
			int columns = (getWidth() - AbstractScrollArea.SCROLLBAR_WIDTH) / expectedWidgetWidth;
			GridLayout.RowHelper adder = newGrid.createRowHelper(columns);
			filteredWidgets.forEach(adder::addChild);
			if (spaceElementsOut) {
				newGrid.columnSpacing(((getWidth() - AbstractScrollArea.SCROLLBAR_WIDTH) - columns * expectedWidgetWidth) / columns);
			}
			newGrid.arrangeElements();
			newGrid.setPosition(grid.getX(), grid.getY());
			grid = newGrid;

			widgetsContainer.refreshScrollAmount();
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
	}

	@Override
	protected int contentHeight() {
		return 0;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
