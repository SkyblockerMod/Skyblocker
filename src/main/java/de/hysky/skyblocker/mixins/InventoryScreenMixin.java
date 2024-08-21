package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.garden.GardenPlotsWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemListWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {

    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

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


    @Unique
    private GardenPlotsWidget gardenPlotsWidget;
    @Unique
    private ButtonWidget deskButton;

    @Inject(method = "method_19891", at = @At("TAIL"))
    private void skyblocker$moveGardenPlotsWdiget(CallbackInfo ci) {
        if (Utils.getLocation().equals(Location.GARDEN) && gardenPlotsWidget != null) {
            gardenPlotsWidget.setPosition(x + backgroundWidth + 4, y);
            if (deskButton != null) deskButton.setPosition(gardenPlotsWidget.getX() + 4, y + 108);
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void skyblocker$addGardenPlotsWidget(CallbackInfo ci) {
        if (Utils.getLocation().equals(Location.GARDEN) && SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget) {
            gardenPlotsWidget = new GardenPlotsWidget(x + backgroundWidth + 4, y);
            addDrawableChild(gardenPlotsWidget);
            deskButton = ButtonWidget.builder(Text.translatable("skyblocker.gardenPlots.openDesk"), button -> MessageScheduler.INSTANCE.sendMessageAfterCooldown("/desk"))
                    .dimensions(gardenPlotsWidget.getX() + 7, y + 108, 60, 15)
                    .build();
            addDrawableChild(deskButton);
        }
    }
}
