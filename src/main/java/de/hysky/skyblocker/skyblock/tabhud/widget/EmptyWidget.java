package de.hysky.skyblocker.skyblock.tabhud.widget;

import net.minecraft.text.Text;

public class EmptyWidget extends HudWidget {
    public EmptyWidget() {
        super(Text.empty(), 0);
    }

    @Override
    public void updateContent() {}
}
