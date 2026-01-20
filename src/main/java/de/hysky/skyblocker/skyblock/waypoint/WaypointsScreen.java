package de.hysky.skyblocker.skyblock.waypoint;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class WaypointsScreen extends AbstractWaypointsScreen<Screen> {

	public WaypointsScreen(Screen parent) {
		super(Component.translatable("skyblocker.waypoints.config"), parent, Waypoints.waypointsDeepCopy());
	}

	@Override
	protected void init() {
		super.init();
		GridLayout gridWidget = new GridLayout().columnSpacing(5).rowSpacing(2);
		GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
		adder.addChild(Button.builder(Component.translatable("skyblocker.waypoints.share"), buttonShare -> minecraft.setScreen(new WaypointsShareScreen(this, waypoints))).build());
		adder.addChild(Button.builder(Component.translatable("skyblocker.waypoints.newGroup"), buttonNew -> waypointsListWidget.addWaypointGroupAfterSelected()).build());
		adder.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose()).build());
		adder.addChild(Button.builder(CommonComponents.GUI_DONE, button -> {
			saveWaypoints();
			onClose();
		}).build());
		layout.addToFooter(gridWidget);
		layout.setFooterHeight(64);
		layout.addToHeader(Button.builder(Component.translatable("skyblocker.waypoints.otherOptions"), b -> minecraft.setScreen(new WaypointsOptionScreen(this))).build(), p -> p.alignHorizontallyLeft().paddingLeft(10));
		updateButtons();
		super.lateInit();
	}

	private void saveWaypoints() {
		Waypoints.clearAndPutAllWaypoints(waypoints);
		Waypoints.saveWaypoints(minecraft);
	}

	@Override
	public void onClose() {
		assert minecraft != null;
		if (!Waypoints.areWaypointsEqual(waypoints)) {
			minecraft.setScreen(new ConfirmScreen(confirmedAction -> minecraft.setScreen(confirmedAction ? parent : this),
					Component.translatable("text.skyblocker.quit_config"),
					Component.translatable("text.skyblocker.quit_config_sure"),
					Component.translatable("text.skyblocker.quit_discard"),
					CommonComponents.GUI_CANCEL
			));
		} else {
			minecraft.setScreen(parent);
		}
	}
}
