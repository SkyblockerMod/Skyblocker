package me.xmrvizzy.skyblocker.skyblock.rei;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;

import java.util.ArrayList;
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

        displayRegistry.registerDisplayGenerator(SKYBLOCK, new SkyblockCraftingDisplayGenerator());

    }

    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        ArrayList<EntryStack<ItemStack>> entries = new ArrayList<>();
        ItemRegistry.getRecipes().forEach(recipe -> entries.add(EntryStacks.of(recipe.getResult())));
        entryRegistry.addEntries(entries);
    }
}
