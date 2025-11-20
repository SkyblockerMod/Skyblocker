package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.util.Colors;

public class DropdownWidget<T> extends ContainerWidget {
	private static final MinecraftClient client = MinecraftClient.getInstance();
	public static final int ENTRY_HEIGHT = 15;
	public static final int HEADER_HEIGHT = ENTRY_HEIGHT + 4;
	protected final List<T> entries;
	protected final Consumer<T> selectCallback;
	protected final Consumer<Boolean> openedCallback;
	private final DropdownList dropdownList;
	protected T prevSelected;
	protected T selected;
	protected boolean open;
	private int maxHeight;

	public DropdownWidget(MinecraftClient minecraftClient, int x, int y, int width, int maxHeight, List<T> entries, Consumer<T> selectCallback, T selected, Consumer<Boolean> openedCallback) {
		super(x, y, width, HEADER_HEIGHT, Text.empty());
		this.maxHeight = maxHeight;
		this.entries = entries;
		this.selectCallback = selectCallback;
		this.openedCallback = openedCallback;
		this.selected = selected;
		dropdownList = new DropdownList(minecraftClient, x + 1, y + HEADER_HEIGHT, width - 2, maxHeight - HEADER_HEIGHT);
		for (T element : entries) {
			dropdownList.addEntry(new Entry(element));
		}
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
		context.fill(getX(), getY(), getRight(), getY() + HEADER_HEIGHT + 1, Colors.BLACK);
		HudHelper.drawBorder(context, getX(), getY(), getWidth(), HEADER_HEIGHT + 1, Colors.WHITE);
		context.drawText(client.textRenderer, ">", getX() + 4, getY() + 6, Colors.ALTERNATE_WHITE, true);
		context.drawText(client.textRenderer, selected.toString(), getX() + 12, getY() + 6, Colors.WHITE, true);
		if (isMouseOver(mouseX, mouseY)) context.setCursor(StandardCursors.POINTING_HAND);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	private void setOpen(boolean open) {
		this.open = open;
		if (this.open) {
			setHeight(maxHeight);
			dropdownList.setHeight(Math.min(entries.size() * ENTRY_HEIGHT + 4, maxHeight - HEADER_HEIGHT));
		} else {
			setHeight(HEADER_HEIGHT);
		}
		this.openedCallback.accept(open);
	}

	public boolean isOpen() {
		return this.open;
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
		dropdownList.refreshScroll(); // update entry list positions
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		dropdownList.setY(getY() + HEADER_HEIGHT);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		dropdownList.setWidth(getWidth() - 2);
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (!visible) return false;
		if (getX() <= click.x() && click.x() < getX() + getWidth() && getY() <= click.y() && click.y() < getY() + HEADER_HEIGHT) {
			setOpen(!open);
			playDownSound(client.getSoundManager());
			return true;
		}
		return super.mouseClicked(click, doubled);
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
			super(minecraftClient, width, height, y, ENTRY_HEIGHT);
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
		protected void drawScrollbar(DrawContext context, int mouseX, int mouseY) {
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
		public boolean mouseClicked(Click click, boolean doubled) {
			if (!visible) return false;
			return super.mouseClicked(click, doubled);
		}

		@Override
		public boolean mouseReleased(Click click) {
			if (!visible) return false;
			return super.mouseReleased(click);
		}

		@Override
		public boolean mouseDragged(Click click, double offsetX, double offsetY) {
			if (!visible) return false;
			return super.mouseDragged(click, offsetX, offsetY);
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
			context.fill(getX(), getY(), getRight(), getBottom(), 0xFF << 24);
			HudHelper.drawBorder(context, getX(), getY(), getWidth(), getHeight(), -1);
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
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			// drawScrollableText does some weird stuff with the y value, so we put startY = y and endY = y + 11 which makes the text render on the same line as the tick mark below (y + 2).
			drawScrollableText(context, client.textRenderer, Text.literal(entry.toString()).fillStyle(Style.EMPTY.withUnderline(hovered)), this.getX() + 10, this.getY(), this.getX() + this.getWidth(), this.getY() + 11, -1);
			if (selected == this.entry) {
				context.drawTextWithShadow(client.textRenderer, "✔", this.getX() + 1, this.getY() + 2, 0xFFFFFFFF);
			}
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
			select(entry);
			return true;
		}
	}
}
