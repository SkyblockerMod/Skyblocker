package de.hysky.skyblocker.mixins.accessors;

import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookWidget.class)
public interface RecipeBookWidgetAccessor {
    @Invoker
    int invokeGetTop();

    @Invoker
    int invokeGetLeft();
}
