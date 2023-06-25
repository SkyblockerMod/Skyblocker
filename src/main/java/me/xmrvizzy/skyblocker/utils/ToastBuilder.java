package me.xmrvizzy.skyblocker.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
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
    public Toast.Visibility draw(DrawContext context, ToastManager manager, long startTime) {
    	TextRenderer textRenderer = manager.getClient().textRenderer;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        context.drawTexture(TEXTURE, 0, 0, 0, 0, this.getWidth(), this.getHeight());
        context.drawText(textRenderer, title, 7, 7, -11534256, false);
        context.drawText(textRenderer, description, 7, 18, -16777216, false);
        return startTime >= 3000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}
