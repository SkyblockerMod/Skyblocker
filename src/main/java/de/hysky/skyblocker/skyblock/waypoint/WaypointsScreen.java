package de.hysky.skyblocker.skyblock.waypoint;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class WaypointsScreen extends AbstractWaypointsScreen<Screen> {
    private ButtonWidget buttonNew;
    private ButtonWidget buttonDone;

    public WaypointsScreen(Screen parent) {
        super(Text.translatable("skyblocker.waypoints.config"), parent, Waypoints.waypointsDeepCopy());
    }

    @Override
    protected void init() {
        super.init();
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginY(2);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.share"), buttonShare -> client.setScreen(new WaypointsShareScreen(this, waypoints))).build());
        buttonNew = adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.newGroup"), buttonNew -> waypointsListWidget.addWaypointGroupAfterSelected()).build());
        adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> close()).build());
        buttonDone = adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            saveWaypoints();
            close();
        }).build());
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
        gridWidget.forEachChild(this::addDrawableChild);
        updateButtons();
        super.lateInit();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, Colors.WHITE);
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
