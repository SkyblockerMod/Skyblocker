package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class AddWidgetWidget extends EntryListWidget<AddWidgetWidget.Entry> {

	private final Consumer<HudWidget> widgetConsumer;
	private static final int MAX_ENTRIES = 10;

	public AddWidgetWidget(MinecraftClient client, Consumer<HudWidget> widgetConsumer) {
		super(client, 10, 10, 0, 12);
		this.widgetConsumer = widgetConsumer;
		visible = false;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	@Override
	protected void drawMenuListBackground(DrawContext context) {
		context.fill(getX(), getY(), getRight(), getBottom(), ColorHelper.withAlpha(100, 0));
		if (getScrollY() > 0) {
			for (int x = 0; x < this.getWidth(); x++) {
				if (x % 2 == 0) {
					context.fill(this.getX() + x, this.getY() - 1, this.getX() + x + 1, this.getY(), -1);
				}
			}
		}

		if (getScrollY() < getMaxScrollY()) {
			for (int x = 0; x < this.getWidth(); x++) {
				if (x % 2 == 0) {
					context.fill(
							this.getX() + x, this.getY() + this.getHeight(), this.getX() + x + 1, this.getY() + this.getHeight() + 1, -1
					);
				}
			}
		}
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.renderWidget(context, mouseX, mouseY, deltaTicks);
		if (mouseX < getX() - 20 || mouseY < getY() - 20 || mouseX > getRight() + 20 || mouseY > getBottom() + 20) visible = false;
	}

	@Override
	protected void drawHeaderAndFooterSeparators(DrawContext context) {}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (!visible) return false;
		return super.mouseClicked(click, doubled);
	}

	public void openWith(List<HudWidget> widgets) {
		visible = true;
		replaceEntries(widgets.stream().sorted(Comparator.comparing(w -> w.getInformation().displayName().getString())).map(Entry::new).toList());
		setHeight(Math.min(widgets.size(), MAX_ENTRIES) * itemHeight);
		setWidth(widgets.stream().mapToInt(entry -> client.textRenderer.getWidth(entry.getInformation().displayName())).max().orElse(100) + 3);
	}

	@Override
	public void setX(int x) {
		super.setX(x);
	}

	@Override
	public int getRowLeft() {
		return getX();
	}

	@Override
	public int getRowWidth() {
		return width - 2;
	}

	@Override
	protected void drawScrollbar(DrawContext context, int mouseX, int mouseY) {
		if (this.overflows()) {
			int x = this.getScrollbarX();
			int y = this.getScrollbarThumbY();
			int h = this.getScrollbarThumbHeight();
			context.fill(x, y, x + 2, y + h, Colors.WHITE);
		}
	}

	@Override
	protected int getScrollbarX() {
		return getRight() - 2;
	}

	protected class Entry extends EntryListWidget.Entry<Entry> {

		private final HudWidget hudWidget;

		private Entry(HudWidget widget) {
			this.hudWidget = widget;
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
			if (hovered) {
				context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), ColorHelper.getWhite(0.1f));
			}
			context.drawText(client.textRenderer, hudWidget.getInformation().displayName(), getX(), getY() + 1, -1, false);
			//ClickableWidget.drawScrollableText(context, client.textRenderer, hudWidget.getInformation().displayName(), x, y, x + entryWidth, y + entryHeight, Colors.WHITE);
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
			widgetConsumer.accept(hudWidget);
			visible = false;
			return true;
		}
	}
}
