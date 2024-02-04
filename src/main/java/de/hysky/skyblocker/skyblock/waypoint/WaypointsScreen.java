package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.hysky.skyblocker.utils.waypoint.WaypointCategory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class WaypointsScreen extends Screen {
    private final Screen parent;
    final Multimap<String, WaypointCategory> waypoints = MultimapBuilder.hashKeys().arrayListValues().build();
    private WaypointsListWidget waypointsListWidget;
    private ButtonWidget buttonNew;
    private ButtonWidget buttonDone;

    protected WaypointsScreen() {
        this(null);
    }

    public WaypointsScreen(Screen parent) {
        super(Text.translatable("skyblocker.waypoints.config"));
        this.parent = parent;
        Waypoints.waypoints.forEach((island, category) -> waypoints.put(island, new WaypointCategory(category)));
    }

    @Override
    protected void init() {
        super.init();
        waypointsListWidget = addDrawableChild(new WaypointsListWidget(client, this, width, height - 96, 32, 24));
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginY(2);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.share"), buttonShare -> {}).build());
        buttonNew = adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.newCategory"), buttonNew -> waypointsListWidget.addWaypointCategoryAfterSelected()).build());
        adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> close()).build());
        buttonDone = adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            saveWaypoints();
            close();
        }).build());
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
        gridWidget.forEachChild(this::addDrawableChild);
        updateButtons();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
    }

    private void saveWaypoints() {
        Waypoints.waypoints.clear();
        Waypoints.waypoints.putAll(waypoints);
        Waypoints.saveWaypoints(client);
    }

    private void updateButtons() {}

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void close() {
        client.setScreen(parent);
    }
}
