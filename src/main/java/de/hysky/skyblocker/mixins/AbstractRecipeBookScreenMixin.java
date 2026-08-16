package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookMenu;

import de.hysky.skyblocker.injected.RecipeBookHolder;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin<T extends RecipeBookMenu> extends AbstractContainerScreen<T> implements RecipeBookHolder {
	@Shadow
	@Final
	private RecipeBookComponent<?> recipeBookComponent;

	public AbstractRecipeBookScreenMixin(T menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	public void registerRecipeBookToggleCallback(Runnable callback) {}

	@Override
	public RecipeBookComponent<?> getRecipeBookComponent() {
		return recipeBookComponent;
	}
}
