package me.xmrvizzy.skyblocker.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor
    int getX();
    @Accessor
    int getY();
    @Accessor
    int getBackgroundWidth();
    @Accessor
    int getBackgroundHeight();
}
