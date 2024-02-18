package de.hysky.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.mixin.accessor.LayeredDrawerAccessor;
import de.hysky.skyblocker.skyblock.FancyStatusBars;
import de.hysky.skyblocker.skyblock.dungeon.DungeonMap;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScoreHUD;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
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

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
            if (ItemProtection.isItemProtected(player.getInventory().main.get(index))){
                RenderSystem.enableBlend();
                context.drawTexture(ITEM_PROTECTION, x, y, 0, 0, 16, 16, 16, 16);
                RenderSystem.disableBlend();
            }
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
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

        if (Utils.isInDungeons() && DungeonScore.isDungeonStarted()) {
            if (SkyblockerConfigManager.get().locations.dungeons.enableMap && !DungeonManager.isInBoss()) DungeonMap.render(context.getMatrices());
            if (SkyblockerConfigManager.get().locations.dungeons.dungeonScore.enableScoreHUD) DungeonScoreHUD.render(context);
        }
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

    /**
     * Hopefully other mods don't add stages into these two drawers...
     * 
     * @implNote Check this every update to see if the indexes of each layer have changed.
     */
    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;layeredDrawer:Lnet/minecraft/client/gui/LayeredDrawer;", opcode = Opcodes.GETFIELD))
    private void skyblocker$beforeDrawersInitialized(CallbackInfo ci, @Local(ordinal = 0) LayeredDrawer persistentDrawer, @Local(ordinal = 1) LayeredDrawer intermittentDrawer) {
        List<LayeredDrawer.Layer> persistentLayers = ((LayeredDrawerAccessor) persistentDrawer).getLayers();

        //After Main HUD - stage index is 2
        LayeredDrawer.Layer mainHudLayer = persistentLayers.get(2);

        persistentLayers.set(2, (context, tickDelta) -> {
            mainHudLayer.render(context, tickDelta);
            HudRenderEvents.AFTER_MAIN_HUD.invoker().onRender(context, tickDelta);
        });

        List<LayeredDrawer.Layer> intermittentLayers = ((LayeredDrawerAccessor) intermittentDrawer).getLayers();

        //Before Chat - stage index is 5
        LayeredDrawer.Layer chatLayer = intermittentLayers.get(5);

        intermittentLayers.set(5, (context, tickDelta) -> {
            HudRenderEvents.BEFORE_CHAT.invoker().onRender(context, tickDelta);
            chatLayer.render(context, tickDelta);
        });
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void skyblocker$afterDrawersInitialized(CallbackInfo ci) {
        this.layeredDrawer.addLayer(HudRenderEvents.LAST.invoker()::onRender);
    }
}
