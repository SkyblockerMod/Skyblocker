package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.fancybars.FancyStatusBars;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.skyblock.item.ItemCooldowns;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.ItemRarityBackgrounds;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique
    private static final Supplier<Identifier> SLOT_LOCK_ICON = () -> SkyblockerConfigManager.get().general.itemProtection.slotLockStyle.tex;

    @Unique
    private static final Identifier ITEM_PROTECTION = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/item_protection.png");
    @Unique
    private static final Pattern DICER_TITLE_BLACKLIST = Pattern.compile(".+? DROP!");

    @Unique
    private final FancyStatusBars statusBars = new FancyStatusBars();

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private LayeredDrawer layeredDrawer;

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V", ordinal = 0))
    public void skyblocker$renderHotbarItemLockOrRarityBg(CallbackInfo ci, @Local(argsOnly = true) DrawContext context, @Local(ordinal = 4, name = "m") int index, @Local(ordinal = 5, name = "n") int x, @Local(ordinal = 6, name = "o") int y, @Local PlayerEntity player) {
        if (Utils.isOnSkyblock()) {
            // slot lock
            if (SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgrounds) ItemRarityBackgrounds.tryDraw(player.getInventory().main.get(index), context, x, y);
            if (HotbarSlotLock.isLocked(index)) {
                RenderSystem.enableBlend();
                context.drawTexture(SLOT_LOCK_ICON.get(), x, y, 0, 0, 16, 16, 16, 16);
                RenderSystem.disableBlend();
            }
            //item protection
            if (ItemProtection.isItemProtected(player.getInventory().main.get(index))) {
                RenderSystem.enableBlend();
                context.drawTexture(ITEM_PROTECTION, x, y, 0, 0, 16, 16, 16, 16);
                RenderSystem.disableBlend();
            }
        }
    }

    @Inject(method = { "renderExperienceBar", "renderExperienceLevel" }, at = @At("HEAD"), cancellable = true)
    private void skyblocker$renderExperienceBar(CallbackInfo ci) {
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.bars.enableBars && !Utils.isInTheRift())
            ci.cancel();
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void skyblocker$renderStatusBars(DrawContext context, CallbackInfo ci) {
        if (!Utils.isOnSkyblock())
            return;
        if (statusBars.render(context, context.getScaledWindowWidth(), context.getScaledWindowHeight()))
            ci.cancel();
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void skyblocker$renderMountHealth(CallbackInfo ci) {
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.bars.enableBars && !Utils.isInTheRift())
            ci.cancel();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void skyblocker$dontRenderStatusEffects(CallbackInfo ci) {
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.hideStatusEffectOverlay) ci.cancel();
    }

    @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"))
    private float skyblocker$modifyAttackIndicatorCooldown(float cooldownProgress) {
        if (Utils.isOnSkyblock() && client.player != null) {
            ItemStack stack = client.player.getMainHandStack();
            if (ItemCooldowns.isOnCooldown(stack)) {
                return ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent();
            }
        }

        return cooldownProgress;
    }

    @Inject(method = "setTitle", at = @At("HEAD"), cancellable = true)
    private void skyblocker$dicerTitlePrevent(Text title, CallbackInfo ci) {
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().locations.garden.dicerTitlePrevent && title != null && DICER_TITLE_BLACKLIST.matcher(title.getString()).matches()) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 2))
    private LayeredDrawer.Layer skyblocker$afterMainHud(LayeredDrawer.Layer mainHudLayer) {
        return (context, tickDelta) -> {
        	mainHudLayer.render(context, tickDelta);
        	HudRenderEvents.AFTER_MAIN_HUD.invoker().onRender(context, tickDelta);
        };
    }

    @ModifyArg(method = "<init>", slice = @Slice(from = @At(value = "NEW", target = "Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 1)), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 5))
    private LayeredDrawer.Layer skyblocker$beforeChat(LayeredDrawer.Layer beforeChatLayer) {
        return (context, tickDelta) -> {
        	HudRenderEvents.BEFORE_CHAT.invoker().onRender(context, tickDelta);
        	beforeChatLayer.render(context, tickDelta);
        };
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void skyblocker$afterDrawersInitialized(CallbackInfo ci) {
        this.layeredDrawer.addLayer(HudRenderEvents.LAST.invoker()::onRender);
    }
}
