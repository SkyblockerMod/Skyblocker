package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.utils.render.gui.ARGBTextInput;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import de.hysky.skyblocker.utils.render.gui.ColorPickerWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.awt.Color;
import java.util.function.Consumer;

public class EditBarColorPopup extends AbstractPopupScreen {

	private final Consumer<Color> setColor;
	private int currentColor = -1;

	private LinearLayout layout = LinearLayout.vertical();

	protected EditBarColorPopup(Component title, Screen backgroundScreen, Consumer<Color> setColor) {
		super(title, backgroundScreen);
		this.setColor = setColor;
	}

	@Override
	protected void init() {
		super.init();
		layout = LinearLayout.vertical();
		layout.spacing(8).defaultCellSetting().alignHorizontallyCenter();
		layout.addChild(new StringWidget(title.copy().withStyle(Style.EMPTY.withBold(true)), Minecraft.getInstance().font));

		LinearLayout colorLayout = layout.addChild(LinearLayout.horizontal().spacing(4));
		ColorPickerWidget colorPicker = new ColorPickerWidget(0, 0, 200, 100);
		ARGBTextInput argb = new ARGBTextInput(0, 0, font, true, false);

		argb.setOnChange(color -> {
			colorPicker.setARGBColor(color);
			currentColor = color;
		});
		colorPicker.setOnColorChange((color, _) -> {
			argb.setARGBColor(color);
			currentColor = color;
		});

		colorLayout.addChild(colorPicker);
		colorLayout.addChild(argb);

		LinearLayout horizontal = LinearLayout.horizontal();
		Button buttonWidget = Button.builder(Component.literal("Cancel"), _ -> onClose()).width(80).build();
		horizontal.addChild(buttonWidget);
		horizontal.addChild(Button.builder(Component.literal("Done"), _ -> {
			setColor.accept(new Color(currentColor));
			onClose();
		}).width(80).build());

		layout.addChild(horizontal);
		layout.visitWidgets(this::addRenderableWidget);
		this.layout.arrangeElements();
		FrameLayout.centerInRectangle(layout, this.getRectangle());
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		extractPopupBackground(graphics, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
	}
}
