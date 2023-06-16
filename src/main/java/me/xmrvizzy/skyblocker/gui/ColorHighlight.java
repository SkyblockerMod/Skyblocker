package me.xmrvizzy.skyblocker.gui;

public record ColorHighlight(int slot, int color) {
    public static ColorHighlight red(int slot) {
        return new ColorHighlight(slot, ContainerSolver.RED_HIGHLIGHT);
    }

    public static ColorHighlight yellow(int slot) {
        return new ColorHighlight(slot, ContainerSolver.YELLOW_HIGHLIGHT);
    }

    public static ColorHighlight green(int slot) {
        return new ColorHighlight(slot, ContainerSolver.GREEN_HIGHLIGHT);
    }

    public static ColorHighlight gray(int slot) {
        return new ColorHighlight(slot, ContainerSolver.GRAY_HIGHLIGHT);
    }
}