package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.BackpackPreview;
import me.xmrvizzy.skyblocker.skyblock.item.WikiLookup;
import me.xmrvizzy.skyblocker.skyblock.quicknav.QuickNav;
import me.xmrvizzy.skyblocker.skyblock.quicknav.QuickNavButton;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
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

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;

    protected HandledScreenMixin(Text title) {
        super(title);
    }
    @Shadow
    @Nullable protected Slot focusedSlot;

    @Inject(method = "init()V", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        // quicknav
        if (Utils.isOnSkyblock && SkyblockerConfig.get().quickNav.enableQuickNav) {
            String screenTitle = super.getTitle().getString().trim();
            List<QuickNavButton> buttons = QuickNav.init(screenTitle);
            for (QuickNavButton button : buttons) super.addDrawableChild(button);
        }
        // backpack preview
        BackpackPreview.updateStorage((HandledScreen<?>)(Object)this);
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.focusedSlot != null){
            if (keyCode != 256 && !this.client.options.inventoryKey.matchesKey(keyCode, scanCode)){
                if (WikiLookup.wikiLookup.matchesKey(keyCode, scanCode)) WikiLookup.openWiki(this.focusedSlot);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "drawMouseoverTooltip", cancellable = true)
    public void drawMouseOverTooltip(MatrixStack matrices, int x, int y, CallbackInfo ci) {
        String title = ((HandledScreen<?>)(Object)this).getTitle().getString();
        boolean shiftDown = SkyblockerConfig.get().general.backpackPreviewWithoutShift ^ Screen.hasShiftDown();
        if (shiftDown && title.equals("Storage") && this.focusedSlot != null) {
            if (this.focusedSlot.inventory == this.client.player.getInventory()) return;
            if (BackpackPreview.renderPreview(matrices, this.focusedSlot.getIndex(), x, y)) ci.cancel();
        }
    }
}
