package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointCategory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Arrays;

public abstract class AbstractWaypointsScreen<T extends Screen> extends Screen {
    protected final T parent;
    protected final Multimap<String, WaypointCategory> waypoints;
    protected String island;
    protected WaypointsListWidget waypointsListWidget;
    protected DropdownWidget<Location> islandWidget;

    public AbstractWaypointsScreen(Text title, T parent) {
        this(title, parent, MultimapBuilder.hashKeys().arrayListValues().build());
    }

    public AbstractWaypointsScreen(Text title, T parent, Multimap<String, WaypointCategory> waypoints) {
        this(title, parent, waypoints, Utils.getLocationRaw());
    }

    public AbstractWaypointsScreen(Text title, T parent, Multimap<String, WaypointCategory> waypoints, String island) {
        super(title);
        this.parent = parent;
        this.waypoints = waypoints;
        this.island = island;
    }

    @Override
    protected void init() {
        super.init();
        waypointsListWidget = addDrawableChild(new WaypointsListWidget(client, this, width, height - 96, 32, 24));
        islandWidget = addDrawableChild(new DropdownWidget<>(client, width - 160, 8, 150, height - 8, Arrays.asList(Location.values()), this::islandChanged, Location.from(island)));
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
        island = location.id();
        waypointsListWidget.setIsland(island);
    }

    protected abstract boolean isEnabled(NamedWaypoint waypoint);

    protected abstract void enabledChanged(NamedWaypoint waypoint, boolean enabled);

    protected void updateButtons() {
        waypointsListWidget.updateButtons();
    }
}
