package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.utils.Location;
import net.minecraft.text.Text;

public class EmptyWidget extends HudWidget {
    public EmptyWidget() {
        super(Text.empty(), 0, "empty");
    }

    @Override
    public void updateContent() {}

    @Override
    public boolean shouldRender(Location location) {
        return false;
    }
}
