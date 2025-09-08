package de.hysky.skyblocker.utils.render.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.List;

public abstract class SearchableGridWidget extends ContainerWidget {
	private static final int TEXT_FIELD_HEIGHT = 20;

	private final List<ClickableWidget> filteredWidgets = new ObjectArrayList<>();
	protected GridWidget grid = new GridWidget();

	private final DirectionalLayoutWidget layoutWidget = DirectionalLayoutWidget.vertical();
	private final TextFieldWidget searchField;
	private final WidgetsContainer widgetsContainer;

	private final int expectedWidgetWidth;

	public SearchableGridWidget(int x, int y, int width, int height, Text message, int expectedWidgetWidth) {
		super(x, y, width, height, message);
		searchField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width, TEXT_FIELD_HEIGHT, Text.translatable("gui.recipebook.search_hint"));
		searchField.setPlaceholder(Text.translatable("gui.recipebook.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
		searchField.setChangedListener(this::filterInternal);
		this.expectedWidgetWidth = expectedWidgetWidth;

		widgetsContainer = new WidgetsContainer();
		layoutWidget.add(searchField);
		layoutWidget.add(widgetsContainer);
		layoutWidget.refreshPositions();
		layoutWidget.setPosition(x, y);
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

	public void setSearch(String search) {
		searchField.setText(search);
	}

	protected void recreateGrid() {
		GridWidget newGrid = new GridWidget();
		GridWidget.Adder adder = newGrid.createAdder((getWidth() - 6) / expectedWidgetWidth);
		filteredWidgets.forEach(adder::add);
		newGrid.refreshPositions();
		newGrid.setPosition(grid.getX(), grid.getY());
		grid = newGrid;
	}

	private void filterInternal(String input) {
		Collection<? extends ClickableWidget> widgets = filterWidgets(input);
		filteredWidgets.clear();
		filteredWidgets.addAll(widgets);
		recreateGrid();
	}

	protected abstract Collection<? extends ClickableWidget> filterWidgets(String input);

	@Override
	public List<? extends Element> children() {
		return List.of(searchField, widgetsContainer);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		searchField.render(context, mouseX, mouseY, deltaTicks);
		widgetsContainer.render(context, mouseX, mouseY, deltaTicks);
	}

	private class WidgetsContainer extends ContainerWidget {

		private WidgetsContainer() {
			super(0, 0, SearchableGridWidget.this.getWidth(), SearchableGridWidget.this.getHeight() - TEXT_FIELD_HEIGHT, Text.literal("Grid"));
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
		public List<? extends Element> children() {
			return filteredWidgets;
		}

		@Override
		protected int getContentsHeightWithPadding() {
			return grid.getHeight();
		}

		@Override
		protected double getDeltaYPerScroll() {
			return SearchableGridWidget.this.getDeltaYPerScroll();
		}

		@Override
		public void setScrollY(double scrollY) {
			super.setScrollY(scrollY);
			grid.setY(getY() - (int) getScrollY());
		}

		private boolean isVisible(ClickableWidget widget) {
			return widget.getBottom() >= getY() && widget.getY() < getBottom();
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			context.enableScissor(getX(), getY(), getRight(), getBottom());
			for (ClickableWidget widget : filteredWidgets) {
				if (isVisible(widget)) widget.render(context, mouseX, mouseY, deltaTicks);
			}
			drawScrollbar(context);
			context.disableScissor();
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return 0;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
