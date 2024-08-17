package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemListWidget;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @ModifyExpressionValue(method = "<init>", at = @At(value = "NEW", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeBookWidget;"))
    private RecipeBookWidget skyblocker$replaceRecipeBook(RecipeBookWidget original) {
        return SkyblockerConfigManager.get().general.itemList.enableItemList && Utils.isOnSkyblock() ? new ItemListWidget() : original;
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TexturedButtonWidget;<init>(IIIILnet/minecraft/client/gui/screen/ButtonTextures;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)V"), index = 0)
    private int skyblocker$moveButton(int x) {
        return Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory ? x + 21 : x;
    }

    @ModifyArg(method = "method_19891", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;setPosition(II)V"), index = 0)
    private int skyblocker$moveButtonWhenPressed(int x) {
        return Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory ? x + 21 : x;
    }
}
