package de.hysky.skyblocker.utils.render.gui;

import java.time.Duration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemButtonWidget extends Button {

	private final ItemStack item;

	public ItemButtonWidget(int x, int y, int width, int height, ItemStack item, Component message, OnPress onPress, CreateNarration narrationSupplier) {
		super(x, y, width, height, message, onPress, narrationSupplier);
		this.item = item.copy();
		setTooltip(Tooltip.create(message));
		setTooltipDelay(Duration.ofMillis(250));
	}

	public ItemButtonWidget(int x, int y, int width, int height, ItemStack item, Component message, OnPress onPress) {
		this(x, y, width, height, item, message, onPress, DEFAULT_NARRATION);
	}

	public ItemButtonWidget(int x, int y, ItemStack item, Component message, OnPress onPress) {
		this(x, y, 20, 20, item, message, onPress, DEFAULT_NARRATION);
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		this.extractDefaultSprite(graphics);
		graphics.item(this.item, getX() + getWidth() / 2 - 8, getY() + getHeight() / 2 - 8);
	}
}
