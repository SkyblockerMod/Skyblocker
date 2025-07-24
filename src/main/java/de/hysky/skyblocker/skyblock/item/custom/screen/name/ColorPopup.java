package de.hysky.skyblocker.skyblock.item.custom.screen.name;


import de.hysky.skyblocker.utils.render.gui.ARGBTextInput;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import de.hysky.skyblocker.utils.render.gui.ColorPickerWidget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;

public class ColorPopup extends AbstractPopupScreen {

	private final GridWidget layout = new GridWidget();

	private final boolean gradient;
	private final GradientConsumer gradientConsumer;
	private final IntIntPair currentColor = new IntIntMutablePair(-1, -1);

	private ColorPopup(Screen backgroundScreen, GradientConsumer gradientConsumer, boolean gradient) {
		super(Text.literal("Color Popup"), backgroundScreen);
		this.gradientConsumer = gradientConsumer;
		this.gradient = gradient;
		layout.getMainPositioner().alignHorizontalCenter();
	}

	private ColorPopup(Screen backgroundScreen, IntConsumer consumer) {
		this(backgroundScreen, ((start, end) -> consumer.accept(start)), false);
	}

	public static ColorPopup create(Screen backgroundScreen, IntConsumer colorConsumer) {
		return new ColorPopup(backgroundScreen, colorConsumer);
	}

	public static ColorPopup createGradient(Screen backgroundScreen, GradientConsumer gradientConsumer) {
		return new ColorPopup(backgroundScreen, gradientConsumer, true);
	}

	@Override
	protected void init() {
		GridWidget.Adder adder = layout.createAdder(2);
		addDrawableChild(adder.add(new TextWidget(Text.translatable("skyblocker.customItemNames.screen.customColorTitle"), textRenderer), 2));
		if (gradient) {
			createLayoutGradient(adder);
		} else {
			createLayout(adder);
		}
		adder.add(EmptyWidget.ofHeight(15), 2);
		addDrawableChild(adder.add(ButtonWidget.builder(Text.translatable("gui.cancel"), b -> close()).build(), Positioner.create().alignRight().marginRight(2)));
		addDrawableChild(adder.add(ButtonWidget.builder(Text.translatable("gui.done"), b -> {
			gradientConsumer.accept(currentColor.firstInt(), currentColor.secondInt());
			close();
		}).build(), Positioner.create().alignLeft().marginLeft(2)));
		super.init();
	}

	@Override
	protected void refreshWidgetPositions() {
		super.refreshWidgetPositions();
		layout.refreshPositions();
		layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
	}

	private void createLayout(GridWidget.Adder adder) {
		ColorPickerWidget colorPicker = new ColorPickerWidget(0, 0, 200, 100);
		ARGBTextInput argb = new ARGBTextInput(0, 0, textRenderer, true, false);
		addDrawableChild(colorPicker);
		addDrawableChild(argb);

		argb.setOnChange(color -> {
			colorPicker.setRGBColor(color);
			currentColor.first(color);
		});
		colorPicker.setOnColorChange((color, mouseRelease) -> {
			argb.setARGBColor(color);
			currentColor.first(color);
		});

		adder.add(colorPicker, 2);
		adder.add(argb, 2);
	}

	private void createLayoutGradient(GridWidget.Adder adder) {
		ColorPickerWidget colorPickerStart = new ColorPickerWidget(0, 0, 200, 100);
		ARGBTextInput argbStart = new ARGBTextInput(0, 0, textRenderer, true, false);
		ColorPickerWidget colorPickerEnd = new ColorPickerWidget(0, 0, 200, 100);
		ARGBTextInput argbEnd = new ARGBTextInput(0, 0, textRenderer, true, false);
		addDrawableChild(colorPickerStart);
		addDrawableChild(argbStart);
		addDrawableChild(colorPickerEnd);
		addDrawableChild(argbEnd);

		argbStart.setOnChange(color -> {
			colorPickerStart.setRGBColor(color);
			currentColor.first(color);
		});
		colorPickerStart.setOnColorChange((color, mouseRelease) -> {
			argbStart.setARGBColor(color);
			currentColor.first(color);
		});
		argbEnd.setOnChange(color -> {
			colorPickerEnd.setRGBColor(color);
			currentColor.second(color);
		});
		colorPickerEnd.setOnColorChange((color, mouseRelease) -> {
			argbEnd.setARGBColor(color);
			currentColor.second(color);
		});

		addDrawableChild(adder.add(new TextWidget(Text.translatable("skyblocker.customItemNames.screen.gradientStart"), textRenderer)));
		addDrawableChild(adder.add(new TextWidget(Text.translatable("skyblocker.customItemNames.screen.gradientEnd"), textRenderer)));

		adder.add(colorPickerStart);
		adder.add(colorPickerEnd);
		adder.add(argbStart);
		adder.add(argbEnd);
	}

	@FunctionalInterface
	public interface GradientConsumer {
		void accept(int start, int end);
	}
}
