package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockRecipe;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Skyblock recipe category class for REI
 */
public class SkyblockRecipeCategory implements DisplayCategory<SkyblockRecipeDisplay> {

    private final Identifier identifier;
    private final Text title;
    private final ItemStack icon;
    private final int height;

    public SkyblockRecipeCategory(Identifier identifier, Text title, ItemStack icon, int height) {
        this.identifier = identifier;
        this.title = title;
        this.icon = icon;
        this.height = height;
    }

    @Override
    public CategoryIdentifier<? extends SkyblockRecipeDisplay> getCategoryIdentifier() {
        return CategoryIdentifier.of(identifier);
    }

    @Override
    public int getDisplayHeight() {
        return height;
    }

    @Override
    public Text getTitle() {
        return title;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(icon);
    }

    /**
     * Draws display for SkyblockCraftingDisplay
     *
     * @param display the display
     * @param bounds  the bounds of the display, configurable with overriding the width, height methods.
     */
    @Override
    public List<Widget> setupDisplay(SkyblockRecipeDisplay display, Rectangle bounds) {
        List<Widget> out = new ArrayList<>();
        out.add(Widgets.createRecipeBase(bounds));
        SkyblockRecipe recipe = display.getRecipe();
        for (SkyblockRecipe.RecipeSlot inputSlot : recipe.getInputSlots(bounds.getWidth(), bounds.getHeight())) {
            out.add(Widgets.createSlot(new Point(inputSlot.x() + bounds.getX(), inputSlot.y() + bounds.getY()))
                    .markInput()
                    .backgroundEnabled(inputSlot.showBackground())
                    .entry(EntryStacks.of(inputSlot.stack())));
        }
        for (SkyblockRecipe.RecipeSlot outputSlot : recipe.getOutputSlots(bounds.getWidth(), bounds.getHeight())) {
            out.add(Widgets.createSlot(new Point(outputSlot.x() + bounds.getX(), outputSlot.y() + bounds.getY()))
                    .markOutput()
                    .backgroundEnabled(outputSlot.showBackground())
                    .entry(EntryStacks.of(outputSlot.stack())));
        }
        out.add(Widgets.createDrawableWidget((context, mouseX, mouseY, delta) -> {
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(bounds.getX(), bounds.getY(), 0.f);
            recipe.render(context, bounds.getWidth(), bounds.getHeight(), mouseX - bounds.getX(), mouseY - bounds.getY());
            matrices.pop();
        }));
        ScreenPos arrowLocation = recipe.getArrowLocation(bounds.getWidth(), bounds.getHeight());
        if (arrowLocation != null)
            out.add(Widgets.createArrow(new Point(arrowLocation.x() + bounds.getX(), arrowLocation.y() + bounds.getY())));
        out.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.getCenterY() + 24), recipe.getExtraText()));
        return out;
    }
}
