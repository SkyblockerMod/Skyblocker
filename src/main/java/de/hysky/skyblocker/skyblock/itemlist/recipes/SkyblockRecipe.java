package de.hysky.skyblocker.skyblock.itemlist.recipes;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import io.github.moulberry.repo.data.NEUIngredient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface SkyblockRecipe {

    Logger LOGGER = LoggerFactory.getLogger(SkyblockCraftingRecipe.class);



    static ItemStack getItemStack(NEUIngredient input) {
        if (input != NEUIngredient.SENTINEL_EMPTY) {
            ItemStack stack = ItemRepository.getItemStack(input.getItemId());
            if (stack != null) {
                return stack.copyWithCount((int) input.getAmount());
            } else {
                LOGGER.warn("[Skyblocker Recipe] Unable to find item {}", input.getItemId());
            }
        }
        return Items.AIR.getDefaultStack();
    }

    /**
     * @param width the available area's width
     * @param height the available area's height
     * @return list of slots
     */
    List<RecipeSlot> getInputSlots(int width, int height);

    /**
     * @param width the available area's width
     * @param height the available area's height
     * @return list of slots
     */
    List<RecipeSlot> getOutputSlots(int width, int height);

    default @Nullable ScreenPos getArrowLocation(int width, int height) {
        return null;
    }

    List<ItemStack> getInputs();
    List<ItemStack> getOutputs();

    /**
     * Render some extra things, i.e an entity
     * @param width available width
     * @param height available height
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    default void render(DrawContext context, int width, int height, double mouseX, double mouseY) {};

    /**
     * Extra text like collection requirements
     * @return the text
     */
    Text getExtraText();

    Identifier getCategoryIdentifier();

    record RecipeSlot(int x, int y, ItemStack stack, boolean showBackground) {
        public RecipeSlot(int x, int y, ItemStack stack) {
            this(x, y, stack, true);
        }
    }
}
