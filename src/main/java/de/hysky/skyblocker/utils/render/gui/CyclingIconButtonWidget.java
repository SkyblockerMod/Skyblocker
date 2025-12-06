package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;

import java.util.function.Consumer;
import java.util.function.Function;

public class CyclingIconButtonWidget<T> extends PressableWidget {
	private final Function<T, Icon> valueToIcon;
	private final boolean showText;
	private int index;
	private final Function<T, Text> valueToText;
	private final T[] values;
	private final Consumer<T> callback;
	private final Function<T, Tooltip> tooltipFactory;

	private Icon currentIcon;

	public CyclingIconButtonWidget(int width, int height, T value, T[] values, Function<T, Text> valueToText, Function<T, Icon> valueToIcon, Function<T, Tooltip> tooltipFactory, Consumer<T> callback, boolean showText) {
		super(0, 0, width, height, valueToText.apply(value));
		this.index = ArrayUtils.indexOf(values, value);
		assert index >= 0;
		this.values = values;
		this.valueToText = valueToText;
		this.valueToIcon = valueToIcon;
		this.callback = callback;
		this.tooltipFactory = tooltipFactory;
		this.showText = showText;

		this.currentIcon = valueToIcon.apply(value);
		setTooltip(tooltipFactory.apply(value));
	}

	public CyclingIconButtonWidget(int width, int height, T value, T[] values, Function<T, Icon> valueToIcon,  Function<T, Tooltip> tooltipFactory, Consumer<T> callback) {
		this(width, height, value, values, o -> Text.literal(String.valueOf(o)), valueToIcon, tooltipFactory, callback, false);
	}

	@Override
	public void onPress(AbstractInput input) {
		index = (index + 1) % values.length;
		T newValue = values[index];
		callback.accept(newValue);
		setMessage(valueToText.apply(newValue));
		currentIcon = valueToIcon.apply(newValue);
		setTooltip(tooltipFactory.apply(newValue));
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.renderWidget(context, mouseX, mouseY, deltaTicks);
		int x = this.showText ? (this.getX() + this.getWidth() - this.currentIcon.width() - 2) : (this.getX() + this.getWidth() / 2 - this.currentIcon.width() / 2);
		int y = this.getY() + this.getHeight() / 2 - this.currentIcon.height() / 2;
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.currentIcon.id(), x, y, this.currentIcon.width(), this.currentIcon.height(), this.alpha);
	}

	@Override
	public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
		if (!showText) return;
		int x1 = this.getX() + 2;
		int xCenter = this.getX() + this.getWidth() - this.currentIcon.width() - 4;
		int x2 = this.getX() + this.getWidth() / 2;
		drawScrollableText(context, textRenderer, this.getMessage(), x2, x1, this.getY(), xCenter, this.getY() + this.getHeight(), color);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	public record Icon(Identifier id, int width, int height) {}
}
