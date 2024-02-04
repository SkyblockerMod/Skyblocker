package de.hysky.skyblocker.skyblock.waypoint;

import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WaypointsListWidget extends ElementListWidget<WaypointsListWidget.AbstractWaypointEntry> {
    private final WaypointsScreen screen;
    private final String island;
    private final List<WaypointCategory> waypoints;

    public WaypointsListWidget(MinecraftClient client, WaypointsScreen screen, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        this.screen = screen;
        island = Utils.getLocationRaw();
        waypoints = (List<WaypointCategory>) screen.waypoints.get(island);
        for (WaypointCategory category : waypoints) {
            WaypointCategoryEntry categoryEntry = new WaypointCategoryEntry(category);
            addEntry(categoryEntry);
            for (NamedWaypoint waypoint : category.waypoints()) {
                addEntry(new WaypointEntry(categoryEntry, waypoint));
            }
        }
    }

    Optional<WaypointCategoryEntry> getCategory() {
        if (getSelectedOrNull() instanceof WaypointCategoryEntry category) {
            return Optional.of(category);
        } else if (getSelectedOrNull() instanceof WaypointEntry waypointEntry) {
            return Optional.of(waypointEntry.category);
        }
        return Optional.empty();
    }

    void addWaypointCategoryAfterSelected() {
        WaypointCategoryEntry categoryEntry = new WaypointCategoryEntry();
        Optional<WaypointCategoryEntry> selectedCategoryEntryOptional = getCategory();
        int index = waypoints.size();
        int entryIndex = children().size();
        if (selectedCategoryEntryOptional.isPresent()) {
            WaypointCategoryEntry selectedCategoryEntry = selectedCategoryEntryOptional.get();
            index = waypoints.indexOf(selectedCategoryEntry.category) + 1;
            entryIndex = children().indexOf(selectedCategoryEntry) + 1;
            while (entryIndex < children().size() && !(children().get(entryIndex) instanceof WaypointCategoryEntry)) {
                entryIndex++;
            }
        }
        waypoints.add(index, categoryEntry.category);
        children().add(entryIndex, categoryEntry);
    }

    @Override
    protected boolean isSelectedEntry(int index) {
        return Objects.equals(getSelectedOrNull(), children().get(index));
    }

    protected static abstract class AbstractWaypointEntry extends ElementListWidget.Entry<AbstractWaypointEntry> {
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            super.mouseClicked(mouseX, mouseY, button);
            return true;
        }
    }

    protected class WaypointCategoryEntry extends AbstractWaypointEntry {
        private final WaypointCategory category;
        private final List<ClickableWidget> children;
        private final ButtonWidget buttonNewWaypoint;

        public WaypointCategoryEntry() {
            this(new WaypointCategory("New Category", island, new ArrayList<>()));
        }

        public WaypointCategoryEntry(WaypointCategory category) {
            this.category = category;
            buttonNewWaypoint = ButtonWidget.builder(Text.translatable("skyblocker.waypoints.new"), buttonNewWaypoint -> {
                WaypointEntry waypointEntry = new WaypointEntry(this);
                int entryIndex;
                if (getSelectedOrNull() instanceof WaypointEntry selectedWaypointEntry && selectedWaypointEntry.category == this) {
                    entryIndex = WaypointsListWidget.this.children().indexOf(selectedWaypointEntry) + 1;
                } else {
                    entryIndex = WaypointsListWidget.this.children().indexOf(this) + 1;
                    while (entryIndex < children().size() && !(children().get(entryIndex) instanceof WaypointCategoryEntry)) {
                        entryIndex++;
                    }
                }
                category.waypoints().add(waypointEntry.waypoint);
                WaypointsListWidget.this.children().add(entryIndex, waypointEntry);
            }).width(100).build();
            children = List.of(buttonNewWaypoint);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return children;
        }

        @Override
        public List<? extends Element> children() {
            return children;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            buttonNewWaypoint.setPosition(x + entryWidth - 30, y + 6);
            buttonNewWaypoint.render(context, mouseX, mouseY, tickDelta);
            context.drawTextWithShadow(client.textRenderer, category.name(), width / 2 - 150, y + 5, 0xFFFFFF);
        }
    }

    protected class WaypointEntry extends AbstractWaypointEntry {
        private final WaypointCategoryEntry category;
        private final NamedWaypoint waypoint;

        public WaypointEntry(WaypointCategoryEntry category) {
            this(category, new NamedWaypoint(BlockPos.ORIGIN, "New Waypoint", new float[]{0f, 1f, 0f}));
        }

        public WaypointEntry(WaypointCategoryEntry category, NamedWaypoint waypoint) {
            this.category = category;
            this.waypoint = waypoint;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(client.textRenderer, waypoint.getName(), width / 2 - 125, y + 5, 0xFFFFFF);
            context.drawTextWithShadow(client.textRenderer, waypoint.pos.toString(), width / 2 - 50, y + 5, 0xFFFFFF);
            float[] colorComponents = waypoint.getColorComponents();
            context.drawTextWithShadow(client.textRenderer, String.format("#%02X%02X%02X", (int) (colorComponents[0] * 255), (int) (colorComponents[1] * 255), (int) (colorComponents[2] * 255)), width / 2 + 10, y + 5, 0xFFFFFF);
        }
    }
}
