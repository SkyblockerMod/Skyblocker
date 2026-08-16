package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.EquipmentAssetManager;

@Mixin(EntityRenderDispatcher.class)
public interface EntityRenderDispatcherAccessor {
	@Accessor
	EquipmentAssetManager getEquipmentAssets();
}
