package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.garden.GardenPlotsWidget;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.SkyblockRecipeBookWidget;
import de.hysky.skyblocker.utils.Location;
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

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {

    @Unique
    private GardenPlotsWidget gardenPlotsWidget;

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
		return !(Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay);
	}

	//This makes it so that REI at least doesn't wrongly exclude the zone
	@ModifyReturnValue(method = "shouldHideStatusEffectHud", at = @At("RETURN"))
	private boolean skyblocker$markStatusEffectsHidden(boolean original) {
		return Utils.isOnSkyblock() ? !SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay : original;
	}

    @Inject(method = "onRecipeBookToggled", at = @At("TAIL"))
    private void skyblocker$moveGardenPlotsWdiget(CallbackInfo ci) {
        if (gardenPlotsWidget != null) {
            gardenPlotsWidget.setPosition(x + backgroundWidth + 4, y);
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void skyblocker$addGardenPlotsWidget(CallbackInfo ci) {
        if (Utils.getLocation().equals(Location.GARDEN) && SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget) {
            addDrawableChild(gardenPlotsWidget = new GardenPlotsWidget(x + backgroundWidth + 4, y));
        }
    }
}
