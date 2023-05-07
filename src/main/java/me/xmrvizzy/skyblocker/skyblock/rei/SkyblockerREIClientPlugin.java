package me.xmrvizzy.skyblocker.skyblock.rei;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import me.xmrvizzy.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * REI integration
 */
public class SkyblockerREIClientPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<SkyblockCraftingDisplay> SKYBLOCK = CategoryIdentifier.of(SkyblockerMod.NAMESPACE, "skyblock");

    @Override
    public void registerCategories(CategoryRegistry categoryRegistry) {
        // TODO separate icon from quickNav
        SkyblockerConfig.ItemData iconItem = SkyblockerConfig.get().quickNav.button7.item;
        String nbtString = "{id:\"minecraft:" + iconItem.itemName.toLowerCase(Locale.ROOT) + "\",Count:1";
        if (iconItem.nbt.length() > 2) nbtString += "," + iconItem.nbt;
        nbtString += "}";

        try {
            categoryRegistry.addWorkstations(SKYBLOCK, EntryStacks.of(ItemStack.fromNbt(StringNbtReader.parse(nbtString))));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        categoryRegistry.add(new SkyblockCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry displayRegistry) {

        ViewSearchBuilder builder = ViewSearchBuilder.builder();
        builder.addCategory(SKYBLOCK);

        System.out.println(displayRegistry.getCategoryDisplayGenerators(SKYBLOCK).isEmpty());


        displayRegistry.registerDisplayGenerator(SKYBLOCK, new SkyblockCraftingDisplayGenerator());

        for (SkyblockCraftingRecipe recipe : ItemRegistry.getRecipes()) {
            List<EntryIngredient> inputs = new ArrayList<>();
            List<EntryIngredient> outputs = new ArrayList<>();

            ArrayList<EntryStack<ItemStack>> inputEntryStacks = new ArrayList<>();
            recipe.getGrid().forEach((item) -> inputEntryStacks.add(EntryStacks.of(item)));

            for (EntryStack<ItemStack> entryStack : inputEntryStacks) {
                inputs.add(EntryIngredient.of(entryStack));
            }
//            inputs.add(EntryIngredient.of(inputEntryStacks.get(0)));
            outputs.add(EntryIngredient.of(EntryStacks.of(recipe.getResult())));
            displayRegistry.add(new SkyblockCraftingDisplay(null, inputs, outputs));
        }

    }

    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        ArrayList<EntryStack<ItemStack>> entries = new ArrayList<>();
        ItemRegistry.getRecipes().forEach(recipe -> entries.add(EntryStacks.of(recipe.getResult())));
        entryRegistry.addEntries(entries);
    }

    @Override
    public void registerScreens(ScreenRegistry screenRegistry) {
        screenRegistry.registerFocusedStack(new SkyblockFocusedStackProvider());
    }
}
