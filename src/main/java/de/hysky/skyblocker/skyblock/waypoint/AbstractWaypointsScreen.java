package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.DropdownWidget;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointGroup;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Arrays;

public abstract class AbstractWaypointsScreen<T extends Screen> extends Screen {
    protected final T parent;
    protected final Multimap<Location, WaypointGroup> waypoints;
    protected Location island;
    protected WaypointsListWidget waypointsListWidget;
    protected DropdownWidget<Location> islandWidget;

    public AbstractWaypointsScreen(Text title, T parent) {
        this(title, parent, MultimapBuilder.enumKeys(Location.class).arrayListValues().build());
    }

    public AbstractWaypointsScreen(Text title, T parent, Multimap<Location, WaypointGroup> waypoints) {
        this(title, parent, waypoints, Utils.getLocation());
    }

    public AbstractWaypointsScreen(Text title, T parent, Multimap<Location, WaypointGroup> waypoints, Location island) {
        super(title);
        this.parent = parent;
        this.waypoints = waypoints;
        this.island = island;
    }

    @Override
    protected void init() {
        super.init();
        waypointsListWidget = addDrawableChild(new WaypointsListWidget(client, this, width, height - 120, 32, 24));
    }

    /**
     * This should be called at the end of the implementation's init to ensure that these elements render last.
     */
    protected final void lateInit() {
    	islandWidget = addDrawableChild(new DropdownWidget<>(client, width - 160, 8, 150, height - 8, Arrays.asList(Location.values()), this::islandChanged, island));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (islandWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        boolean mouseClicked = super.mouseClicked(mouseX, mouseY, button);
        updateButtons();
        return mouseClicked;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (islandWidget.isMouseOver(mouseX, mouseY) && islandWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    protected void islandChanged(Location location) {
        island = location;
        waypointsListWidget.setIsland(island);
    }

    /**
     * Gets whether the waypoint is enabled in the current screen.
     * Override for custom behavior such as using the checkbox for whether it should be included in the exported waypoints.
     *
     * @return whether the waypoint is enabled in the current screen
     */
    protected boolean isEnabled(NamedWaypoint waypoint) {
        return waypoint.isEnabled();
    }

    /**
     * Called when the enabled state of a waypoint checkbox changes.
     * Override for custom behavior such as updating whether the waypoint should be included in the exported waypoints.
     */
    protected void enabledChanged(NamedWaypoint waypoint, boolean enabled) {
        waypoint.setEnabled(enabled);
    }

    protected void updateButtons() {
        waypointsListWidget.updateButtons();
    }
}
