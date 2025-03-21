package de.hysky.skyblocker.skyblock.item.custom.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TrimElementButton extends PressableWidget {

	private final @Nullable Identifier element;
	private final ItemStack icon;
	private final Consumer<TrimElementButton> onPress;

	public TrimElementButton(@Nullable Identifier element, ItemStack icon, Consumer<TrimElementButton> onPress) {
		super(0, 0, 20, 20, icon.getName());
		this.element = element;
		this.icon = icon;
		this.onPress = onPress;
		setTooltip(Tooltip.of(getMessage()));
	}

	public @Nullable Identifier getElement() {
		return element;
	}

	@Override
	public void setMessage(Text message) {
		super.setMessage(message);
		setTooltip(Tooltip.of(getMessage()));
	}

	@Override
	public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
		context.drawItem(icon, getX() + getWidth() / 2 - 8, getY() + getHeight() / 2 - 8);
	}

	@Override
	public void onPress() {
		onPress.accept(this);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}
}
