package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderDispatcher.class)
public interface EntityRenderDispatcherAccessor {
	@Accessor("equipmentModelLoader")
	EquipmentModelLoader getEquipmentModelLoader();
}
