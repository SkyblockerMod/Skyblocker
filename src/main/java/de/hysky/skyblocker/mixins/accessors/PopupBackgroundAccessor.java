package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PopupScreen.class)
public interface PopupBackgroundAccessor {
	@Accessor("backgroundScreen")
	Screen getUnderlyingScreen();
}
