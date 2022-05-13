package me.xmrvizzy.skyblocker.skyblock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public class ToggleHud {
    static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void render(MatrixStack matrices) {
        final int height = mc.getWindow().getScaledHeight();
        final int widthcenter = mc.getWindow().getScaledWidth() / 2;


        if (mc.options.sprintKey.isPressed() && mc.options.sprintToggled) {
            mc.textRenderer.drawWithShadow(matrices, "✦", widthcenter - 102F, height-13F, 16777215);
        } else if (mc.options.sprintKey.isPressed() && !mc.options.sprintToggled) {
            mc.textRenderer.drawWithShadow(matrices, "✧", widthcenter - 102F, height-13F, 16777215);
        }
    }
}
