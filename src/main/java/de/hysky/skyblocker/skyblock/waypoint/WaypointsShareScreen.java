package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.Multimap;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointGroup;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WaypointsShareScreen extends AbstractWaypointsScreen<WaypointsScreen> {
    private final Set<NamedWaypoint> selectedWaypoints = new HashSet<>();

    protected WaypointsShareScreen(WaypointsScreen parent, Multimap<Location, WaypointGroup> waypoints) {
        super(Text.translatable("skyblocker.waypoints.shareWaypoints"), parent, waypoints, parent.island);
    }

    @Override
    protected void init() {
        super.init();
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginY(2);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.importWaypointsSkyblocker"), buttonImport -> {
            try {
                List<WaypointGroup> waypointGroups = Waypoints.fromSkyblocker(client.keyboard.getClipboard(), island);
                for (WaypointGroup waypointGroup : waypointGroups) {
                    selectedWaypoints.addAll(waypointGroup.waypoints());
                    waypoints.put(waypointGroup.island(), waypointGroup);
                }
                waypointsListWidget.updateEntries();
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.importSuccess"), Text.translatable("skyblocker.waypoints.importSuccessText", waypointGroups.stream().map(WaypointGroup::waypoints).mapToInt(List::size).sum(), waypointGroups.size()));
            } catch (Exception e) {
                Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Skyblocker waypoint data", e);
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.importError"), Text.translatable("skyblocker.waypoints.importErrorText"));
            }
        }).tooltip(Tooltip.of(Text.translatable("skyblocker.waypoints.importWaypointsSkyblocker.tooltip"))).build());
        adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.exportWaypointsSkyblocker"), buttonExport -> {
            try {
                List<WaypointGroup> waypointGroups = waypoints.values().stream().filter(waypointGroup -> waypointGroup.island().equals(island)).map(waypointGroup -> waypointGroup.filterWaypoints(selectedWaypoints::contains)).filter(waypointGroup -> !waypointGroup.waypoints().isEmpty()).toList();
                client.keyboard.setClipboard(Waypoints.toSkyblocker(waypointGroups));
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.exportSuccess"), Text.translatable("skyblocker.waypoints.exportSuccessText", waypointGroups.stream().map(WaypointGroup::waypoints).mapToInt(List::size).sum(), waypointGroups.size()));
            } catch (Exception e) {
                Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while serializing Skyblocker waypoint data", e);
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.exportError"), Text.translatable("skyblocker.waypoints.exportErrorText"));
            }
        }).tooltip(Tooltip.of(Text.translatable("skyblocker.waypoints.exportWaypointsSkyblocker.tooltip"))).build());
        adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.importWaypointsSkytils"), buttonImport -> {
            try {
                List<WaypointGroup> waypointGroups = Waypoints.fromSkytils(client.keyboard.getClipboard(), island);
                for (WaypointGroup waypointGroup : waypointGroups) {
                    selectedWaypoints.addAll(waypointGroup.waypoints());
                    waypoints.put(waypointGroup.island(), waypointGroup);
                }
                waypointsListWidget.updateEntries();
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.importSuccess"), Text.translatable("skyblocker.waypoints.importSuccessText", waypointGroups.stream().map(WaypointGroup::waypoints).mapToInt(List::size).sum(), waypointGroups.size()));
            } catch (Exception e) {
                Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Skytils waypoint data", e);
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.importError"), Text.translatable("skyblocker.waypoints.importErrorText"));
            }
        }).tooltip(Tooltip.of(Text.translatable("skyblocker.waypoints.importWaypointsSkytils.tooltip"))).build());
        adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.exportWaypointsSkytils"), buttonExport -> {
            try {
                List<WaypointGroup> waypointGroups = waypoints.values().stream().filter(waypointGroup -> waypointGroup.island().equals(island)).map(waypointGroup -> waypointGroup.filterWaypoints(selectedWaypoints::contains)).filter(waypointGroup -> !waypointGroup.waypoints().isEmpty()).toList();
                client.keyboard.setClipboard(Waypoints.toSkytilsBase64(waypointGroups));
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.exportSuccess"), Text.translatable("skyblocker.waypoints.exportSuccessText", waypointGroups.stream().map(WaypointGroup::waypoints).mapToInt(List::size).sum(), waypointGroups.size()));
            } catch (Exception e) {
                Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while serializing Skytils waypoint data", e);
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.exportError"), Text.translatable("skyblocker.waypoints.exportErrorText"));
            }
        }).tooltip(Tooltip.of(Text.translatable("skyblocker.waypoints.exportWaypointsSkytils.tooltip"))).build());
        adder.add(ButtonWidget.builder(Text.translatable("skyblocker.waypoints.importWaypointsSnoopy"), buttonImport -> {
            try {
                WaypointGroup waypointGroup = Waypoints.fromColeweightJson(client.keyboard.getClipboard(), island);
                selectedWaypoints.addAll(waypointGroup.waypoints());
                waypoints.put(waypointGroup.island(), waypointGroup);
                waypointsListWidget.updateEntries();
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.importSuccess"), Text.translatable("skyblocker.waypoints.importSuccessText", waypointGroup.waypoints().size(), 1));
            } catch (Exception e) {
                Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Snoopy waypoint data", e);
                SystemToast.show(client.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Text.translatable("skyblocker.waypoints.importError"), Text.translatable("skyblocker.waypoints.importErrorText"));
            }
        }).tooltip(Tooltip.of(Text.translatable("skyblocker.waypoints.importWaypointsSnoopy.tooltip"))).build());
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, buttonBack -> close()).build());
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height - 76, this.width, 64);
        gridWidget.forEachChild(this::addDrawableChild);
        super.lateInit();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, Colors.WHITE);
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
