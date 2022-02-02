package me.xmrvizzy.skyblocker.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(value= EnvType.CLIENT)
public class ToastBuilder implements Toast {
    private final Text title;
    private final Text description;

    public ToastBuilder(Text title, Text description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public Toast.Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());
        manager.getClient().textRenderer.draw(matrices, title, 7.0f, 7.0f, -11534256);
        manager.getClient().textRenderer.draw(matrices, description, 7.0f, 18.0f, -16777216);
        return startTime >= 3000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}
