package de.hysky.skyblocker.skyblock.waypoint;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class WaypointsScreen extends Screen {
    private WaypointsListWidget waypointsListWidget;
    private final Screen parent;

    protected WaypointsScreen() {
        this(null);
    }

    public WaypointsScreen(Screen parent) {
        super(Text.translatable("skyblocker.waypoints.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        waypointsListWidget = addDrawableChild(new WaypointsListWidget(client, width, height - 96, 32, 25));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void close() {
        client.setScreen(parent);
    }
}
