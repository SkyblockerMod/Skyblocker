package de.hysky.skyblocker.skyblock.waypoint;

import de.hysky.skyblocker.mixins.accessors.CheckboxWidgetAccessor;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointGroup;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WaypointsListWidget extends ElementListWidget<WaypointsListWidget.AbstractWaypointEntry> {
    private final AbstractWaypointsScreen<?> screen;
    private Location island;
    private List<WaypointGroup> waypoints;

    public WaypointsListWidget(MinecraftClient client, AbstractWaypointsScreen<?> screen, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        this.screen = screen;
        setIsland(screen.island);
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 100;
    }

    @Override
    protected int getScrollbarX() {
        return super.getScrollbarX();
    }

    Optional<WaypointGroupEntry> getGroup() {
        if (getSelectedOrNull() instanceof WaypointGroupEntry groupEntry) {
            return Optional.of(groupEntry);
        } else if (getSelectedOrNull() instanceof WaypointEntry waypointEntry) {
            return Optional.of(waypointEntry.groupEntry);
        }
        return Optional.empty();
    }

    void setIsland(Location island) {
        this.island = island;
        waypoints = (List<WaypointGroup>) screen.waypoints.get(island);
        updateEntries();
    }

    void addWaypointGroupAfterSelected() {
        WaypointGroupEntry groupEntry = new WaypointGroupEntry();
        Optional<WaypointGroupEntry> selectedGroupEntryOptional = getGroup();
        int index = waypoints.size();
        int entryIndex = children().size();
        if (selectedGroupEntryOptional.isPresent()) {
            WaypointGroupEntry selectedGroupEntry = selectedGroupEntryOptional.get();
            index = waypoints.indexOf(selectedGroupEntry.group) + 1;
            entryIndex = children().indexOf(selectedGroupEntry) + 1;
            while (entryIndex < children().size() && !(children().get(entryIndex) instanceof WaypointGroupEntry)) {
                entryIndex++;
            }
        }
        waypoints.add(index, groupEntry.group);
        children().add(entryIndex, groupEntry);
    }

    void updateEntries() {
        clearEntries();
        for (WaypointGroup group : waypoints) {
            WaypointGroupEntry groupEntry = new WaypointGroupEntry(group);
            addEntry(groupEntry);
            for (NamedWaypoint waypoint : group.waypoints()) {
                addEntry(new WaypointEntry(groupEntry, waypoint));
            }
        }
    }

    void updateButtons() {
        for (Entry<AbstractWaypointEntry> entry : children()) {
            if (entry instanceof WaypointGroupEntry groupEntry && groupEntry.enabled.isChecked() != groupEntry.shouldBeChecked()) {
                ((CheckboxWidgetAccessor) groupEntry.enabled).setChecked(!groupEntry.enabled.isChecked());
            } else if (entry instanceof WaypointEntry waypointEntry && waypointEntry.enabled.isChecked() != screen.isEnabled(waypointEntry.waypoint)) {
                waypointEntry.enabled.onPress();
            }
        }
    }

    private BlockPos getDefaultPos() {
        return client.crosshairTarget instanceof BlockHitResult blockHitResult && client.crosshairTarget.getType() == HitResult.Type.BLOCK ? blockHitResult.getBlockPos() : client.player != null ? client.player.getBlockPos() : BlockPos.ORIGIN;
    }

    protected abstract static class AbstractWaypointEntry extends ElementListWidget.Entry<AbstractWaypointEntry> {
    }

    protected class WaypointGroupEntry extends AbstractWaypointEntry {
        private WaypointGroup group;
        private final List<ClickableWidget> children;
        private final CheckboxWidget enabled;
        private final TextFieldWidget nameField;
        private final CheckboxWidget ordered;
        private final ButtonWidget buttonNewWaypoint;
        private final ButtonWidget buttonDelete;

        public WaypointGroupEntry() {
            this(new WaypointGroup("New Group", island, new ArrayList<>()));
        }

        public WaypointGroupEntry(WaypointGroup group) {
            this.group = group;
            enabled = CheckboxWidget.builder(Text.literal(""), client.textRenderer).checked(shouldBeChecked()).callback((checkbox, checked) -> group.waypoints().forEach(waypoint -> screen.enabledChanged(waypoint, checked))).build();
            nameField = new TextFieldWidget(client.textRenderer, 70, 20, Text.literal("Name"));
            nameField.setText(group.name());
            nameField.setChangedListener(this::updateName);
            ordered = CheckboxWidget.builder(Text.literal("Ordered"), client.textRenderer).checked(group.ordered()).callback((checkbox, checked) -> updateOrdered(checked)).build();
            buttonNewWaypoint = ButtonWidget.builder(Text.translatable("skyblocker.waypoints.new"), buttonNewWaypoint -> {
                WaypointEntry waypointEntry = new WaypointEntry(this);
                int entryIndex;
                if (getSelectedOrNull() instanceof WaypointEntry selectedWaypointEntry && selectedWaypointEntry.groupEntry == this) {
                    entryIndex = WaypointsListWidget.this.children().indexOf(selectedWaypointEntry) + 1;
                } else {
                    entryIndex = WaypointsListWidget.this.children().indexOf(this) + 1;
                    while (entryIndex < WaypointsListWidget.this.children().size() && !(WaypointsListWidget.this.children().get(entryIndex) instanceof WaypointGroupEntry)) {
                        entryIndex++;
                    }
                }
                group.waypoints().add(waypointEntry.waypoint);
                WaypointsListWidget.this.children().add(entryIndex, waypointEntry);
            }).width(72).build();
            buttonDelete = ButtonWidget.builder(Text.translatable("selectServer.deleteButton"), buttonDelete -> {
                int entryIndex = WaypointsListWidget.this.children().indexOf(this) + 1;
                while (entryIndex < WaypointsListWidget.this.children().size() && !(WaypointsListWidget.this.children().get(entryIndex) instanceof WaypointGroupEntry)) {
                    WaypointsListWidget.this.children().remove(entryIndex);
                }
                WaypointsListWidget.this.children().remove(this);
                waypoints.remove(group);
            }).width(38).build();
            children = List.of(enabled, nameField, ordered, buttonNewWaypoint, buttonDelete);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return children;
        }

        @Override
        public List<? extends Element> children() {
            return children;
        }

        private boolean shouldBeChecked() {
            return !group.waypoints().isEmpty() && group.waypoints().stream().allMatch(screen::isEnabled);
        }

        private void updateName(String name) {
            int index = waypoints.indexOf(group);
            group = group.withName(name);
            if (index >= 0) {
                waypoints.set(index, group);
            }
        }

        private void updateOrdered(boolean ordered) {
            int index = waypoints.indexOf(group);
            group = group.withOrdered(ordered);
            if (index >= 0) {
                waypoints.set(index, group);
            }
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            enabled.setPosition(x, y + 1);
            nameField.setPosition(x + 22, y);
            ordered.setPosition(x + entryWidth - 190, y + 1);
            buttonNewWaypoint.setPosition(x + entryWidth - 115, y);
            buttonDelete.setPosition(x + entryWidth - 38, y);
            for (ClickableWidget child : children) {
                child.render(context, mouseX, mouseY, tickDelta);
            }
        }
    }

    protected class WaypointEntry extends AbstractWaypointEntry {
        private final WaypointGroupEntry groupEntry;
        private NamedWaypoint waypoint;
        private final List<ClickableWidget> children;
        private final CheckboxWidget enabled;
        private final TextFieldWidget nameField;
        private final TextFieldWidget xField;
        private final TextFieldWidget yField;
        private final TextFieldWidget zField;
        private final TextFieldWidget colorField;
        private final ButtonWidget buttonDelete;

        public WaypointEntry(WaypointGroupEntry groupEntry) {
            this(groupEntry, groupEntry.group.createWaypoint(getDefaultPos()));
        }

        public WaypointEntry(WaypointGroupEntry groupEntry, NamedWaypoint waypoint) {
            this.groupEntry = groupEntry;
            this.waypoint = waypoint;
            enabled = CheckboxWidget.builder(Text.literal(""), client.textRenderer).checked(screen.isEnabled(waypoint)).callback((checkbox, checked) -> screen.enabledChanged(waypoint, checked)).build();
            nameField = new TextFieldWidget(client.textRenderer, 65, 20, Text.literal("Name"));
            nameField.setText(waypoint.getName().getString());
            nameField.setChangedListener(this::updateName);
            xField = new TextFieldWidget(client.textRenderer, 26, 20, Text.literal("X"));
            xField.setText(Integer.toString(waypoint.pos.getX()));
            xField.setTextPredicate(this::checkInt);
            xField.setChangedListener(this::updateX);
            yField = new TextFieldWidget(client.textRenderer, 26, 20, Text.literal("Y"));
            yField.setText(Integer.toString(waypoint.pos.getY()));
            yField.setTextPredicate(this::checkInt);
            yField.setChangedListener(this::updateY);
            zField = new TextFieldWidget(client.textRenderer, 26, 20, Text.literal("Z"));
            zField.setText(Integer.toString(waypoint.pos.getZ()));
            zField.setTextPredicate(this::checkInt);
            zField.setChangedListener(this::updateZ);
            colorField = new TextFieldWidget(client.textRenderer, 56, 20, Text.literal("Color"));
            colorField.setText(String.format("%02X%02X%02X%02X", (int) (waypoint.alpha * 255), (int) (waypoint.colorComponents[0] * 255), (int) (waypoint.colorComponents[1] * 255), (int) (waypoint.colorComponents[2] * 255)));
            colorField.setChangedListener(this::updateColor);
            buttonDelete = ButtonWidget.builder(Text.translatable("selectServer.deleteButton"), button -> {
                groupEntry.group.waypoints().remove(waypoint);
                WaypointsListWidget.this.children().remove(this);
            }).width(38).build();
            children = List.of(enabled, nameField, xField, yField, zField, colorField, buttonDelete);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return children;
        }

        @Override
        public List<? extends Element> children() {
            return children;
        }

        private void updateName(String name) {
            if (waypoint.name.getString().equals(name)) return;
            int index = groupEntry.group.waypoints().indexOf(waypoint);
            waypoint = waypoint.withName(name);
            if (index >= 0) {
                groupEntry.group.waypoints().set(index, waypoint);
            }
        }

        private boolean checkInt(String string) {
            try {
                parseEmptiableInt(string);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private void updateX(String xString) {
            updateInt(xString, waypoint.pos.getX(), waypoint::withX);
        }

        private void updateY(String yString) {
            updateInt(yString, waypoint.pos.getY(), waypoint::withY);
        }

        private void updateZ(String zString) {
            updateInt(zString, waypoint.pos.getZ(), waypoint::withZ);
        }

        private void updateInt(String newValueString, int currentValue, Int2ObjectFunction<NamedWaypoint> wither) {
            try {
                int index = groupEntry.group.waypoints().indexOf(waypoint);
                int newValue = parseEmptiableInt(newValueString);
                if (newValue == currentValue) return;
                waypoint = wither.apply(newValue);
                if (index >= 0) {
                    groupEntry.group.waypoints().set(index, waypoint);
                }
            } catch (NumberFormatException e) {
                Waypoints.LOGGER.warn("[Skyblocker Waypoints] Failed to parse integer: {}", newValueString, e);
            }
        }

        private void updateColor(String colorString) {
            try {
                int index = groupEntry.group.waypoints().indexOf(waypoint);
                int colorInt = parseEmptiableInt(colorString, 16);
                float[] colorComponents = {((colorInt & 0x00FF0000) >> 16) / 255f, ((colorInt & 0x0000FF00) >> 8) / 255f, (colorInt & 0x000000FF) / 255f};
                float alpha = ((colorInt & 0xFF000000) >>> 24) / 255f;
                if (Arrays.equals(waypoint.colorComponents, colorComponents) && waypoint.alpha == alpha) return;
                waypoint = waypoint.withColor(colorComponents, alpha);
                if (index >= 0) {
                    groupEntry.group.waypoints().set(index, waypoint);
                }
            } catch (NumberFormatException e) {
                Waypoints.LOGGER.warn("[Skyblocker Waypoints] Failed to parse color: {}", colorString, e);
            }
        }

        private int parseEmptiableInt(String value) throws NumberFormatException {
            return value.isEmpty() || value.equals("-") ? 0 : Integer.parseInt(value);
        }

        @SuppressWarnings("SameParameterValue")
        private int parseEmptiableInt(String value, int radix) throws NumberFormatException {
            return value.isEmpty() || value.equals("-") ? 0 : Integer.parseInt(value, radix);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(client.textRenderer, "X:", width / 2 - 56, y + 6, 0xFFFFFF);
            context.drawTextWithShadow(client.textRenderer, "Y:", width / 2 - 19, y + 6, 0xFFFFFF);
            context.drawTextWithShadow(client.textRenderer, "Z:", width / 2 + 18, y + 6, 0xFFFFFF);
            context.drawTextWithShadow(client.textRenderer, "#", x + entryWidth - 105, y + 6, 0xFFFFFF);
            enabled.setPosition(x + 10, y + 1);
            nameField.setPosition(x + 32, y);
            xField.setPosition(width / 2 - 48, y);
            yField.setPosition(width / 2 - 11, y);
            zField.setPosition(width / 2 + 26, y);
            colorField.setPosition(x + entryWidth - 99, y);
            buttonDelete.setPosition(x + entryWidth - 38, y);
            for (ClickableWidget child : children) {
                child.render(context, mouseX, mouseY, tickDelta);
            }
        }
    }
}
