package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemListWidget;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @Redirect(
            method = "",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/client/gui/screen/recipebook/RecipeBookWidget"
            )
    )
    RecipeBookWidget constructor() {
        return new ItemListWidget();
    }
}
