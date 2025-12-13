package de.hysky.skyblocker.utils.render.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class SearchableGridWidget extends AbstractContainerWidget {
	private static final int TEXT_FIELD_HEIGHT = 20;

	private final List<AbstractWidget> filteredWidgets = new ObjectArrayList<>();
	protected GridLayout grid = new GridLayout();

	private final LinearLayout layoutWidget = LinearLayout.vertical();
	private final EditBox searchField;
	private final WidgetsContainer widgetsContainer;

	private final int expectedWidgetWidth;

	public SearchableGridWidget(int x, int y, int width, int height, Component message, int expectedWidgetWidth) {
		super(x, y, width, height, message);
		searchField = new EditBox(Minecraft.getInstance().font, width, TEXT_FIELD_HEIGHT, Component.translatable("gui.recipebook.search_hint"));
		searchField.setHint(Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
		searchField.setResponder(this::filterInternal);
		this.expectedWidgetWidth = expectedWidgetWidth;

		widgetsContainer = new WidgetsContainer();
		layoutWidget.addChild(searchField);
		layoutWidget.addChild(widgetsContainer);
		layoutWidget.arrangeElements();
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
		searchField.setValue(search);
	}

	protected void recreateGrid() {
		GridLayout newGrid = new GridLayout();
		GridLayout.RowHelper adder = newGrid.createRowHelper((getWidth() - 6) / expectedWidgetWidth);
		filteredWidgets.forEach(adder::addChild);
		newGrid.arrangeElements();
		newGrid.setPosition(grid.getX(), grid.getY());
		grid = newGrid;
	}

	private void filterInternal(String input) {
		Collection<? extends AbstractWidget> widgets = filterWidgets(input);
		filteredWidgets.clear();
		filteredWidgets.addAll(widgets);
		recreateGrid();
	}

	protected abstract Collection<? extends AbstractWidget> filterWidgets(String input);

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of(searchField, widgetsContainer);
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		searchField.render(context, mouseX, mouseY, deltaTicks);
		widgetsContainer.render(context, mouseX, mouseY, deltaTicks);
	}

	private class WidgetsContainer extends AbstractContainerWidget {

		private WidgetsContainer() {
			super(0, 0, SearchableGridWidget.this.getWidth(), SearchableGridWidget.this.getHeight() - TEXT_FIELD_HEIGHT, Component.literal("Grid"));
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
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			context.enableScissor(getX(), getY(), getRight(), getBottom());
			for (AbstractWidget widget : filteredWidgets) {
				if (isVisible(widget)) widget.render(context, mouseX, mouseY, deltaTicks);
			}
			renderScrollbar(context, mouseX, mouseY);
			context.disableScissor();
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
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
