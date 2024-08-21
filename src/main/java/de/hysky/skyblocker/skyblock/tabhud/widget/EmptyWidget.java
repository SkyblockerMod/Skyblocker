package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.gui.DrawContext;

public class EmptyWidget extends HudWidget {
    public EmptyWidget() {
        super("");
    }

    @Override
    public boolean shouldRender(Location location) {
        return false;
    }

    @Override
    public void update() {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {}
}