package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemListWidget;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @ModifyExpressionValue(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/gui/screen/recipebook/RecipeBookWidget"))
    private RecipeBookWidget skyblocker$replaceRecipeBook(RecipeBookWidget original) {
        return SkyblockerConfigManager.get().general.itemList.enableItemList && Utils.isOnSkyblock() ? new ItemListWidget() : original;
    }

    @WrapOperation(method = "init", at = @At(value = "NEW", target = "(IIIILnet/minecraft/client/gui/screen/ButtonTextures;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/TexturedButtonWidget;"))
    private TexturedButtonWidget skyblocker$moveButton(int x, int y, int width, int height, ButtonTextures textures, ButtonWidget.PressAction pressAction, Operation<TexturedButtonWidget> original) {
        if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory) return original.call(x, y, width, height, textures, pressAction);
        return new TexturedButtonWidget(x + 21, y, width, height, textures, pressAction);
    }

    @WrapOperation(method = "method_19891", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;setPosition(II)V"))
    private void skyblocker$moveButtonWhenPressed(ButtonWidget instance, int i, int j, Operation<Void> original) {
        if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.showEquipmentInInventory) original.call(instance, i, j);
        else instance.setPosition(i + 21, j);
    }
}
