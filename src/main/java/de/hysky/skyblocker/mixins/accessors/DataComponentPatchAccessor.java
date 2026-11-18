package de.hysky.skyblocker.mixins.accessors;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

@Mixin(DataComponentPatch.class)
public interface DataComponentPatchAccessor {
	@Accessor
	Reference2ObjectMap<DataComponentType<?>, Object> getMap();

	@Invoker("<init>")
	static DataComponentPatch invokeInit(Reference2ObjectMap<DataComponentType<?>, Object> map) {
		throw new UnsupportedOperationException();
	}
}
