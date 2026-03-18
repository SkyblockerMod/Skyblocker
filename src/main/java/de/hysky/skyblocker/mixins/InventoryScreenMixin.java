package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.compatibility.ResourcePackCompatibility;
import de.hysky.skyblocker.injected.RecipeBookHolder;
import de.hysky.skyblocker.mixins.accessors.ScreenAccessor;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.SkyblockRecipeBookWidget;
import de.hysky.skyblocker.skyblock.quicknav.QuickNav;
import de.hysky.skyblocker.skyblock.quicknav.QuickNavButton;
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
	@Unique
	private @Nullable List<QuickNavButton> quickNavButtons;

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

	@WrapWithCondition(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"))
	private boolean skyblocker$dontExtractStatusEffects(EffectsInInventory statusEffectsDisplay, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
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

		// Init Quick Nav
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().quickNav.enableQuickNav && this.minecraft.player != null && !this.minecraft.player.isCreative()) {
			for (QuickNavButton quickNavButton : this.quickNavButtons = QuickNav.init(this.getTitle().getString().trim())) {
				this.addWidget(quickNavButton);
			}
		}
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

	/**
	 * Draws the unselected tabs in front of the background, but behind the main inventory, similar to creative inventory tabs.
	 */
	@Inject(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
	private void skyblocker$extractUnselectedQuickNavButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (this.quickNavButtons != null) for (QuickNavButton quickNavButton : this.quickNavButtons) {
			// Render the button behind the main inventory background if it's not toggled or if it's still fading in
			if (!quickNavButton.toggled() || quickNavButton.getAlpha() < 255) {
				quickNavButton.setRenderInFront(false);
				quickNavButton.extractRenderState(graphics, mouseX, mouseY, a);
			}
		}
	}

	/**
	 * Draws the selected tab in front of the background and the main inventory, similar to creative inventory tabs.
	 */
	@Inject(method = "extractBackground", at = @At("RETURN"))
	private void skyblocker$extractSelectedQuickNavButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (this.quickNavButtons != null) for (QuickNavButton quickNavButton : this.quickNavButtons) {
			if (quickNavButton.toggled()) {
				quickNavButton.setRenderInFront(true);
				quickNavButton.extractRenderState(graphics, mouseX, mouseY, a);
			}
		}
	}
}
