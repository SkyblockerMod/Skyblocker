package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.FancyStatusBars;
import me.xmrvizzy.skyblocker.skyblock.HotbarSlotLock;
import me.xmrvizzy.skyblocker.skyblock.StatusBarTracker;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonMap;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	//@Shadow
	//@Final
	//private static Identifier ICONS = new Identifier("textures/gui/icons.png");
    @Unique
    private static final Identifier SLOT_LOCK = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/slot_lock.png");
    @Unique

    private final StatusBarTracker statusBarTracker = SkyblockerMod.getInstance().statusBarTracker;
    @Unique
    private final FancyStatusBars statusBars = new FancyStatusBars();
    @Unique
    private DrawContext hotbarContext;
    @Unique
    private int hotbarSlotIndex;

    @Shadow
    private int scaledHeight;
    @Shadow
    private int scaledWidth;

    @Shadow
    public void setOverlayMessage(Text message, boolean tinted) {
    }

    @Inject(method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"), cancellable = true)
    private void skyblocker$onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (!Utils.isOnSkyblock() || !SkyblockerConfig.get().general.bars.enableBars || Utils.isInTheRift())
            return;
        String msg = message.getString();
        String res = statusBarTracker.update(msg, SkyblockerConfig.get().messages.hideMana);
        if (!msg.equals(res)) {
            if (res != null)
                setOverlayMessage(Text.of(res), tinted);
            ci.cancel();
        }
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"))
    public void skyblocker$renderHotbar(float f, DrawContext context, CallbackInfo ci) {
        if (Utils.isOnSkyblock()) {
            hotbarContext = context;
            hotbarSlotIndex = 0;
        }
    }

    @Inject(method = "renderHotbarItem", at = @At("HEAD"))
    public void skyblocker$renderHotbarItem(DrawContext context, int i, int j, float f, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if (Utils.isOnSkyblock()) {
            if (HotbarSlotLock.isLocked(hotbarSlotIndex)) {
                hotbarContext.drawTexture(SLOT_LOCK, i, j, 0, 0, 16, 16);
            }
            hotbarSlotIndex++;
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void skyblocker$renderExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        if (Utils.isOnSkyblock() && SkyblockerConfig.get().general.bars.enableBars && !Utils.isInTheRift())
            ci.cancel();
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void skyblocker$renderStatusBars(DrawContext context, CallbackInfo ci) {
        if (!Utils.isOnSkyblock())
            return;
        if (statusBars.render(context, scaledWidth, scaledHeight))
            ci.cancel();

        if (Utils.isInDungeons() && SkyblockerConfig.get().locations.dungeons.enableMap)
            DungeonMap.render(context.getMatrices());

        //RenderSystem.setShaderTexture(0, ICONS);
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void skyblocker$renderMountHealth(DrawContext context, CallbackInfo ci) {
        if (Utils.isOnSkyblock() && SkyblockerConfig.get().general.bars.enableBars && !Utils.isInTheRift())
            ci.cancel();
    }
}