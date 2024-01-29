package de.hysky.skyblocker.skyblock.waypoint;

import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class WaypointsListWidget extends ElementListWidget<WaypointsListWidget.AbstractWaypointEntry> {
    public WaypointsListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    protected static abstract class AbstractWaypointEntry extends ElementListWidget.Entry<AbstractWaypointEntry> {
    }

    protected class WaypointCategoryEntry extends AbstractWaypointEntry {
        private final WaypointCategory category;

        public WaypointCategoryEntry(WaypointCategory category) {
            this.category = category;
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
            return null;
        }

        @Override
        public List<? extends Element> children() {
            return null;
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
