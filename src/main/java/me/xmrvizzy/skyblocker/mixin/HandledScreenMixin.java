package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemListWidget;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (Utils.isSkyblock && SkyblockerConfig.get().general.itemList.enableItemList) {
            super.addDrawableChild(new ItemListWidget((HandledScreen)(Object)this));
        }
    }
}
