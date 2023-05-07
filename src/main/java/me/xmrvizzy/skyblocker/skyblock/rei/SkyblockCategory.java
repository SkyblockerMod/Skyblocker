package me.xmrvizzy.skyblocker.skyblock.rei;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;

import java.util.Locale;

/**
 * Skyblock recipe category class for REI
 */
public class SkyblockCategory implements DisplayCategory<SkyblockCraftingDisplay> {
    @Override
    public CategoryIdentifier<SkyblockCraftingDisplay> getCategoryIdentifier() {
        return SkyblockerREIClientPlugin.SKYBLOCK;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("key.categories.skyblocker");
    }

    @Override
    public Renderer getIcon() {
        // TODO separate icon from quickNav
        SkyblockerConfig.ItemData iconItem = SkyblockerConfig.get().quickNav.button7.item;
        String nbtString = "{id:\"minecraft:" + iconItem.itemName.toLowerCase(Locale.ROOT) + "\",Count:1";
        if (iconItem.nbt.length() > 2) nbtString += "," + iconItem.nbt;
        nbtString += "}";
        try {
            return EntryStacks.of(ItemStack.fromNbt(StringNbtReader.parse(nbtString)));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
