package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.Multimap;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointGroup;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class WaypointsShareScreen extends AbstractWaypointsScreen<WaypointsScreen> {
	private final Set<NamedWaypoint> selectedWaypoints = new HashSet<>();

	// Import options
	private boolean overrideLocation = false;
	private boolean sortWaypoints = false;

	protected WaypointsShareScreen(WaypointsScreen parent, Multimap<Location, WaypointGroup> waypoints) {
		super(Component.translatable("skyblocker.waypoints.shareWaypoints"), parent, waypoints, parent.island);
	}

	@Override
	protected void init() {
		super.init();
		int rowSpacing = 2;
		GridLayout gridWidget = new GridLayout().columnSpacing(5).rowSpacing(rowSpacing);
		GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
		// First row
		adder.addChild(Checkbox.builder(Component.translatable("skyblocker.waypoints.importOptions.overrideLocation"), font)
				.maxWidth(Button.DEFAULT_WIDTH)
				.onValueChange((checkbox, checked) -> overrideLocation = checked)
				.tooltip(Tooltip.create(Component.translatable("skyblocker.waypoints.importOptions.overrideLocation.tooltip")))
				.build());
		adder.addChild(Checkbox.builder(Component.translatable("skyblocker.waypoints.importOptions.sortWaypoints"), font)
				.maxWidth(Button.DEFAULT_WIDTH)
				.onValueChange((checkbox, checked) -> sortWaypoints = checked)
				.tooltip(Tooltip.create(Component.translatable("skyblocker.waypoints.importOptions.sortWaypoints.tooltip")))
				.build());
		// Second Row

		adder.addChild(Button.builder(Component.translatable("skyblocker.waypoints.importWaypointsSkyblocker"), buttonImport -> {
			try {
				List<WaypointGroup> waypointGroups = Waypoints.fromSkyblocker(minecraft.keyboardHandler.getClipboard(), island);
				if (waypointGroups == null) {
					showErrorToast();
					return;
				}
				for (WaypointGroup waypointGroup : waypointGroups) {
					if (overrideLocation) waypointGroup = waypointGroup.withIsland(island);
					if (sortWaypoints) waypointGroup = waypointGroup.sortWaypoints(NamedWaypoint.NAME_COMPARATOR);
					selectedWaypoints.addAll(waypointGroup.waypoints());
					waypoints.put(waypointGroup.island(), waypointGroup);
				}
				waypointsListWidget.updateEntries();
				SystemToast.addOrUpdate(minecraft.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Component.translatable("skyblocker.waypoints.importSuccess"), Component.translatable("skyblocker.waypoints.importSuccessText", waypointGroups.stream().map(WaypointGroup::waypoints).mapToInt(List::size).sum(), waypointGroups.size()));
			} catch (Exception e) {
				Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Skyblocker waypoint data", e);
				showErrorToast();
			}
		}).tooltip(Tooltip.create(Component.translatable("skyblocker.waypoints.importWaypointsSkyblocker.tooltip"))).build());
		adder.addChild(Button.builder(Component.translatable("skyblocker.waypoints.exportWaypointsSkyblocker"), buttonExport -> {
			try {
				List<WaypointGroup> waypointGroups = waypoints.values().stream().filter(waypointGroup -> waypointGroup.island().equals(island)).map(waypointGroup -> waypointGroup.filterWaypoints(selectedWaypoints::contains)).filter(waypointGroup -> !waypointGroup.waypoints().isEmpty()).toList();
				minecraft.keyboardHandler.setClipboard(Waypoints.toSkyblocker(waypointGroups));
				SystemToast.addOrUpdate(minecraft.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Component.translatable("skyblocker.waypoints.exportSuccess"), Component.translatable("skyblocker.waypoints.exportSuccessText", waypointGroups.stream().map(WaypointGroup::waypoints).mapToInt(List::size).sum(), waypointGroups.size()));
			} catch (Exception e) {
				Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while serializing Skyblocker waypoint data", e);
				SystemToast.addOrUpdate(minecraft.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Component.translatable("skyblocker.waypoints.exportError"), Component.translatable("skyblocker.waypoints.exportErrorText"));
			}
		}).tooltip(Tooltip.create(Component.translatable("skyblocker.waypoints.exportWaypointsSkyblocker.tooltip"))).build());

		// Third row
		adder.addChild(Button.builder(Component.translatable("skyblocker.waypoints.importWaypointsSkytils"), buttonImport -> {
			try {
				List<WaypointGroup> waypointGroups = Waypoints.fromSkytils(minecraft.keyboardHandler.getClipboard(), island);
				if (waypointGroups == null) {
					showErrorToast();
					return;
				}
				for (WaypointGroup waypointGroup : waypointGroups) {
					if (overrideLocation) waypointGroup = waypointGroup.withIsland(island);
					if (sortWaypoints) waypointGroup = waypointGroup.sortWaypoints(NamedWaypoint.NAME_COMPARATOR);
					selectedWaypoints.addAll(waypointGroup.waypoints());
					waypoints.put(waypointGroup.island(), waypointGroup);
				}
				waypointsListWidget.updateEntries();
				SystemToast.addOrUpdate(minecraft.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Component.translatable("skyblocker.waypoints.importSuccess"), Component.translatable("skyblocker.waypoints.importSuccessText", waypointGroups.stream().map(WaypointGroup::waypoints).mapToInt(List::size).sum(), waypointGroups.size()));
			} catch (Exception e) {
				Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Skytils waypoint data", e);
				showErrorToast();
			}
		}).tooltip(Tooltip.create(Component.translatable("skyblocker.waypoints.importWaypointsSkytils.tooltip"))).build());
		adder.addChild(Button.builder(Component.translatable("skyblocker.waypoints.exportWaypointsSkytils"), buttonExport -> {
			try {
				List<WaypointGroup> waypointGroups = waypoints.values().stream().filter(waypointGroup -> waypointGroup.island().equals(island)).map(waypointGroup -> waypointGroup.filterWaypoints(selectedWaypoints::contains)).filter(waypointGroup -> !waypointGroup.waypoints().isEmpty()).toList();
				minecraft.keyboardHandler.setClipboard(Waypoints.toSkytilsBase64(waypointGroups));
				SystemToast.addOrUpdate(minecraft.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Component.translatable("skyblocker.waypoints.exportSuccess"), Component.translatable("skyblocker.waypoints.exportSuccessText", waypointGroups.stream().map(WaypointGroup::waypoints).mapToInt(List::size).sum(), waypointGroups.size()));
			} catch (Exception e) {
				Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while serializing Skytils waypoint data", e);
				SystemToast.addOrUpdate(minecraft.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Component.translatable("skyblocker.waypoints.exportError"), Component.translatable("skyblocker.waypoints.exportErrorText"));
			}
		}).tooltip(Tooltip.create(Component.translatable("skyblocker.waypoints.exportWaypointsSkytils.tooltip"))).build());

		// Fourth row
		adder.addChild(Button.builder(Component.translatable("skyblocker.waypoints.importWaypointsSnoopy"), buttonImport -> {
			try {
				WaypointGroup waypointGroup = Waypoints.fromColeweightJson(minecraft.keyboardHandler.getClipboard(), island);
				if (overrideLocation) waypointGroup = waypointGroup.withIsland(island);
				if (sortWaypoints) waypointGroup = waypointGroup.sortWaypoints(NamedWaypoint.NAME_COMPARATOR);
				selectedWaypoints.addAll(waypointGroup.waypoints());
				waypoints.put(waypointGroup.island(), waypointGroup);
				waypointsListWidget.updateEntries();
				SystemToast.addOrUpdate(minecraft.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Component.translatable("skyblocker.waypoints.importSuccess"), Component.translatable("skyblocker.waypoints.importSuccessText", waypointGroup.waypoints().size(), 1));
			} catch (Exception e) {
				Waypoints.LOGGER.error("[Skyblocker Waypoints] Encountered exception while parsing Snoopy waypoint data", e);
				showErrorToast();
			}
		}).tooltip(Tooltip.create(Component.translatable("skyblocker.waypoints.importWaypointsSnoopy.tooltip"))).build());
		adder.addChild(Button.builder(CommonComponents.GUI_DONE, buttonBack -> onClose()).build());
		layout.addToFooter(gridWidget);
		int rows = 4;
		layout.setFooterHeight(20 * rows + rowSpacing * (rows - 1) + 8);
		super.lateInit();
	}

	private void showErrorToast() {
		SystemToast.addOrUpdate(minecraft.getToastManager(), Waypoints.WAYPOINTS_TOAST_TYPE, Component.translatable("skyblocker.waypoints.importError"), Component.translatable("skyblocker.waypoints.importErrorText"));
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
	public void onClose() {
		minecraft.setScreen(parent);
	}
}
