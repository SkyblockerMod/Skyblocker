package de.hysky.skyblocker.skyblock.waypoint;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.gui.ARGBTextInput;
import de.hysky.skyblocker.utils.render.gui.ColorPickerWidget;
import de.hysky.skyblocker.utils.render.gui.RangedSliderWidget;
import java.awt.Color;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class WaypointsOptionScreen extends Screen {
	private final Screen parent;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

	protected WaypointsOptionScreen(Screen parent) {
		super(Component.translatable("skyblocker.waypoints.waypointsOptions"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		GridLayout grid = new GridLayout().spacing(2);
		layout.addToContents(grid);
		GridLayout.RowHelper adder = grid.createRowHelper(2);
		UIAndVisualsConfig.Waypoints waypointsReadOnly = SkyblockerConfigManager.get().uiAndVisuals.waypoints;
		adder.addChild(CycleButton
				.booleanBuilder(CommonComponents.GUI_YES, CommonComponents.OPTION_OFF, waypointsReadOnly.renderLine)
				.create(Component.translatable("skyblocker.config.uiAndVisuals.waypoints.renderLine"), (button, value) -> updateConfig(waypoints -> waypoints.renderLine = value))
		);
		adder.addChild(CycleButton
				.booleanBuilder(CommonComponents.GUI_YES, CommonComponents.OPTION_OFF, waypointsReadOnly.allowSkippingWaypoints)
				.withTooltip(ignored -> Tooltip.create(Component.translatable("skyblocker.config.uiAndVisuals.waypoints.allowSkippingWaypoints.@Tooltip")))
				.create(Component.translatable("skyblocker.config.uiAndVisuals.waypoints.allowSkippingWaypoints"), (button, value) -> updateConfig(waypoints -> waypoints.allowSkippingWaypoints = value))
		);
		adder.addChild(CycleButton
				.booleanBuilder(CommonComponents.GUI_YES, CommonComponents.OPTION_OFF, waypointsReadOnly.allowGoingBackwards)
				.withTooltip(ignored -> Tooltip.create(Component.translatable("skyblocker.config.uiAndVisuals.waypoints.allowGoingBackwards.@Tooltip")))
				.create(Component.translatable("skyblocker.config.uiAndVisuals.waypoints.allowGoingBackwards"), (button, value) -> updateConfig(waypoints -> waypoints.allowGoingBackwards = value))
		);
		adder.addChild(RangedSliderWidget.builder()
				.optionFormatter(Component.translatable("skyblocker.config.uiAndVisuals.waypoints.lineWidth"), Formatters.FLOAT_NUMBERS)
				.callback(value -> updateConfig(waypoints -> waypoints.lineWidth = (float) value))
				.minMax(1, 15)
						.defaultValue(waypointsReadOnly.lineWidth)
				.step(0.5)
				.build()
		);
		adder.addChild(RangedSliderWidget.builder()
				.optionFormatter(Component.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointActivationRadius"), Formatters.FLOAT_NUMBERS)
				.callback(value -> updateConfig(waypoints -> waypoints.waypointActivationRadius = (float) value))
				.minMax(1, 10)
				.defaultValue(waypointsReadOnly.waypointActivationRadius)
				.step(0.5)
				.build()).setTooltip(Tooltip.create(Component.translatable("skyblocker.config.uiAndVisuals.waypoints.waypointActivationRadius.@Tooltip")));
		adder.addChild(SpacerElement.width(0));
		adder.addChild(new StringWidget(Component.translatable("skyblocker.config.uiAndVisuals.waypoints.lineColor"), font), 2, LayoutSettings.defaults().alignHorizontallyCenter().paddingTop(4));
		LinearLayout colorLayout = LinearLayout.horizontal();
		adder.addChild(colorLayout, 2, LayoutSettings.defaults().alignHorizontallyCenter());
		ColorPickerWidget colorPickerWidget = colorLayout.addChild(new ColorPickerWidget(0, 0, 200, 100, true));
		ARGBTextInput argbTextInput = colorLayout.addChild(new ARGBTextInput(0, 0, font, true, true));
		colorPickerWidget.setOnColorChange((color, mouseRelease) -> {
			argbTextInput.setARGBColor(color);
			if (mouseRelease) updateConfig(waypoints -> waypoints.lineColor = new Color(color, true));
		});
		argbTextInput.setOnChange(color -> {
			colorPickerWidget.setARGBColor(color);
			updateConfig(waypoints -> waypoints.lineColor = new Color(color, true));
		});
		colorPickerWidget.setARGBColor(waypointsReadOnly.lineColor.getRGB());
		argbTextInput.setARGBColor(waypointsReadOnly.lineColor.getRGB());
		layout.addToHeader(new StringWidget(getTitle(), font));
		layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).build());
		repositionElements();
		layout.visitWidgets(this::addRenderableWidget);
	}

	private void updateConfig(Consumer<UIAndVisualsConfig.Waypoints> consumer) {
		SkyblockerConfigManager.updateOnly(config -> consumer.accept(config.uiAndVisuals.waypoints));
	}

	@Override
	protected void repositionElements() {
		layout.arrangeElements();
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
		SkyblockerConfigManager.update(_ -> {});
	}
}
