package de.hysky.skyblocker.skyblock.item.custom.screen.name;

import de.hysky.skyblocker.utils.render.gui.ARGBTextInput;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import de.hysky.skyblocker.utils.render.gui.ColorPickerWidget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ColorPopup extends AbstractPopupScreen {

	private final GridLayout layout = new GridLayout();

	private final boolean gradient;
	private final GradientConsumer gradientConsumer;
	private final IntIntPair currentColor = new IntIntMutablePair(-1, -1);

	private ColorPopup(Screen backgroundScreen, GradientConsumer gradientConsumer, boolean gradient) {
		super(Component.literal("Color Popup"), backgroundScreen);
		this.gradientConsumer = gradientConsumer;
		this.gradient = gradient;
		layout.defaultCellSetting().alignHorizontallyCenter();
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
		GridLayout.RowHelper adder = layout.createRowHelper(2);
		addRenderableWidget(adder.addChild(new StringWidget(Component.translatable("skyblocker.customItemNames.screen.customColorTitle"), font), 2));
		if (gradient) {
			createLayoutGradient(adder);
		} else {
			createLayout(adder);
		}
		adder.addChild(SpacerElement.height(15), 2);
		addRenderableWidget(adder.addChild(Button.builder(Component.translatable("gui.cancel"), b -> onClose()).build(), LayoutSettings.defaults().alignHorizontallyRight().paddingRight(2)));
		addRenderableWidget(adder.addChild(Button.builder(Component.translatable("gui.done"), b -> {
			gradientConsumer.accept(currentColor.firstInt(), currentColor.secondInt());
			onClose();
		}).build(), LayoutSettings.defaults().alignHorizontallyLeft().paddingLeft(2)));
		super.init();
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		layout.arrangeElements();
		layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
	}

	private void createLayout(GridLayout.RowHelper adder) {
		ColorPickerWidget colorPicker = new ColorPickerWidget(0, 0, 200, 100);
		ARGBTextInput argb = new ARGBTextInput(0, 0, font, true, false);
		addRenderableWidget(colorPicker);
		addRenderableWidget(argb);

		argb.setOnChange(color -> {
			colorPicker.setARGBColor(color);
			currentColor.first(color);
		});
		colorPicker.setOnColorChange((color, mouseRelease) -> {
			argb.setARGBColor(color);
			currentColor.first(color);
		});

		adder.addChild(colorPicker, 2);
		adder.addChild(argb, 2);
	}

	private void createLayoutGradient(GridLayout.RowHelper adder) {
		ColorPickerWidget colorPickerStart = new ColorPickerWidget(0, 0, 200, 100);
		ARGBTextInput argbStart = new ARGBTextInput(0, 0, font, true, false);
		ColorPickerWidget colorPickerEnd = new ColorPickerWidget(0, 0, 200, 100);
		ARGBTextInput argbEnd = new ARGBTextInput(0, 0, font, true, false);
		addRenderableWidget(colorPickerStart);
		addRenderableWidget(argbStart);
		addRenderableWidget(colorPickerEnd);
		addRenderableWidget(argbEnd);

		argbStart.setOnChange(color -> {
			colorPickerStart.setARGBColor(color);
			currentColor.first(color);
		});
		colorPickerStart.setOnColorChange((color, mouseRelease) -> {
			argbStart.setARGBColor(color);
			currentColor.first(color);
		});
		argbEnd.setOnChange(color -> {
			colorPickerEnd.setARGBColor(color);
			currentColor.second(color);
		});
		colorPickerEnd.setOnColorChange((color, mouseRelease) -> {
			argbEnd.setARGBColor(color);
			currentColor.second(color);
		});

		addRenderableWidget(adder.addChild(new StringWidget(Component.translatable("skyblocker.customItemNames.screen.gradientStart"), font)));
		addRenderableWidget(adder.addChild(new StringWidget(Component.translatable("skyblocker.customItemNames.screen.gradientEnd"), font)));

		adder.addChild(colorPickerStart);
		adder.addChild(colorPickerEnd);
		adder.addChild(argbStart);
		adder.addChild(argbEnd);
	}

	@FunctionalInterface
	public interface GradientConsumer {
		void accept(int start, int end);
	}
}
