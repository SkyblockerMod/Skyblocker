package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookWidgetAccessor {
	@Invoker
	int invokeGetYOrigin();

	@Invoker
	int invokeGetXOrigin();
}
