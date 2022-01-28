package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemListWidget;
import me.xmrvizzy.skyblocker.skyblock.quicknav.QuickNav;
import me.xmrvizzy.skyblocker.skyblock.quicknav.QuickNavButton;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (Utils.isSkyblock && SkyblockerConfig.get().general.itemList.enableItemList) {
            super.addDrawableChild(new ItemListWidget((HandledScreen)(Object)this));
        }

        // quicknav
        if (Utils.isSkyblock && SkyblockerConfig.get().general.quicknav.enableQuicknav) {
            String title = super.getTitle().getString().trim();
            int left_x = (super.width - this.backgroundWidth) / 2 + 4;
            int right_x = (super.width + this.backgroundWidth) / 2 - 3;
            int top_y = (super.height - this.backgroundHeight) / 2 - 28;
            int bottom_y = (super.height + this.backgroundHeight) / 2 - 4;
            if (this.backgroundHeight > 166) --bottom_y; // why is this even a thing
            List<QuickNavButton> buttons = QuickNav.init(title, left_x, right_x, top_y, bottom_y);
            for (QuickNavButton button : buttons) super.addDrawableChild(button);

        }
    }
}
