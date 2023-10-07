package me.xmrvizzy.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.xmrvizzy.skyblocker.config.SkyblockerConfigManager;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemListWidget;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @ModifyExpressionValue(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/gui/screen/recipebook/RecipeBookWidget"))
    private RecipeBookWidget skyblocker$replaceRecipeBook(RecipeBookWidget original) {
        return SkyblockerConfigManager.get().general.itemList.enableItemList && Utils.isOnSkyblock() ? new ItemListWidget() : original;
    }
}
