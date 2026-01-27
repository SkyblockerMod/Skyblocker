package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;

public class AddWidgetWidget extends AbstractSelectionList<AddWidgetWidget.Entry> {

	private final Consumer<HudWidget> widgetConsumer;
	private static final int MAX_ENTRIES = 10;

	public AddWidgetWidget(Minecraft client, Consumer<HudWidget> widgetConsumer) {
		super(client, 10, 10, 0, 12);
		this.widgetConsumer = widgetConsumer;
		visible = false;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	@Override
	protected void renderListBackground(GuiGraphics context) {
		context.fill(getX(), getY(), getRight(), getBottom(), ARGB.color(100, 0));
		if (scrollAmount() > 0) {
			for (int x = 0; x < this.getWidth(); x++) {
				if (x % 2 == 0) {
					context.fill(this.getX() + x, this.getY() - 1, this.getX() + x + 1, this.getY(), -1);
				}
			}
		}

		if (scrollAmount() < maxScrollAmount()) {
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
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		super.renderWidget(context, mouseX, mouseY, deltaTicks);
		if (mouseX < getX() - 20 || mouseY < getY() - 20 || mouseX > getRight() + 20 || mouseY > getBottom() + 20) visible = false;
	}

	@Override
	protected void renderListSeparators(GuiGraphics context) {}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (!visible) return false;
		return super.mouseClicked(click, doubled);
	}

	public void openWith(List<HudWidget> widgets) {
		visible = true;
		replaceEntries(widgets.stream().sorted(Comparator.comparing(w -> w.getInformation().displayName().getString())).map(de.hysky.skyblocker.skyblock.tabhud.config.AddWidgetWidget.Entry::new).toList());
		setHeight(Math.min(widgets.size(), MAX_ENTRIES) * defaultEntryHeight);
		setWidth(widgets.stream().mapToInt(entry -> minecraft.font.width(entry.getInformation().displayName())).max().orElse(100) + 3);
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
	protected void renderScrollbar(GuiGraphics context, int mouseX, int mouseY) {
		if (this.scrollbarVisible()) {
			int x = this.scrollBarX();
			int y = this.scrollBarY();
			int h = this.scrollerHeight();
			context.fill(x, y, x + 2, y + h, CommonColors.WHITE);
		}
	}

	@Override
	protected int scrollBarX() {
		return getRight() - 2;
	}

	protected class Entry extends AbstractSelectionList.Entry<de.hysky.skyblocker.skyblock.tabhud.config.AddWidgetWidget.Entry> {

		private final HudWidget hudWidget;

		private Entry(HudWidget widget) {
			this.hudWidget = widget;
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
			if (hovered) {
				context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), ARGB.white(0.1f));
			}
			context.drawString(minecraft.font, hudWidget.getInformation().displayName(), getX(), getY() + 1, -1, false);
			//ClickableWidget.drawScrollableText(context, client.textRenderer, hudWidget.getInformation().displayName(), x, y, x + entryWidth, y + entryHeight, Colors.WHITE);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			widgetConsumer.accept(hudWidget);
			visible = false;
			return true;
		}
	}
}
