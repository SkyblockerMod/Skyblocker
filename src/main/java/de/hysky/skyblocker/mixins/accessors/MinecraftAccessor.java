package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.renderer.block.BlockModelResolver;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Accessor
	ProfileKeyPairManager getProfileKeyPairManager();

	@Accessor
	BlockModelResolver getBlockModelResolver();
}
