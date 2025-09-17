package de.hysky.skyblocker.skyblock.waypoint;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class WaypointsScreen extends AbstractWaypointsScreen<Screen> {

	public WaypointsScreen(Screen parent) {
        super(Text.translatable("skyblocker.waypoints.config"), parent, Waypoints.waypointsDeepCopy());
    }

    @Override
    protected void init() {
        super.init();
        GridWidget gridWidget = new GridWidget().setColumnSpacing(5).setRowSpacing(2);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.share"), buttonShare -> client.setScreen(new WaypointsShareScreen(this, waypoints))).build());
		adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.newGroup"), buttonNew -> waypointsListWidget.addWaypointGroupAfterSelected()).build());
		adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> close()).build());
		adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
			saveWaypoints();
			close();
		}).build());
		layout.addFooter(gridWidget);
		layout.setFooterHeight(64);
		updateButtons();
        super.lateInit();
    }

    private void saveWaypoints() {
        Waypoints.clearAndPutAllWaypoints(waypoints);
        Waypoints.saveWaypoints(client);
    }

    @Override
    public void close() {
        assert client != null;
        if (!Waypoints.areWaypointsEqual(waypoints)) {
            client.setScreen(new ConfirmScreen(confirmedAction -> client.setScreen(confirmedAction ? parent : this),
                    Text.translatable("text.skyblocker.quit_config"),
                    Text.translatable("text.skyblocker.quit_config_sure"),
                    Text.translatable("text.skyblocker.quit_discard"),
                    ScreenTexts.CANCEL
            ));
        } else {
            client.setScreen(parent);
        }
    }
}
