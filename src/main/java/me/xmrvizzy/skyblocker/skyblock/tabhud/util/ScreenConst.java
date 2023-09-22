package me.xmrvizzy.skyblocker.skyblock.tabhud.util;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class ScreenConst {
    public static final int WIDGET_PAD = 5;
    public static final int WIDGET_PAD_HALF = 3;
    private static final int SCREEN_PAD_BASE = 20;

    public static int getScreenPad() {
        return (int) ((1f / ((float) SkyblockerConfig.get().general.tabHud.tabHudScale / 100f) * SCREEN_PAD_BASE));
    }
}
