package de.hysky.skyblocker.utils.render.gui;

import org.apache.commons.lang3.ArrayUtils;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphics.HoveredTextEffects;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class CyclingIconButtonWidget<T> extends AbstractButton {
	private final Function<T, Icon> valueToIcon;
	private final boolean showText;
	private int index;
	private final Function<T, Component> valueToText;
	private final T[] values;
	private final Consumer<T> callback;
	private final Function<T, Tooltip> tooltipFactory;

	private Icon currentIcon;

	public CyclingIconButtonWidget(int width, int height, T value, T[] values, Function<T, Component> valueToText, Function<T, Icon> valueToIcon, Function<T, Tooltip> tooltipFactory, Consumer<T> callback, boolean showText) {
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
		this(width, height, value, values, o -> Component.literal(String.valueOf(o)), valueToIcon, tooltipFactory, callback, false);
	}

	@Override
	public void onPress(InputWithModifiers input) {
		index = (index + 1) % values.length;
		T newValue = values[index];
		callback.accept(newValue);
		setMessage(valueToText.apply(newValue));
		currentIcon = valueToIcon.apply(newValue);
		setTooltip(tooltipFactory.apply(newValue));
	}

	@Override
	protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		this.renderDefaultSprite(context);
		int x = this.showText ? (this.getX() + this.getWidth() - this.currentIcon.width() - 2) : (this.getX() + this.getWidth() / 2 - this.currentIcon.width() / 2);
		int y = this.getY() + this.getHeight() / 2 - this.currentIcon.height() / 2;
		context.blitSprite(RenderPipelines.GUI_TEXTURED, this.currentIcon.id(), x, y, this.currentIcon.width(), this.currentIcon.height(), this.alpha);
		this.renderDefaultLabel(context.textRenderer(HoveredTextEffects.NONE));
	}

	@Override
	public void renderDefaultLabel(ActiveTextCollector drawer) {
		if (!showText) return;
		int x1 = this.getX() + 2;
		int x2 = this.getX() + this.getWidth() - this.currentIcon.width() - 4;
		int xCenter = this.getX() + this.getWidth() / 2;
		drawer.acceptScrolling(this.getMessage(), xCenter, x1, x2, this.getY(), this.getY() + this.getHeight());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	public record Icon(Identifier id, int width, int height) {}
}
