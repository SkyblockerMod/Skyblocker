package de.hysky.skyblocker.utils.render.gui;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonColors;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.hysky.skyblocker.utils.render.HudHelper;

public class DropdownWidget<T> extends AbstractContainerWidget {
	private static final Minecraft client = Minecraft.getInstance();
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

	public DropdownWidget(Minecraft minecraftClient, int x, int y, int width, int maxHeight, List<T> entries, Consumer<T> selectCallback, T selected, Consumer<Boolean> openedCallback) {
		super(x, y, width, HEADER_HEIGHT, Component.empty());
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
	public List<? extends GuiEventListener> children() {
		return List.of(dropdownList);
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		dropdownList.visible = open;
		dropdownList.render(context, mouseX, mouseY, delta);
		context.fill(getX(), getY(), getRight(), getY() + HEADER_HEIGHT + 1, CommonColors.BLACK);
		HudHelper.drawBorder(context, getX(), getY(), getWidth(), HEADER_HEIGHT + 1, CommonColors.WHITE);
		context.drawString(client.font, ">", getX() + 4, getY() + 6, CommonColors.LIGHTER_GRAY, true);
		context.drawString(client.font, selected.toString(), getX() + 12, getY() + 6, CommonColors.WHITE, true);
		if (isMouseOver(mouseX, mouseY)) context.requestCursor(CursorTypes.POINTING_HAND);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

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
		dropdownList.refreshScrollAmount(); // update entry list positions
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
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (!visible) return false;
		if (getX() <= click.x() && click.x() < getX() + getWidth() && getY() <= click.y() && click.y() < getY() + HEADER_HEIGHT) {
			setOpen(!open);
			playDownSound(client.getSoundManager());
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	protected int contentHeight() {
		return getHeight();
	}

	@Override
	protected double scrollRate() {
		return 0;
	}

	// container widget doesn't make it go to children anymore cuz WHY NOT
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!visible) return false;
		if (this.getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent()) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private class DropdownList extends ContainerObjectSelectionList<Entry> {

		private DropdownList(Minecraft minecraftClient, int x, int y, int width, int height) {
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
		protected void renderScrollbar(GuiGraphics context, int mouseX, int mouseY) {
			if (this.scrollbarVisible()) {
				int i = this.scrollBarX();
				int j = this.scrollerHeight();
				int k = this.scrollBarY();
				// Modified from DrawContext#drawVerticalLine
				context.fill(i, k + 1, i + 2, k + j, -1);
			}
		}

		@Override
		protected int scrollBarX() {
			return getRowLeft() + getRowWidth();
		}

		// Visible

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			if (!visible) return false;
			return super.mouseClicked(click, doubled);
		}

		@Override
		public boolean mouseReleased(MouseButtonEvent click) {
			if (!visible) return false;
			return super.mouseReleased(click);
		}

		@Override
		public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
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
		protected void renderListSeparators(GuiGraphics context) {}

		@Override
		protected void renderListBackground(GuiGraphics context) {
			context.fill(getX(), getY(), getRight(), getBottom(), 0xFF << 24);
			HudHelper.drawBorder(context, getX(), getY(), getWidth(), getHeight(), -1);
		}

		@Override
		protected void enableScissor(GuiGraphics context) {
			context.enableScissor(this.getX(), this.getY() + 1, this.getRight(), this.getBottom() - 1);
		}
	}

	private class Entry extends ContainerObjectSelectionList.Entry<Entry> {

		private final T entry;

		private Entry(T element) {
			this.entry = element;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of();
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of();
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			// drawScrollableText does some weird stuff with the y value, so we put startY = y and endY = y + 11 which makes the text render on the same line as the tick mark below (y + 2).
			renderScrollingString(context, client.font, Component.literal(entry.toString()).withStyle(Style.EMPTY.withUnderlined(hovered)), this.getX() + 10, this.getY(), this.getX() + this.getWidth(), this.getY() + 11, -1);
			if (selected == this.entry) {
				context.drawString(client.font, "✔", this.getX() + 1, this.getY() + 2, 0xFFFFFFFF);
			}
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			select(entry);
			return true;
		}
	}
}
