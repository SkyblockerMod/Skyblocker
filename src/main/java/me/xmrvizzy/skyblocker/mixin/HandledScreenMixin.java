package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
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
        // quicknav
        if (Utils.isSkyblock && SkyblockerConfig.get().general.quicknav.enableQuicknav) {
            String screenTitle = super.getTitle().getString().trim();
            List<QuickNavButton> buttons = QuickNav.init(screenTitle);
            for (QuickNavButton button : buttons) super.addDrawableChild(button);

        }
    }
}
