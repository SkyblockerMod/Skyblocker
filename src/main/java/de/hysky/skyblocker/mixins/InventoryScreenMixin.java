package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.injected.RecipeBookHolder;

import de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen;
import net.minecraft.resources.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.SkyblockRecipeBookComponent;
import de.hysky.skyblocker.utils.Utils;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
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
	private static RecipeBookComponent<?> skyblocker$replaceRecipeBook(RecipeBookComponent<?> original, @Local(name = "player") Player player) {
		return SkyblockerConfigManager.get().general.itemList.enableRecipeBook && Utils.isOnSkyblock() ? new SkyblockRecipeBookComponent(player.inventoryMenu) : original;
	}

	@ModifyArg(method = "getRecipeBookButtonPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/navigation/ScreenPosition;<init>(II)V"), index = 0)
	private int skyblocker$moveButton(int x) {
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory ? x + 21 : x;
	}

	@WrapWithCondition(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"))
	private boolean skyblocker$dontExtractStatusEffects(EffectsInInventory statusEffectsDisplay, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		return !(Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay || Utils.isInGarden() && SkyblockerConfigManager.get().farming.plotsWidget.enabled);
	}

	// This makes it so that REI at least doesn't wrongly exclude the zone
	@ModifyReturnValue(method = "showsActiveEffects", at = @At("RETURN"))
	private boolean skyblocker$markStatusEffectsHidden(boolean original) {
		// In the garden, status effects are shown when both hideStatusEffectOverlay and gardenPlotsWidget are false
		if (Utils.isInGarden()) return original && !SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay && !SkyblockerConfigManager.get().farming.plotsWidget.enabled;
		// In the rest of Skyblock, status effects are shown when hideStatusEffectOverlay is false
		if (Utils.isOnSkyblock()) return original && !SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay;
		// In vanilla, status effects are shown as normal
		return original;
	}

	@ModifyExpressionValue(method = "extractBackground", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;INVENTORY_LOCATION:Lnet/minecraft/resources/Identifier;", opcode = Opcodes.GETSTATIC))
	private Identifier skyblocker$getBackground(Identifier original) {
		// gotta do this, if I don't call super in SkyblockInventoryScreen quick nav doesn't get rendered
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory) return SkyblockInventoryScreen.BACKGROUND.get();
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

	@Override
	public void registerRecipeBookToggleCallback(Runnable runnable) {
		recipeBookToggleCallbacks.add(runnable);
	}
}
