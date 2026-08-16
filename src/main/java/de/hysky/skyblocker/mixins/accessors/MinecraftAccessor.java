package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.renderer.block.BlockModelResolver;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Accessor("profileKeyPairManager")
	ProfileKeyPairManager getProfileKeyPairManagerField();

	@Accessor
	BlockModelResolver getBlockModelResolver();
}
