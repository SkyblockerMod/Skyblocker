package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.handler.PacketUnbundler;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Consumer;

public class DropdownWidget<T> extends ContainerWidget {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static final int ENTRY_HEIGHT = 15;
    protected final List<T> entries;
    protected final Consumer<T> selectCallback;
	private final DropdownList dropdownList;
	protected T prevSelected;
    protected T selected;
    protected boolean open;

    public DropdownWidget(MinecraftClient minecraftClient, int x, int y, int width, int maxHeight, List<T> entries, Consumer<T> selectCallback, T selected) {
        super(x, y, width, Math.min((entries.size() + 1) * ENTRY_HEIGHT + 8, maxHeight), Text.empty());
        this.entries = entries;
        this.selectCallback = selectCallback;
        this.selected = selected;
		dropdownList = new DropdownList(minecraftClient, x + 1, y + ENTRY_HEIGHT + 4, width - 2, maxHeight - ENTRY_HEIGHT - 4);
		for (T element : entries) {
			dropdownList.addEntry(new Entry(element));
		}
	}

	public void setMaxHeight(int maxHeight) {
		setHeight(maxHeight);
		dropdownList.setHeight(maxHeight - ENTRY_HEIGHT - 4);
	}

	@Override
	public List<? extends Element> children() {
		return List.of(dropdownList);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(0, 0, 100);
		dropdownList.visible = open;
		dropdownList.render(context, mouseX, mouseY, delta);
		context.fill(getX(), getY(), getRight(), getY() + ENTRY_HEIGHT + 4, 0xFF << 24);
		context.drawBorder(getX(), getY(), getWidth(), ENTRY_HEIGHT + 4, -1);
		drawScrollableText(context, client.textRenderer, Text.literal(
				selected.toString()),
				getX() + 2,
				getY() + 2,
				getRight() - 2,
				getY() + ENTRY_HEIGHT + 2,
				-1);
		matrices.pop();
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	protected void select(T entry) {
		selected = entry;
		open = false;
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
		dropdownList.setY(getY() + ENTRY_HEIGHT + 4);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		dropdownList.setWidth(getWidth() - 2);
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		dropdownList.setHeight(height - ENTRY_HEIGHT - 4);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		 if (!visible) return false;
		 if (getX() <= mouseX && mouseX < getX() + getWidth() && getY() <= mouseY && mouseY < getY() + ENTRY_HEIGHT + 4) {
			 open = !open;
			 playDownSound(client.getSoundManager());
			 return true;
		 }
		 return super.mouseClicked(mouseX, mouseY, button);
	}

	private class DropdownList extends ElementListWidget<Entry> {


		public DropdownList(MinecraftClient minecraftClient, int x, int y, int width, int height) {
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
		protected void updateScrollingState(double mouseX, double mouseY, int button) {}

		@Override
		protected boolean isScrollbarVisible() {
			return !overrideScrollbarVisible && super.isScrollbarVisible();
		}

		private boolean overrideScrollbarVisible = false;

		@Override
		public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			overrideScrollbarVisible = true;
			super.renderWidget(context, mouseX, mouseY, delta);
			overrideScrollbarVisible = false;
			if (this.isScrollbarVisible()) {
				int i = this.getScrollbarX();
				int j = (int) ((float) (this.height * this.height) / (float) this.getMaxPosition());
				j = Math.clamp(j, 32, this.height - 8);
				int k = (int) this.getScrollAmount() * (this.height - j) / this.getMaxScroll() + this.getY();
				if (k < this.getY()) {
					k = this.getY();
				}

				context.drawVerticalLine(i, k, k + j, -1);
			}
		}

		@Override
		protected int getScrollbarX() {
			return getRowLeft() + getRowWidth() + 1;
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
			context.fill(getX(), getY(), getRight(), getBottom(), 0xFF << 24);
			context.drawBorder(getX(), getY(), getWidth(), getHeight(), -1);
		}

		@Override
		protected void enableScissor(DrawContext context) {
			context.enableScissor(this.getX(), this.getY() + 1, this.getRight(), this.getBottom() - 1);
		}
	}
	
	private class Entry extends ElementListWidget.Entry<Entry> {

		private final T entry;

		public Entry(T element) {
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
			drawScrollableText(context, client.textRenderer, Text.literal(entry.toString()).fillStyle(Style.EMPTY.withUnderline(hovered)), x + 10, y + 2, x + entryWidth, y + 11, -1);
			if (selected == this.entry) {
				context.drawTextWithShadow(client.textRenderer, "✔", x, y + 2, 0xFFFFFFFF);
			}
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			select(entry);
			return true;
		}
	}
}
