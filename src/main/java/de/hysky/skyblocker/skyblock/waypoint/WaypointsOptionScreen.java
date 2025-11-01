package de.hysky.skyblocker.skyblock.waypoint;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.gui.ARGBTextInput;
import de.hysky.skyblocker.utils.render.gui.ColorPickerWidget;
import de.hysky.skyblocker.utils.render.gui.RangedSliderWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class WaypointsOptionScreen extends Screen {
	private static final UIAndVisualsConfig.Waypoints WAYPOINTS = SkyblockerConfigManager.get().uiAndVisuals.waypoints;

	private final @NotNull Screen parent;
	private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

	protected WaypointsOptionScreen(@NotNull Screen parent) {
		super(Text.literal("Waypoints Options"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		GridWidget grid = new GridWidget().setSpacing(2);
		layout.addBody(grid);
		GridWidget.Adder adder = grid.createAdder(2);
		adder.add(CyclingButtonWidget
				.onOffBuilder(ScreenTexts.YES, ScreenTexts.OFF)
						.initially(WAYPOINTS.renderLine)
				.build(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.renderLine"), (button, value) -> WAYPOINTS.renderLine = value)
		);
		adder.add(CyclingButtonWidget
				.onOffBuilder(ScreenTexts.YES, ScreenTexts.OFF)
						.initially(WAYPOINTS.allowSkippingWaypoints)
				.tooltip(ignored -> Tooltip.of(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.allowSkippingWaypoints.@Tooltip")))
				.build(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.allowSkippingWaypoints"), (button, value) -> WAYPOINTS.allowSkippingWaypoints = value)
		);
		adder.add(CyclingButtonWidget
				.onOffBuilder(ScreenTexts.YES, ScreenTexts.OFF)
				.initially(WAYPOINTS.allowGoingBackwards)
				.tooltip(ignored -> Tooltip.of(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.allowGoingBackwards.@Tooltip")))
				.build(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.allowGoingBackwards"), (button, value) -> WAYPOINTS.allowGoingBackwards = value)
		);
		adder.add(RangedSliderWidget.builder()
				.optionFormatter(Text.translatable("skyblocker.config.uiAndVisuals.waypoints.lineWidth"), Formatters.FLOAT_NUMBERS)
				.callback(value -> WAYPOINTS.lineWidth = (float) value)
				.minMax(1, 15)
						.defaultValue(WAYPOINTS.lineWidth)
				.step(0.5)
				.build()
		);
		DirectionalLayoutWidget colorLayout = DirectionalLayoutWidget.horizontal();
		adder.add(colorLayout, 2, Positioner.create().alignHorizontalCenter());
		ColorPickerWidget colorPickerWidget = colorLayout.add(new ColorPickerWidget(0, 0, 200, 100, true));
		ARGBTextInput argbTextInput = colorLayout.add(new ARGBTextInput(0, 0, textRenderer, true, true));
		colorPickerWidget.setOnColorChange((color, mouseRelease) -> {
			argbTextInput.setARGBColor(color);
			if (mouseRelease) WAYPOINTS.lineColor = new Color(color, true);
		});
		argbTextInput.setOnChange(color -> {
			colorPickerWidget.setARGBColor(color);
			WAYPOINTS.lineColor = new Color(color, true);
		});
		colorPickerWidget.setARGBColor(WAYPOINTS.lineColor.getRGB());
		argbTextInput.setARGBColor(WAYPOINTS.lineColor.getRGB());
		layout.addHeader(new TextWidget(getTitle(), textRenderer));
		layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, b -> close()).build());
		refreshWidgetPositions();
		layout.forEachChild(this::addDrawableChild);
	}

	@Override
	protected void refreshWidgetPositions() {
		layout.refreshPositions();
	}

	@Override
	public void close() {
		client.setScreen(parent);
		SkyblockerConfigManager.update(c -> {});
	}
}
