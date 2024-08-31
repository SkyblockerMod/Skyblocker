package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.Multimap;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointCategory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WaypointsShareScreen extends AbstractWaypointsScreen<WaypointsScreen> {
    private final Set<NamedWaypoint> selectedWaypoints = new HashSet<>();

    protected WaypointsShareScreen(WaypointsScreen parent, Multimap<String, WaypointCategory> waypoints) {
        super(Text.translatable("skyblocker.waypoints.shareWaypoints"), parent, waypoints, parent.island);
    }

    @Override
    protected void init() {
        super.init();
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginY(2);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.importWaypointsSkytils"), buttonImport -> {
            try {
                List<WaypointCategory> waypointCategories = Waypoints.fromSkytils(client.keyboard.getClipboard(), island);
                for (WaypointCategory waypointCategory : waypointCategories) {
                    selectedWaypoints.addAll(waypointCategory.waypoints());
                    waypoints.put(waypointCategory.island(), waypointCategory);
                }
                waypointsListWidget.updateEntries();
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.importSuccess"), Text.translatable("skyblocker.waypoints.importSuccessText", waypointCategories.stream().map(WaypointCategory::waypoints).mapToInt(List::size).sum(), waypointCategories.size()));
            } catch (Exception e) {
                Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Skytils waypoint data", e);
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.importError"), Text.translatable("skyblocker.waypoints.importErrorText"));
            }
        }).tooltip(Tooltip.of(Text.translatable("skyblocker.waypoints.importWaypointsSkytils.tooltip"))).build());
        adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.importWaypointsSnoopy"), buttonImport -> {
            try {
                WaypointCategory waypointCategory = Waypoints.fromColeweightJson(client.keyboard.getClipboard(), island);
                selectedWaypoints.addAll(waypointCategory.waypoints());
                waypoints.put(waypointCategory.island(), waypointCategory);
                waypointsListWidget.updateEntries();
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.importSuccess"), Text.translatable("skyblocker.waypoints.importSuccessText", waypointCategory.waypoints().size(), 1));
            } catch (Exception e) {
                Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Snoopy waypoint data", e);
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.importError"), Text.translatable("skyblocker.waypoints.importErrorText"));
            }
        }).tooltip(Tooltip.of(Text.translatable("skyblocker.waypoints.importWaypointsSnoopy.tooltip"))).build());
        adder.add(ButtonWidget.builder(ScreenTexts.BACK, buttonBack -> close()).build());
        adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.exportWaypointsSkytils"), buttonExport -> {
            try {
                List<WaypointCategory> waypointCategories = waypoints.values().stream().filter(waypointCategory -> waypointCategory.island().equals(island)).map(waypointCategory -> waypointCategory.filterWaypoints(selectedWaypoints::contains)).filter(waypointCategory -> !waypointCategory.waypoints().isEmpty()).toList();
                client.keyboard.setClipboard(Waypoints.toSkytilsBase64(waypointCategories));
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.exportSuccess"), Text.translatable("skyblocker.waypoints.exportSuccessText", waypointCategories.stream().map(WaypointCategory::waypoints).mapToInt(List::size).sum(), waypointCategories.size()));
            } catch (Exception e) {
                Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while serializing Skytils waypoint data", e);
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.exportError"), Text.translatable("skyblocker.waypoints.exportErrorText"));
            }
        }).tooltip(Tooltip.of(Text.translatable("skyblocker.waypoints.exportWaypointsSkytils.tooltip"))).build());
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
    }

    @Override
    protected boolean isEnabled(NamedWaypoint waypoint) {
        return selectedWaypoints.contains(waypoint);
    }

    @Override
    protected void enabledChanged(NamedWaypoint waypoint, boolean enabled) {
        if (enabled) selectedWaypoints.add(waypoint);
        else selectedWaypoints.remove(waypoint);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void close() {
        client.setScreen(parent);
    }
}
