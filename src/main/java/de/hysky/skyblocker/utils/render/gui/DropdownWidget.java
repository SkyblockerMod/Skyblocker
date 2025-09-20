package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropdownWidget<T> extends ContainerWidget {
	protected static final MinecraftClient client = MinecraftClient.getInstance();
	public final int entryHeight;
	protected int headerHeight;
	protected final List<T> entries;
	protected final Consumer<T> selectCallback;
	private final DropdownList dropdownList;
	protected T prevSelected;
	protected T selected;
	protected boolean open;
	private int maxHeight;
	protected Function<T, Text> formatter = t -> Text.literal(t.toString());


	public DropdownWidget(MinecraftClient minecraftClient, int x, int y, int width, int maxHeight, int entryHeight, List<T> entries, Consumer<T> selectCallback, T selected) {
		super(x, y, width, 0, Text.empty());
		this.entryHeight = entryHeight;
		this.headerHeight = entryHeight + 4;
		this.maxHeight = maxHeight;
		this.entries = entries;
		this.selectCallback = selectCallback;
		this.selected = selected;
		dropdownList = new DropdownList(minecraftClient, x + 1, y + headerHeight, width - 2, maxHeight - headerHeight);
		for (T element : entries) {
			dropdownList.addEntry(new Entry(element));
		}
		setHeight(headerHeight);
	}

	public void setFormatter(Function<T, Text> formatter) {
		this.formatter = formatter;
	}

	public DropdownWidget(MinecraftClient minecraftClient, int x, int y, int width, int maxHeight, List<T> entries, Consumer<T> selectCallback, T selected) {
		this(minecraftClient, x, y, width, maxHeight, 15, entries, selectCallback, selected);
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
		setOpen(open);
	}

	@Override
	public List<? extends Element> children() {
		return List.of(dropdownList);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		dropdownList.visible = open;
		dropdownList.render(context, mouseX, mouseY, delta);
		renderHeader(context, mouseX, mouseY, delta);
	}

	protected void renderHeader(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(getX(), getY(), getRight(), getY() + headerHeight + 1, 0xFF << 24);
		context.drawBorder(getX(), getY(), getWidth(), headerHeight + 1, -1);
		drawScrollableText(context, client.textRenderer, formatter.apply(selected),
				getX() + 2,
				getY() + 2,
				getRight() - 2,
				getY() + headerHeight - 2,
				-1);
	}

	protected void renderEntry(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, T entry) {
		// drawScrollableText does some weird stuff with the y value, so we put startY = y and endY = y + 11 which makes the text render on the same line as the tick mark below (y + 2).
		drawScrollableText(context, client.textRenderer, formatter.apply(entry).copy().fillStyle(Style.EMPTY.withUnderline(hovered)), x + 10, y, x + entryWidth, y + 11, -1);
		if (selected == entry) {
			context.drawTextWithShadow(client.textRenderer, "✔", x + 1, y + 2, 0xFFFFFFFF);
		}
	}

	protected void drawMenuListBackground(DrawContext context, int listX, int listY, int listWidth, int listHeight) {
		context.fill(listX, listY, listX + listWidth, listY + listHeight, 0xFF << 24);
		context.drawBorder(listX, listY, listWidth, listHeight, -1);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	private void setOpen(boolean open) {
		this.open = open;
		if (this.open) {
			setHeight(maxHeight);
			dropdownList.setHeight(Math.min(entries.size() * entryHeight + 4, maxHeight - headerHeight));
		} else {
			setHeight(headerHeight);
		}
	}

	protected void select(T entry) {
		selected = entry;
		setOpen(false);
		if (selected != prevSelected) {
			selectCallback.accept(entry);
			prevSelected = selected;
		}
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		dropdownList.setX(getX() + 1);
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		dropdownList.setY(getY() + headerHeight);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		dropdownList.setWidth(getWidth() - 2);
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		dropdownList.setHeight(height - headerHeight);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!visible) return false;
		if (getX() <= mouseX && mouseX < getX() + getWidth() && getY() <= mouseY && mouseY < getY() + headerHeight) {
			setOpen(!open);
			playDownSound(client.getSoundManager());
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return getHeight();
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 0;
	}

	// container widget doesn't make it go to children anymore cuz WHY NOT
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!visible) return false;
		if (this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent()) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private class DropdownList extends ElementListWidget<Entry> {

		private DropdownList(MinecraftClient minecraftClient, int x, int y, int width, int height) {
			super(minecraftClient, width, height, y, entryHeight);
			setX(x);
		}

		@Override
		protected int addEntry(DropdownWidget<T>.Entry entry) {
			return super.addEntry(entry);
		}

		@Override
		public int getRowLeft() {
			return getX() + 2;
		}

		@Override
		public int getRowWidth() {
			return getWidth() - 5; // 1 for scrollbar
		}

		// Custom scrollbar


		@Override
		protected void drawScrollbar(DrawContext context) {
			if (this.overflows()) {
				int i = this.getScrollbarX();
				int j = this.getScrollbarThumbHeight();
				int k = this.getScrollbarThumbY();
				// Modified from DrawContext#drawVerticalLine
				context.fill(i, k + 1, i + 2, k + j, -1);
			}
		}

		@Override
		protected int getScrollbarX() {
			return getRowLeft() + getRowWidth();
		}

		// Visible

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (!visible) return false;
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			if (!visible) return false;
			return super.mouseReleased(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (!visible) return false;
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
			if (!visible) return false;
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}

		// Background

		@Override
		protected void drawHeaderAndFooterSeparators(DrawContext context) {}

		@Override
		protected void drawMenuListBackground(DrawContext context) {
			DropdownWidget.this.drawMenuListBackground(context, getX(), getY(), getWidth(), getHeight());
		}

		@Override
		protected void enableScissor(DrawContext context) {
			context.enableScissor(this.getX(), this.getY() + 1, this.getRight(), this.getBottom() - 1);
		}
	}

	private class Entry extends ElementListWidget.Entry<Entry> {

		private final T entry;

		private Entry(T element) {
			this.entry = element;
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			renderEntry(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta, entry);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			select(entry);
			return true;
		}
	}
}
