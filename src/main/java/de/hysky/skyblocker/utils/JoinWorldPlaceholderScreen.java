package de.hysky.skyblocker.utils;

import net.minecraft.text.Text;

public final class JoinWorldPlaceholderScreen extends BasePlaceholderScreen {

    private static final String SCREEN_TITLE = "Joining World Screen";

    public JoinWorldPlaceholderScreen() {
        super(Text.literal(SCREEN_TITLE));
    }
}
