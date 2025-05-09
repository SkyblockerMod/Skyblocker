package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.time.Duration;

public class ItemButtonWidget extends ButtonWidget {

	private final ItemStack item;

	public ItemButtonWidget(int x, int y, int width, int height, ItemStack item, Text message, PressAction onPress, NarrationSupplier narrationSupplier) {
		super(x, y, width, height, message, onPress, narrationSupplier);
		this.item = item.copy();
		setTooltip(Tooltip.of(message));
		setTooltipDelay(Duration.ofMillis(250));
	}

	public ItemButtonWidget(int x, int y, int width, int height, ItemStack item, Text message, PressAction onPress) {
		this(x, y, width, height, item, message, onPress, DEFAULT_NARRATION_SUPPLIER);
	}

	public ItemButtonWidget(int x, int y, ItemStack item, Text message, PressAction onPress) {
		this(x, y, 20, 20, item, message, onPress, DEFAULT_NARRATION_SUPPLIER);
	}

	@Override
	public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderWidget(context, mouseX, mouseY, delta);
		context.drawItem(this.item, getX() + getWidth() / 2 - 8, getY() + getHeight() / 2 - 8);
	}
}
