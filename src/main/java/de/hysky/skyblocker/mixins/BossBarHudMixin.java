package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerBossBars;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {

    @Final
    @Shadow
    private MinecraftClient client;

    @Shadow
    protected abstract void renderBossBar(DrawContext context, int x, int y, BossBar bossBar);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, CallbackInfo ci) {

        if (SkyblockerConfigManager.get().slayers.displayBossbar && SlayerBossBars.shouldRenderBossBar()) {
            ClientBossBar bar = SlayerBossBars.updateBossBar();

            int textWidth = this.client.textRenderer.getWidth(bar.getName());
            context.drawTextWithShadow(this.client.textRenderer, bar.getName(), context.getScaledWindowWidth() / 2 - textWidth / 2, 3, 16777215);

            this.renderBossBar(context, (context.getScaledWindowWidth() / 2) - 91, 12, bar);

            ci.cancel();
        }

    }
}
