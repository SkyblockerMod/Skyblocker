package de.hysky.skyblocker.compatibility.jei;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class SkyblockCraftingRecipeCategory extends CraftingRecipeCategory {
    private final Text title = Text.translatable("emi.category.skyblocker.skyblock");

    public SkyblockCraftingRecipeCategory(IGuiHelper guiHelper) {
        super(guiHelper);
    }

    @NotNull
    @Override
    public Text getTitle() {
        return title;
    }
}
