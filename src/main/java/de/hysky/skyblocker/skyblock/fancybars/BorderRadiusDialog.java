package de.hysky.skyblocker.skyblock.fancybars;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

/**
 * A small dialog that lets the user type a border-radius value (0–20 px).
 * Opened from the right-click edit panel on any status bar.
 */
public class BorderRadiusDialog extends Screen {

        private static final int MAX_RADIUS = 20;

        private final Screen parent;
        private final int currentRadius;
        private final IntConsumer onConfirm;

        private EditBox inputBox;
        private Button confirmButton;
        private Component errorMsg = Component.empty();

        public BorderRadiusDialog(Screen parent, int currentRadius, IntConsumer onConfirm) {
                super(Component.translatable("skyblocker.bars.config.borderRadius.dialog.title"));
                this.parent = parent;
                this.currentRadius = currentRadius;
                this.onConfirm = onConfirm;
        }

        @Override
        protected void init() {
                int cx = width / 2;
                int cy = height / 2;

                inputBox = new EditBox(font, cx - 50, cy - 10, 100, 20,
                                Component.translatable("skyblocker.bars.config.borderRadius"));
                inputBox.setMaxLength(3);
                inputBox.setValue(String.valueOf(currentRadius));
                inputBox.setFilter(s -> s.isEmpty() || s.matches("\\d{0,3}"));
                inputBox.setResponder(text -> {
                        errorMsg = validate(text) == -1
                                        ? Component.translatable("skyblocker.bars.config.borderRadius.dialog.error", MAX_RADIUS)
                                        : Component.empty();
                        if (confirmButton != null) confirmButton.active = (validate(text) != -1);
                });
                addRenderableWidget(inputBox);

                confirmButton = addRenderableWidget(Button.builder(
                                Component.translatable("skyblocker.bars.config.borderRadius.dialog.confirm"),
                                btn -> confirm())
                                .bounds(cx - 52, cy + 16, 50, 16)
                                .build());

                addRenderableWidget(Button.builder(
                                Component.translatable("skyblocker.bars.config.borderRadius.dialog.cancel"),
                                btn -> minecraft.setScreen(parent))
                                .bounds(cx + 2, cy + 16, 50, 16)
                                .build());

                setFocused(inputBox);
        }

        /** Returns the clamped value, or -1 if the text is invalid. */
        private int validate(String text) {
                if (text == null || text.isBlank()) return 0;
                try {
                        int v = Integer.parseInt(text.trim());
                        if (v < 0 || v > MAX_RADIUS) return -1;
                        return v;
                } catch (NumberFormatException e) {
                        return -1;
                }
        }

        private void confirm() {
                int v = validate(inputBox.getValue());
                if (v == -1) return;
                onConfirm.accept(v);
                minecraft.setScreen(parent);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == 257 || keyCode == 335) { // ENTER or NUMPAD_ENTER
                        confirm();
                        return true;
                }
                if (keyCode == 256) { // ESCAPE
                        minecraft.setScreen(parent);
                        return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
                renderTransparentBackground(context);

                int cx = width / 2;
                int cy = height / 2;

                // Draw a simple background panel
                int panelW = 160, panelH = 80;
                context.fill(cx - panelW / 2, cy - panelH / 2, cx + panelW / 2, cy + panelH / 2, 0xCC000000);
                context.fill(cx - panelW / 2, cy - panelH / 2, cx + panelW / 2, cy - panelH / 2 + 1, 0xFF555555);
                context.fill(cx - panelW / 2, cy + panelH / 2 - 1, cx + panelW / 2, cy + panelH / 2, 0xFF555555);
                context.fill(cx - panelW / 2, cy - panelH / 2, cx - panelW / 2 + 1, cy + panelH / 2, 0xFF555555);
                context.fill(cx + panelW / 2 - 1, cy - panelH / 2, cx + panelW / 2, cy + panelH / 2, 0xFF555555);

                // Title
                context.drawCenteredString(font, getTitle(), cx, cy - panelH / 2 + 5, 0xFFFFFF);

                // Prompt
                context.drawCenteredString(font,
                                Component.translatable("skyblocker.bars.config.borderRadius.dialog.prompt", MAX_RADIUS),
                                cx, cy - 22, 0xAAAAAA);

                // Error
                if (!errorMsg.getString().isEmpty()) {
                        context.drawCenteredString(font, errorMsg, cx, cy + 36, 0xFF5555);
                }

                super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean isPauseScreen() {
                return false;
        }
}
