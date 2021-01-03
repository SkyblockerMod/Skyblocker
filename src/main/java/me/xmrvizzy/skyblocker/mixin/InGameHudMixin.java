package me.xmrvizzy.skyblocker.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
    private static final Identifier ICONS = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/icons.png");

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private int scaledHeight;
    @Shadow
    private int scaledWidth;

    @Shadow
    public abstract TextRenderer getFontRenderer();

    private String hpColor = "§c";
    private int hpCurrent = 0;
    private int hpMax = 0;
    private int manaCurrent = 0;
    private int manaMax = 0;

    @ModifyVariable(method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"))
    private Text setOverlayMessage(Text message) {
        if (Utils.isSkyblock() && SkyblockerConfig.get().bars.enableBars) {
            String actionBar = message.getString();

            if (actionBar != null) {
                if (actionBar.contains("❤")) {
                    String[] health = actionBar.split("❤")[0]
                            .split("/");
                    hpColor = health[0].substring(0, 2);
                    hpCurrent = Integer.parseInt(health[0].replaceAll(hpColor, ""));
                    hpMax = Integer.parseInt(health[1]);
                }
                if (actionBar.contains("✎")) {
                    String[] mana = actionBar.split("✎")[0]
                            .split("§b");
                    mana = mana[mana.length - 1].split("/");
                    manaCurrent = Integer.parseInt(mana[0]);
                    manaMax = Integer.parseInt(mana[1]);
                }
                if (actionBar.contains("❤") && actionBar.contains("✎")) {
                    actionBar = actionBar.replaceAll(hpColor + hpCurrent + "/" + hpMax + "❤     ", "").replaceAll("     §b" + manaCurrent + "/" + manaMax + "✎ Mana", "");
                    return Text.of(actionBar);
                }
            }
        }
        return message;
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void renderStatusBars(MatrixStack matrices, CallbackInfo ci) {
        if (Utils.isSkyblock()) {
            if (SkyblockerConfig.get().dungeons.enableMap) {
                renderDungeonMap(matrices);
            }

            if (SkyblockerConfig.get().bars.enableBars) {
                ci.cancel();
                renderBars(matrices);
            }

            this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
        }
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void renderMountHealth(MatrixStack matrices, CallbackInfo ci) {
        if (Utils.isSkyblock() && SkyblockerConfig.get().bars.enableBars) {
            ci.cancel();
        }
    }


    private void renderDungeonMap(MatrixStack matrices) {
        ItemStack item = this.client.player.inventory.main.get(8);
        CompoundTag tag = item.getTag();

        if (tag != null && tag.contains("map")) {
            VertexConsumerProvider.Immediate vertices = this.client.getBufferBuilders().getEffectVertexConsumers();
            MapRenderer map = this.client.gameRenderer.getMapRenderer();
            MapState state = FilledMapItem.getMapState(item, this.client.world);

            if (state == null) return;
            matrices.push();
            matrices.translate(2, 2, 0);
            matrices.scale(1, 1, 0);
            map.draw(matrices, vertices, state, false, 15728880);
            vertices.draw();
            matrices.pop();
        }
    }

    private void renderBars(MatrixStack matrices) {
        this.client.getTextureManager().bindTexture(ICONS);
        this.client.getProfiler().push("skyblockBars");
        {
            int left = this.scaledWidth / 2 - 91;
            int hpWidth = getWidth(hpCurrent, hpMax);
            int manaWidth = getWidth(manaCurrent, manaMax);

            if (hpColor.equals("§6") && SkyblockerConfig.get().bars.enableAbsorption) {
                renderBar(matrices, left, hpWidth, SkyblockerConfig.get().bars.absorbedHealthColor);
            } else {
                renderBar(matrices, left, hpWidth, SkyblockerConfig.get().bars.healthColor);
            }
            renderBar(matrices, left + 71 + 40, manaWidth, SkyblockerConfig.get().bars.manaColor);
        }
        this.client.getProfiler().pop();

        this.client.getProfiler().push("skyblockTexts");
        {
            int left = this.scaledWidth / 2 - 90;
            String hpText = hpCurrent + "/" + hpMax;
            String manaText = manaCurrent + "/" + manaMax;
            int hpOffset = (71 - this.getFontRenderer().getWidth(hpText)) / 2;
            int manaOffset = (71 - this.getFontRenderer().getWidth(manaText)) / 2;

            if (hpColor.equals("§6") && SkyblockerConfig.get().bars.enableAbsorption) {
                renderText(matrices, hpText, left + hpOffset, SkyblockerConfig.get().bars.absorbedHealthColor);
            } else {
                renderText(matrices, hpText, left + hpOffset, SkyblockerConfig.get().bars.healthColor);
            }
            renderText(matrices, manaText, left + 71 + 40 + manaOffset, SkyblockerConfig.get().bars.manaColor);
        }
        this.client.getProfiler().pop();
    }

    private void renderBar(MatrixStack matrices, int left, int filled, int color) {
        Color co = new Color(color);
        int top = this.scaledHeight - 36;

        RenderSystem.enableBlend();
        RenderSystem.color4f((float) co.getRed() / 255, (float) co.getGreen() / 255, (float) co.getBlue() / 255, 1.0F);
        this.drawTexture(matrices, left, top, 0, 0, 71, 5);

        if (filled > 0) {
            this.drawTexture(matrices, left, top, 0, 5, filled, 5);
        }

        RenderSystem.disableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderText(MatrixStack matrices, String str, int left, int color) {
        int top = this.scaledHeight - 42;

        this.getFontRenderer().draw(matrices, str, (float) (left + 1), (float) top, 0);
        this.getFontRenderer().draw(matrices, str, (float) (left - 1), (float) top, 0);
        this.getFontRenderer().draw(matrices, str, (float) left, (float) (top + 1), 0);
        this.getFontRenderer().draw(matrices, str, (float) left, (float) (top - 1), 0);
        this.getFontRenderer().draw(matrices, str, (float) left, (float) top, color);
    }

    private int getWidth(int current, int max) {
        int width = 0;
        if (current != 0) {
            if (current > max) {
                width = 71;
            } else {
                width = current * 71 / max;
            }
        } else {
            width = 0;
        }
        return width;
    }
}