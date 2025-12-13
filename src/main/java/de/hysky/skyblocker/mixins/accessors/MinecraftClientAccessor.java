package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {
	@Accessor
	ProfileKeyPairManager getProfileKeyPairManager();
}
