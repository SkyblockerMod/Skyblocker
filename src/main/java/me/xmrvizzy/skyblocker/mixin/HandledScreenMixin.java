package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.item.WikiLookup;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemListWidget;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    protected HandledScreenMixin(Text title) {
        super(title);
    }
    @Shadow
    @Nullable protected Slot focusedSlot;

    @Inject(method = "init()V", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (Utils.isSkyblock && SkyblockerConfig.get().general.itemList.enableItemList) {
            super.addDrawableChild(new ItemListWidget((HandledScreen)(Object)this));
        }
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode != 256 && !MinecraftClient.getInstance().options.keyInventory.matchesKey(keyCode, scanCode)){
            if (WikiLookup.wikiLookup.matchesKey(keyCode, scanCode)) WikiLookup.openWiki(focusedSlot);
        }
    }
}
