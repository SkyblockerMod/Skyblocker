package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;

@Mixin(CustomData.class)
public interface CustomDataAccessor {

	@Accessor
	CompoundTag getTag();
}
