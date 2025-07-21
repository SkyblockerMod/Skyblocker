package de.hysky.skyblocker.utils.render.gui;

public record ColorHighlight(int slot, int color) {
    private static final int RED_HIGHLIGHT = 64 << 24 | 255 << 16;
    private static final int YELLOW_HIGHLIGHT = 128 << 24 | 255 << 16 | 255 << 8;
    private static final int GREEN_HIGHLIGHT = 128 << 24 | 64 << 16 | 196 << 8 | 64;
    private static final int GRAY_HIGHLIGHT = 128 << 24 | 64 << 16 | 64 << 8 | 64;

    public static ColorHighlight red(int slot) {
        return new ColorHighlight(slot, RED_HIGHLIGHT);
    }

    public static ColorHighlight yellow(int slot) {
        return new ColorHighlight(slot, YELLOW_HIGHLIGHT);
    }

    public static ColorHighlight green(int slot) {
        return new ColorHighlight(slot, GREEN_HIGHLIGHT);
    }

    public static ColorHighlight gray(int slot) {
        return new ColorHighlight(slot, GRAY_HIGHLIGHT);
    }
}
