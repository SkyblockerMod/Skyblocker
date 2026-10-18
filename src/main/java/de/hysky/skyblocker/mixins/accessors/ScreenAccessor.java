package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Mixin(Screen.class)
public interface ScreenAccessor {
	@Accessor
	@Mutable
	void setTitle(Component title);
}
