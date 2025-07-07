package de.hysky.skyblocker.skyblock.itemlist.recipes;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import io.github.moulberry.repo.data.NEUIngredient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public interface SkyblockRecipe {

    Logger LOGGER = LoggerFactory.getLogger(SkyblockRecipe.class);
    NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);



    static ItemStack getItemStack(NEUIngredient input) {
        if (input != NEUIngredient.SENTINEL_EMPTY) {
            ItemStack stack = ItemRepository.getItemStack(input.getItemId());
            if (stack != null) {
                return stack.copyWithCount((int) input.getAmount());
            } else if (input.getItemId().equals("SKYBLOCK_COIN")) {
                ItemStack itemStack = new ItemStack(Items.GOLD_NUGGET);
                itemStack.set(DataComponentTypes.ITEM_NAME, Text.literal("Skyblock Coins").formatted(Formatting.GOLD));
                itemStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
                String format = NUMBER_FORMAT.format(input.getAmount());
                itemStack.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal(format).formatted(Formatting.GOLD).append(Text.literal(" coins.")))));
                return itemStack;
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
    default void render(DrawContext context, int width, int height, double mouseX, double mouseY) {}

    /**
     * Extra text like collection requirements
     */
    Text getExtraText();

	/**
	 * Identifier used for REI, EMI. Also used in the recipe book for the name
	 */
    Identifier getCategoryIdentifier();

	/**
	 * Used for EMI.
	 */
    Identifier getRecipeIdentifier();

    record RecipeSlot(int x, int y, ItemStack stack, boolean showBackground) {
        public RecipeSlot(int x, int y, ItemStack stack) {
            this(x, y, stack, true);
        }
    }
}
