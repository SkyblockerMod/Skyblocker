package de.hysky.skyblocker.utils.render.gui;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.hysky.skyblocker.utils.render.HudHelper;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphics.HoveredTextEffects;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonColors;

public class DropdownWidget<T> extends AbstractContainerWidget {
	public final int entryHeight;
	protected int headerHeight;
	protected final Minecraft client;
	protected final List<T> entries;
	protected final Consumer<T> selectCallback;
	protected final Consumer<Boolean> openedCallback;
	private final DropdownList dropdownList;
	protected T prevSelected;
	protected T selected;
	protected boolean open;
	private int maxHeight;
	protected Function<T, Component> formatter = t -> Component.literal(t.toString());


	public DropdownWidget(Minecraft minecraftClient, int x, int y, int width, int maxHeight, int entryHeight, List<T> entries, Consumer<T> selectCallback, T selected, Consumer<Boolean> openedCallback) {
		super(x, y, width, 0, Component.empty());
		this.client = minecraftClient;
		this.entryHeight = entryHeight;
		this.headerHeight = entryHeight + 4;
		this.maxHeight = maxHeight;
		this.entries = entries;
		this.selectCallback = selectCallback;
		this.openedCallback = openedCallback;
		this.selected = selected;
		dropdownList = createDropdown();
		dropdownList.setRectangle(width - 2, maxHeight - headerHeight, x + 1, y + headerHeight);
		for (T element : entries) {
			dropdownList.addEntry(createEntry(element));
		}
		setHeight(headerHeight);
	}

	public int getHeaderHeight() {
		return headerHeight;
	}

	protected DropdownList createDropdown() {
		return new DropdownList(client);
	}

	protected Entry createEntry(T element) {
		return new Entry(element);
	}

	public void setFormatter(Function<T, Component> formatter) {
		this.formatter = formatter;
	}

	public DropdownWidget(Minecraft minecraftClient, int x, int y, int width, int maxHeight, List<T> entries, Consumer<T> selectCallback, T selected, Consumer<Boolean> openedCallback) {
		this(minecraftClient, x, y, width, maxHeight, 15, entries, selectCallback, selected, openedCallback);
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
		renderHeader(context, mouseX, mouseY, delta);
		if (isMouseOver(mouseX, mouseY)) context.requestCursor(CursorTypes.POINTING_HAND);
	}

	protected void renderHeader(GuiGraphics context, int mouseX, int mouseY, float delta) {
		context.fill(getX(), getY(), getRight(), getY() + headerHeight + 1, CommonColors.BLACK);
		HudHelper.drawBorder(context, getX(), getY(), getWidth(), headerHeight + 1, CommonColors.WHITE);
		context.drawString(client.font, ">", getX() + 4, getY() + 6, CommonColors.LIGHTER_GRAY, true);
		context.drawString(client.font, selected.toString(), getX() + 12, getY() + 6, CommonColors.WHITE, true);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	private void setOpen(boolean open) {
		this.open = open;
		if (this.open) {
			setHeight(maxHeight);
			dropdownList.setHeight(Math.min(entries.size() * entryHeight + 4, maxHeight - headerHeight));
		} else {
			setHeight(headerHeight);
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
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (!visible) return false;
		if (getX() <= click.x() && click.x() < getX() + getWidth() && getY() <= click.y() && click.y() < getY() + headerHeight) {
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

	protected class DropdownList extends ContainerObjectSelectionList<Entry> {

		protected DropdownList(Minecraft minecraftClient) {
			super(minecraftClient, 0, 0, 0, entryHeight);
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

	protected class Entry extends ContainerObjectSelectionList.Entry<Entry> {
		protected final T entry;

		protected Entry(T element) {
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
			context.textRenderer(HoveredTextEffects.NONE).acceptScrollingWithDefaultCenter(Component.literal(entry.toString()).withStyle(Style.EMPTY.withUnderlined(hovered)), this.getX() + 10, this.getX() + this.getWidth(), this.getY(), this.getY() + 11);
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
