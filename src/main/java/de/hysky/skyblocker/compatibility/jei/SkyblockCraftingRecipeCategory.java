package de.hysky.skyblocker.compatibility.jei;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.ItemUtils;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class SkyblockCraftingRecipeCategory extends CraftingRecipeCategory {
    @SuppressWarnings({"unchecked", "RedundantCast", "rawtypes"})
    private static final RecipeType<RecipeEntry<CraftingRecipe>> SKYBLOCK_RECIPE = new RecipeType<>(Identifier.of(SkyblockerMod.NAMESPACE, "skyblock"), (Class<? extends RecipeEntry<CraftingRecipe>>) (Class) RecipeEntry.class);
    private final Text title = Text.translatable("emi.category.skyblocker.skyblock");
    private final IDrawable icon;

    public SkyblockCraftingRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper);
        icon = guiHelper.createDrawableItemStack(ItemUtils.getSkyblockerStack());
    }

    @Override
    @NotNull
    public RecipeType<RecipeEntry<CraftingRecipe>> getRecipeType() {
        return SKYBLOCK_RECIPE;
    }

    @NotNull
    @Override
    public Text getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }
}