package de.hysky.skyblocker.skyblock.fancybars;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Paginated tips dialog for the FancyStatusBars config screen.
 * Shows one tip at a time with ← / → navigation and a Close button.
 */
public class TipsScreen extends Screen {

        private static final int DIALOG_W = 300;
        private static final int DIALOG_H = 190;
        private static final int PAD      = 10;

        private static final int COLOR_BG     = 0xFF111122;
        private static final int COLOR_BORDER = 0xFFFFFF55;
        private static final int COLOR_DIV    = 0xFF444466;
        private static final int COLOR_TITLE  = 0xFFFFFF55;
        private static final int COLOR_PAGE   = 0xFFAAAAAA;
        private static final int COLOR_TEXT   = 0xFFEEEEEE;

        private static final List<String> TIPS = List.of(
                        "LEFT-CLICK a bar to select it — a yellow outline with arrows appears. "
                        + "Hold and drag to pick it up and place it anywhere on screen.",

                        "RIGHT-CLICK any bar to open its options panel. "
                        + "You can change colors, bar height, border radius, text/icon position, "
                        + "show-max, show-overflow, and more.",

                        "Set Text or Icon position to CUSTOM in the options panel. "
                        + "Then LEFT-CLICK that text or icon element (cyan outline) to select it "
                        + "and drag it anywhere — even outside the bar.",

                        "Hold SHIFT + Arrow Keys to nudge the selected bar, text, or icon "
                        + "exactly 1 pixel at a time. Great for pixel-perfect alignment without touching the mouse.",

                        "Hold ALT + Arrow Keys to resize the selected element by 1 pixel: "
                        + "LEFT / RIGHT changes width (or text scale for Custom text), "
                        + "UP / DOWN changes height.",

                        "When a CUSTOM text or icon is selected (cyan outline), drag the white "
                        + "resize square on its right edge to scale / resize. "
                        + "Icons also have a bottom-edge handle for height.",

                        "Hover over the border between two side-by-side bars until a resize "
                        + "cursor appears, then drag to redistribute their widths.",

                        "While dragging a bar, a yellow label shows its live position. "
                        + "X = pixels from the left edge. Y = pixels from the bottom "
                        + "(Y = 0 means the bar's bottom is at the very bottom of the screen).",

                        "Use the 'Reset to Default' button to instantly restore every bar "
                        + "to its original position, size, colors, and all other settings."
        );

        private final Screen parent;
        private int currentTip = 0;
        private Button prevButton;
        private Button nextButton;

        public TipsScreen(Screen parent) {
                super(Component.literal("Tips & Tricks"));
                this.parent = parent;
        }

        private int dlgX() { return (width  - DIALOG_W) / 2; }
        private int dlgY() { return (height - DIALOG_H) / 2; }

        @Override
        protected void init() {
                super.init();
                int dx = dlgX(), dy = dlgY();
                int btnY = dy + DIALOG_H - 24;

                prevButton = addRenderableWidget(
                                Button.builder(Component.literal("←"), b -> navigate(-1))
                                                .bounds(dx + PAD, btnY, 26, 16)
                                                .build());

                addRenderableWidget(
                                Button.builder(Component.literal("Close"), b -> minecraft.setScreen(parent))
                                                .bounds(dx + DIALOG_W / 2 - 26, btnY, 52, 16)
                                                .build());

                nextButton = addRenderableWidget(
                                Button.builder(Component.literal("→"), b -> navigate(+1))
                                                .bounds(dx + DIALOG_W - PAD - 26, btnY, 26, 16)
                                                .build());

                updateNavButtons();
        }

        private void navigate(int delta) {
                currentTip = Math.floorMod(currentTip + delta, TIPS.size());
                updateNavButtons();
        }

        private void updateNavButtons() {
                boolean multi = TIPS.size() > 1;
                prevButton.active = multi;
                nextButton.active = multi;
        }

        @Override
        public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
                renderTransparentBackground(ctx);

                int dx = dlgX(), dy = dlgY();

                // Outer border (yellow, 1px)
                ctx.fill(dx - 1, dy - 1, dx + DIALOG_W + 1, dy + DIALOG_H + 1, COLOR_BORDER);
                // Background panel
                ctx.fill(dx, dy, dx + DIALOG_W, dy + DIALOG_H, COLOR_BG);

                // Title
                ctx.drawCenteredString(font, "Tips & Tricks", dx + DIALOG_W / 2, dy + PAD, COLOR_TITLE);

                // Divider under title
                ctx.fill(dx + PAD, dy + 22, dx + DIALOG_W - PAD, dy + 23, COLOR_DIV);

                // Page indicator
                String pageStr = "Tip " + (currentTip + 1) + " of " + TIPS.size();
                ctx.drawCenteredString(font, pageStr, dx + DIALOG_W / 2, dy + 27, COLOR_PAGE);

                // Tip text — word-wrapped
                List<FormattedCharSequence> lines = font.split(
                                Component.literal(TIPS.get(currentTip)), DIALOG_W - PAD * 2);
                int textY = dy + 40;
                for (FormattedCharSequence line : lines) {
                        ctx.drawString(font, line, dx + PAD, textY, COLOR_TEXT, false);
                        textY += font.lineHeight + 2;
                }

                // Render buttons on top
                super.render(ctx, mouseX, mouseY, delta);
        }

        @Override
        public boolean isPauseScreen() { return false; }

        @Override
        public boolean shouldCloseOnEsc() { return true; }

        @Override
        public void onClose() { minecraft.setScreen(parent); }
}
