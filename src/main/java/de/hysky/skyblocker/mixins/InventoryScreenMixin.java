package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.compatibility.ResourcePackCompatibility;
import de.hysky.skyblocker.injected.RecipeBookHolder;
import de.hysky.skyblocker.mixins.accessors.ScreenAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.SkyblockRecipeBookWidget;
import de.hysky.skyblocker.utils.Utils;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> implements RecipeBookHolder {

	@Unique
	private final List<Runnable> recipeBookToggleCallbacks = new ArrayList<>();

	public InventoryScreenMixin(InventoryMenu handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
	}


	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractRecipeBookScreen;<init>(Lnet/minecraft/world/inventory/RecipeBookMenu;Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;)V"))
	private static RecipeBookComponent<?> skyblocker$replaceRecipeBook(RecipeBookComponent<?> original, @Local(argsOnly = true) Player player) {
		return SkyblockerConfigManager.get().general.itemList.enableItemList && Utils.isOnSkyblock() ? new SkyblockRecipeBookWidget(player.inventoryMenu) : original;
	}

	@ModifyArg(method = "getRecipeBookButtonPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/navigation/ScreenPosition;<init>(II)V"), index = 0)
	private int skyblocker$moveButton(int x) {
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory ? x + 21 : x;
	}

	@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;render(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
	private boolean skyblocker$dontDrawStatusEffects(EffectsInInventory statusEffectsDisplay, GuiGraphics context, int mouseX, int mouseY) {
		return !(Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay || Utils.isInGarden() && SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget);
	}

	// This makes it so that REI at least doesn't wrongly exclude the zone
	@ModifyReturnValue(method = "showsActiveEffects", at = @At("RETURN"))
	private boolean skyblocker$markStatusEffectsHidden(boolean original) {
		// In the garden, status effects are shown when both hideStatusEffectOverlay and gardenPlotsWidget are false
		if (Utils.isInGarden()) return original && !SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay && !SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget;
		// In the rest of Skyblock, status effects are shown when hideStatusEffectOverlay is false
		if (Utils.isOnSkyblock()) return original && !SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay;
		// In vanilla, status effects are shown as normal
		return original;
	}

	@Inject(method = "onRecipeBookButtonClick", at = @At("TAIL"))
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
			((ScreenAccessor) this).setTitle(Component.literal(SkyblockerConfigManager.get().quickNav.enableQuickNav ? "InventoryScreenQuickNavSkyblocker" : "InventoryScreenSkyblocker"));
		}
	}

	@Override
	public void registerRecipeBookToggleCallback(Runnable runnable) {
		recipeBookToggleCallbacks.add(runnable);
	}
}
