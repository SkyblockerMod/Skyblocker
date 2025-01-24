package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.skyblock.dwarven.GlaciteColdOverlay;
import de.hysky.skyblocker.skyblock.fancybars.FancyStatusBars;
import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.skyblock.item.ItemCooldowns;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.ItemRarityBackgrounds;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique
    private static final Supplier<Identifier> SLOT_LOCK_ICON = () -> SkyblockerConfigManager.get().general.itemProtection.slotLockStyle.tex;
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

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V", ordinal = 0))
    public void skyblocker$renderHotbarItemLockOrRarityBg(CallbackInfo ci, @Local(argsOnly = true) DrawContext context, @Local(ordinal = 4, name = "m") int index, @Local(ordinal = 5, name = "n") int x, @Local(ordinal = 6, name = "o") int y, @Local PlayerEntity player) {
        if (Utils.isOnSkyblock()) {
            // slot lock
            if (SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgrounds) ItemRarityBackgrounds.tryDraw(player.getInventory().main.get(index), context, x, y);
            if (HotbarSlotLock.isLocked(index)) {
                RenderSystem.enableBlend();
                context.drawTexture(RenderLayer::getGuiTextured, SLOT_LOCK_ICON.get(), x, y, 0, 0, 16, 16, 16, 16);
                RenderSystem.disableBlend();
            }
            //item protection
            if (ItemProtection.isItemProtected(player.getInventory().main.get(index))) {
                RenderSystem.enableBlend();
                context.drawTexture(RenderLayer::getGuiTextured, ItemProtection.ITEM_PROTECTION_TEX, x, y, 0, 0, 16, 16, 16, 16);
                RenderSystem.disableBlend();
            }
        }
    }

    @Inject(method = { "renderExperienceBar", "renderExperienceLevel" }, at = @At("HEAD"), cancellable = true, require = 2)
    private void skyblocker$renderExperienceBar(CallbackInfo ci) {
        if (Utils.isOnSkyblock() && FancyStatusBars.isEnabled() && FancyStatusBars.isExperienceFancyBarVisible())
            ci.cancel();
    }

    @Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V", shift = At.Shift.AFTER), cancellable = true)
    private void skyblocker$renderStatusBars(DrawContext context, CallbackInfo ci) {
        if (Utils.isOnSkyblock() && statusBars.render(context, context.getScaledWindowWidth(), context.getScaledWindowHeight())) ci.cancel();
    }

    @Inject(method = "renderHealthBar", at = @At(value = "HEAD"), cancellable = true)
    private void skyblocker$renderHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (!Utils.isOnSkyblock()) return;
        if (FancyStatusBars.isEnabled() && FancyStatusBars.isHealthFancyBarVisible()) ci.cancel();
    }

    @ModifyExpressionValue(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowHeight()I"))
    private int skyblocker$moveHealthDown(int original) {
        return Utils.isOnSkyblock() && FancyStatusBars.isEnabled() && !FancyStatusBars.isHealthFancyBarVisible() && FancyStatusBars.isExperienceFancyBarVisible() ? original + 6 : original;
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void skyblocker$renderStatusBars(DrawContext context, PlayerEntity player, int i, int j, int k, int x, CallbackInfo ci) {
        if (Utils.isOnSkyblock() && FancyStatusBars.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void skyblocker$renderMountHealth(CallbackInfo ci) {
        if (Utils.isOnSkyblock() && FancyStatusBars.isEnabled())
            ci.cancel();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void skyblocker$dontRenderStatusEffects(CallbackInfo ci) {
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay) ci.cancel();
    }

	// TODO switch to fabric event when available
    @Inject(method = "renderMiscOverlays", at = @At("TAIL"))
    private void skyblocker$afterMiscOverlays(CallbackInfo ci, @Local(argsOnly = true) DrawContext context) {
        GlaciteColdOverlay.render(context);
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
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().farming.garden.dicerTitlePrevent && title != null && DICER_TITLE_BLACKLIST.matcher(title.getString()).matches()) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 2))
    private LayeredDrawer.Layer skyblocker$afterMainHud(LayeredDrawer.Layer mainHudLayer) {
        return (context, tickCounter) -> {
            mainHudLayer.render(context, tickCounter);
            HudRenderEvents.AFTER_MAIN_HUD.invoker().onRender(context, tickCounter);
        };
    }

    @ModifyArg(method = "<init>", slice = @Slice(from = @At(value = "NEW", target = "Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 2)), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 5))
    private LayeredDrawer.Layer skyblocker$beforeChat(LayeredDrawer.Layer beforeChatLayer) {
        return (context, tickCounter) -> {
            HudRenderEvents.BEFORE_CHAT.invoker().onRender(context, tickCounter);
            beforeChatLayer.render(context, tickCounter);
        };
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void skyblocker$afterDrawersInitialized(CallbackInfo ci) {
        this.layeredDrawer.addLayer(HudRenderEvents.LAST.invoker()::onRender);
    }

    // Renders the hud (always on screen) widgets.
    // Inject before the debug hud, this injection point is identical to the after main hud event
    // z = 200
    // TODO: Switch to fabric event when available
    // TODO: The after sleep/before demo timer injection point gives z = 1600,
    // 		 and due to the z offset that comes with item rendering, it still renders above the debug hud
    @Inject(method = "renderMainHud", at = @At("RETURN"))
    private void skyblocker$renderHud(CallbackInfo ci, @Local(argsOnly = true) DrawContext context) {
        skyblocker$renderTabHudInternal(context, true);
    }

    // Renders the tab widgets
    // TODO: Switch to fabric event when available
    @Inject(method = "renderPlayerList", at = @At("HEAD"))
    private void skyblocker$renderTabHud(CallbackInfo ci, @Local(argsOnly = true) DrawContext context) {
        skyblocker$renderTabHudInternal(context, false);
    }

    private void skyblocker$renderTabHudInternal(DrawContext context, boolean hud) {
        if (!Utils.isOnSkyblock()) return;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.currentScreen instanceof WidgetsConfigurationScreen) return;
        Window window = client.getWindow();
        float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100f;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1.F);
        ScreenMaster.render(context, (int) (window.getScaledWidth() / scale), (int) (window.getScaledHeight() / scale), hud);
        matrices.pop();
    }

	@WrapWithCondition(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
	private boolean skyblocker$shouldRenderHud(PlayerListHud playerListHud, DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective) {
		return !Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled || TabHud.shouldRenderVanilla() || MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen;
	}
}
