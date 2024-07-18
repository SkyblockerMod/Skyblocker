package de.hysky.skyblocker.skyblock.itemlist.recipes;

import de.hysky.skyblocker.SkyblockerMod;
import io.github.moulberry.repo.data.NEUForgeRecipe;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SkyblockForgeRecipe implements SkyblockRecipe {

    public static final Identifier IDENTIFIER = Identifier.of(SkyblockerMod.NAMESPACE, "skyblock_forge");

    private final List<ItemStack> inputs;
    private final ItemStack output;

    public SkyblockForgeRecipe(NEUForgeRecipe forgeRecipe) {
        inputs = forgeRecipe.getInputs().stream().map(SkyblockRecipe::getItemStack).toList();
        output = SkyblockRecipe.getItemStack(forgeRecipe.getOutputStack());
    }


    @Override
    public List<RecipeSlot> getInputSlots(int width, int height) {
        List<RecipeSlot> out = new ArrayList<>();
        int centerX = width / 2;
        int centerY = height / 2;
        float radius = getRadius();
        for (int i = 0; i < inputs.size(); i++) {
            float angle = 2 * MathHelper.PI * (float) i / (float) inputs.size();
            int x = (int) (MathHelper.cos(angle) * radius);
            int y = (int) (MathHelper.sin(angle) * radius);
            out.add(new RecipeSlot(Math.max(centerX / 2, (int) radius + 14) - 9 + x, centerY - 9 + y, inputs.get(i)));
        }
        return out;
    }

    private float getRadius() {
        return Math.max(26 * inputs.size() / (2 * MathHelper.PI), 20);
    }

    @Override
    public List<RecipeSlot> getOutputSlots(int width, int height) {
        return List.of(new RecipeSlot(width / 2 + 37, height / 2 - 9, output));
    }

    @Override
    public List<ItemStack> getInputs() {
        return inputs;
    }

    @Override
    public List<ItemStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public Text getExtraText() {
        return Text.empty();
    }

    @Override
    public Identifier getCategoryIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public @Nullable ScreenPos getArrowLocation(int width, int height) {
        float v = (3 * width) / 4.f;
        return new ScreenPos((int) ((v + getRadius() + 9 + 37 - 24) / 2), height / 2 - 9);
    }
}
