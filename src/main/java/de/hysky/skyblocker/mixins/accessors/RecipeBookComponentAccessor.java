package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccessor {
	@Invoker
	int invokeGetYOrigin();

	@Invoker
	int invokeGetXOrigin();
}
