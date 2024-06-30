package de.hysky.skyblocker.mixins.stp;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.resource.ResourcePackProfile;

@Mixin(targets = "net.minecraft.client.gui.screen.pack.ResourcePackOrganizer$AbstractPack")
public interface AbstractPackAccessor {

	@Accessor
	ResourcePackProfile getProfile();
}
