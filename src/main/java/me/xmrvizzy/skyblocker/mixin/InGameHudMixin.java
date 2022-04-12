package me.xmrvizzy.skyblocker.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.FancyStatusBars;
import me.xmrvizzy.skyblocker.skyblock.HotbarSlotLock;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonMap;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
    private static final Identifier SLOT_LOCK = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/slot_lock.png");

    private final FancyStatusBars statusBars = new FancyStatusBars();
    private MatrixStack hotbarMatrices;
    private int hotbarSlotIndex;

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private int scaledHeight;
    @Shadow
    private int scaledWidth;

    @Inject(method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if(!Utils.isOnSkyblock)
            return;
        String msg = message.getString();
        if(statusBars.update(msg))
            ci.cancel();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"))
    public void renderHotbar(float f, MatrixStack matrices, CallbackInfo ci) {
        if (Utils.isOnSkyblock) {
            hotbarMatrices = matrices;
            hotbarSlotIndex = 0;
        }
    }

    @Inject(method = "renderHotbarItem", at = @At("HEAD"))
    public void renderHotbarItem(int i, int j, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if (Utils.isOnSkyblock) {
            if (HotbarSlotLock.isLocked(hotbarSlotIndex)) {
                RenderSystem.setShaderTexture(0,SLOT_LOCK);
                this.drawTexture(hotbarMatrices, i, j, 0, 0,16, 16);
            }
            hotbarSlotIndex++;
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void renderExperienceBar(MatrixStack matrices, int x, CallbackInfo ci) {
        if (Utils.isOnSkyblock && SkyblockerConfig.get().general.bars.enableBars)
            ci.cancel();
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void renderStatusBars(MatrixStack matrices, CallbackInfo ci) {
        if(!Utils.isOnSkyblock)
            return;
        if(statusBars.render(matrices, scaledWidth, scaledHeight))
            ci.cancel();

        if (Utils.isInDungeons && SkyblockerConfig.get().locations.dungeons.enableMap)
            DungeonMap.render(matrices);

        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void renderMountHealth(MatrixStack matrices, CallbackInfo ci) {
        if (Utils.isOnSkyblock && SkyblockerConfig.get().general.bars.enableBars)
            ci.cancel();
    }
}