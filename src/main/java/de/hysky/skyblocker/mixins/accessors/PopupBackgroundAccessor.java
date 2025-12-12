package de.hysky.skyblocker.mixins.accessors;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(PopupScreen.class)
public interface PopupBackgroundAccessor {
	@Accessor("backgroundScreen")
	Screen getUnderlyingScreen();
}
