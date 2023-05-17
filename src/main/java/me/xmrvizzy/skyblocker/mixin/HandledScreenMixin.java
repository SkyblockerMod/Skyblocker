package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.BackpackPreview;
import me.xmrvizzy.skyblocker.skyblock.item.WikiLookup;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
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
    @Nullable
    protected Slot focusedSlot;

    @Inject(at = @At("HEAD"), method = "keyPressed")
    public void skyblocker$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.focusedSlot != null) {
            if (keyCode != 256 && !this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
                if (WikiLookup.wikiLookup.matchesKey(keyCode, scanCode)) WikiLookup.openWiki(this.focusedSlot);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "drawMouseoverTooltip", cancellable = true)
    public void skyblocker$drawMouseOverTooltip(MatrixStack matrices, int x, int y, CallbackInfo ci) {
        String title = this.getTitle().getString();
        boolean shiftDown = SkyblockerConfig.get().general.backpackPreviewWithoutShift ^ Screen.hasShiftDown();
        if (shiftDown && title.equals("Storage") && this.focusedSlot != null) {
            if (this.focusedSlot.inventory == this.client.player.getInventory()) return;
            if (BackpackPreview.renderPreview(matrices, this.focusedSlot.getIndex(), x, y)) ci.cancel();
        }
    }
}
