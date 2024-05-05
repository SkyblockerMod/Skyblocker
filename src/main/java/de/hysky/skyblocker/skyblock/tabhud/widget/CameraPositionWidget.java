package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class CameraPositionWidget extends HudWidget {
    private static final MutableText TITLE = Text.literal("Camera Pos").formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    public CameraPositionWidget() {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
    }

    @Override
    public void updateContent() {
        double yaw = CLIENT.getCameraEntity().getYaw();
        double pitch = CLIENT.getCameraEntity().getPitch();

        addComponent(new PlainTextComponent(Text.literal("Yaw: " + String.format("%.3f", MathHelper.wrapDegrees(yaw)))));
        addComponent(new PlainTextComponent(Text.literal("Pitch: " + String.format("%.3f", MathHelper.wrapDegrees(pitch)))));
    }
}
