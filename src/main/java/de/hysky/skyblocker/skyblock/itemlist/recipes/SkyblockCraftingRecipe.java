package de.hysky.skyblocker.skyblock.itemlist.recipes;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.ItemUtils;
import io.github.moulberry.repo.data.NEUCraftingRecipe;
import io.github.moulberry.repo.data.NEUIngredient;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SkyblockCraftingRecipe implements SkyblockRecipe {

    public static final Identifier IDENTIFIER = Identifier.of(SkyblockerMod.NAMESPACE, "skyblock_crafting");

    private final Text craftText;
    private final List<ItemStack> grid = new ArrayList<>(9);
    private final ItemStack result;

    public SkyblockCraftingRecipe(NEUCraftingRecipe neuCraftingRecipe) {
        this.craftText = neuCraftingRecipe.getExtraText() != null ? Text.literal(neuCraftingRecipe.getExtraText()) : Text.empty();
        for (NEUIngredient input : neuCraftingRecipe.getInputs()) {
            grid.add(SkyblockRecipe.getItemStack(input));
        }
        result = SkyblockRecipe.getItemStack(neuCraftingRecipe.getOutput());
    }

    public List<ItemStack> getGrid() {
        return grid;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public List<RecipeSlot> getInputSlots(int width, int height) {
        ScreenPos start = new ScreenPos(width / 2 - 58, height / 2 - (getExtraText().getString().isEmpty() ? 27: 32));
        List<RecipeSlot> toReturn = new ArrayList<>(9);
        for (int i = 0; i < grid.size(); i++) {
            int x = i % 3;
            int y = i / 3;
            toReturn.add(new RecipeSlot(start.x() + 1 + x * 18, start.y() + 1 + y * 18, grid.get(i)));
        }
        return toReturn;
    }

    @Override
    public List<RecipeSlot> getOutputSlots(int width, int height) {
        ScreenPos start = new ScreenPos(width / 2 - 58, height / 2 - (getExtraText().getString().isEmpty() ? 26: 31));
        return List.of(new RecipeSlot(start.x() + 95, start.y() + 19, result));
    }

    @Override
    public List<ItemStack> getInputs() {
        return grid;
    }

    @Override
    public List<ItemStack> getOutputs() {
        return List.of(result);
    }

    @Override
    public Text getExtraText() {
        return craftText;
    }

    @Override
    public Identifier getCategoryIdentifier() {
        return SkyblockCraftingRecipe.IDENTIFIER;
    }

    @Override
    public Identifier getRecipeIdentifier() {
        return Identifier.of("skyblock", ItemUtils.getItemId(getResult()).toLowerCase().replace(';', '_') + "_" + getResult().getCount());

    }

    @Override
    public @Nullable ScreenPos getArrowLocation(int width, int height) {
        ScreenPos start = new ScreenPos(width / 2 - 58, height / 2 - (getExtraText().getString().isEmpty() ? 26: 31));
        return new ScreenPos(start.x() + 60, start.y() + 18);
    }
}
