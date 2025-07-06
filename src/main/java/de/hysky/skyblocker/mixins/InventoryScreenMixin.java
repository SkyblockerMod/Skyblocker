package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.compatibility.ResourcePackCompatibility;
import de.hysky.skyblocker.injected.RecipeBookHolder;
import de.hysky.skyblocker.mixins.accessors.ScreenAccessor;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.SkyblockRecipeBookWidget;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> implements RecipeBookHolder {

	@Unique
	private final List<Runnable> recipeBookToggleCallbacks = new ArrayList<>();

    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }


    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/RecipeBookScreen;<init>(Lnet/minecraft/screen/AbstractRecipeScreenHandler;Lnet/minecraft/client/gui/screen/recipebook/RecipeBookWidget;Lnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/text/Text;)V"))
    private static RecipeBookWidget<?> skyblocker$replaceRecipeBook(RecipeBookWidget<?> original, @Local(argsOnly = true) PlayerEntity player) {
        return SkyblockerConfigManager.get().general.itemList.enableItemList && Utils.isOnSkyblock() ? new SkyblockRecipeBookWidget(player.playerScreenHandler) : original;
    }

    @ModifyArg(method = "getRecipeBookButtonPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ScreenPos;<init>(II)V"), index = 0)
    private int skyblocker$moveButton(int x) {
        return Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory ? x + 21 : x;
    }

	@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/StatusEffectsDisplay;drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;IIF)V"))
	private boolean skyblocker$dontDrawStatusEffects(StatusEffectsDisplay statusEffectsDisplay, DrawContext context, int mouseX, int mouseY, float tickDelta) {
		return !(Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay || Utils.isInGarden() && SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget);
	}

	// This makes it so that REI at least doesn't wrongly exclude the zone
	// shouldHideStatusEffectHud should actually be showsStatusEffects
	@ModifyReturnValue(method = "shouldHideStatusEffectHud", at = @At("RETURN"))
	private boolean skyblocker$markStatusEffectsHidden(boolean original) {
		// In the garden, status effects are shown when both hideStatusEffectOverlay and gardenPlotsWidget are false
		if (Utils.isInGarden()) return original && !SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay && !SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget;
		// In the rest of Skyblock, status effects are shown when hideStatusEffectOverlay is false
		if (Utils.isOnSkyblock()) return original && !SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay;
		// In vanilla, status effects are shown as normal
		return original;
	}

    @Inject(method = "onRecipeBookToggled", at = @At("TAIL"))
    private void skyblocker$callRecipeToggleCallbacks(CallbackInfo ci) {
		recipeBookToggleCallbacks.forEach(Runnable::run);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void skyblocker$clearRecipeToggleCallbacks(CallbackInfo ci) {
		recipeBookToggleCallbacks.clear();
    }

	@Inject(method = "<init>", at = @At("TAIL"), order = 900) // run it a little earlier in case firmament do stuff
	private void skyblocker$furfskyCompat(CallbackInfo ci) {
		if (Utils.isOnSkyblock() && ResourcePackCompatibility.options.renameInventoryScreen().orElse(false)) {
			((ScreenAccessor) this).setTitle(Text.literal(SkyblockerConfigManager.get().quickNav.enableQuickNav ? "InventoryScreenQuickNavSkyblocker": "InventoryScreenSkyblocker"));
		}
	}

	@Override
	public void registerRecipeBookToggleCallback(Runnable runnable) {
		recipeBookToggleCallbacks.add(runnable);
	}
}
