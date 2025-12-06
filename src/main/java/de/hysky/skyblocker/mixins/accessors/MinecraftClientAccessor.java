package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.ProfileKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
	@Accessor
	ProfileKeys getProfileKeys();
}
