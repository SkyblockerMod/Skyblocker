package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Accessor
	ProfileKeyPairManager getProfileKeyPairManager();
}
