package de.hysky.skyblocker.mixins.accessors;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

@Mixin(DataComponentPatch.class)
public interface DataComponentPatchAccessor {
	@Accessor
	Reference2ObjectMap<DataComponentType<?>, Optional<?>> getMap();

	@Invoker("<init>")
	static DataComponentPatch invokeInit(Reference2ObjectMap<DataComponentType<?>, Optional<?>> map) {
		throw new UnsupportedOperationException();
	}
}
