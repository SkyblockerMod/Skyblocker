package me.xmrvizzy.skyblocker.skyblock.quicknav;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;

import java.util.ArrayList;
import java.util.List;

public class QuickNav {
    public static List<QuickNavButton> init(String title, int left_x, int right_x, int top_y, int bottom_y) {
        List<QuickNavButton> buttons = new ArrayList<>();
        try {
            buttons.add(new QuickNavButton(0, title.contains("Your Skills"), "/skills", new ItemStack(Items.DIAMOND_SWORD)));
            buttons.add(new QuickNavButton(1, title.contains("Collection"), "/collection", new ItemStack(Items.PAINTING)));

            buttons.add(new QuickNavButton(3, title.contains("Pets"), "/pets", new ItemStack(Items.BONE)));
            buttons.add(new QuickNavButton(4, title.contains("Wardrobe"), "/wardrobe", ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:leather_chestplate\", Count:1, tag:{display:{color:8991416}}}"))));
            buttons.add(new QuickNavButton(5, title.contains("Storage"), "/storage", new ItemStack(Items.ENDER_CHEST)));

            buttons.add(new QuickNavButton(6, false, "/warp hub", new ItemStack(Items.COMPASS)));
            buttons.add(new QuickNavButton(7, false, "/warp dungeon_hub", new ItemStack(Items.WITHER_SKELETON_SKULL)));

            buttons.add(new QuickNavButton(9, title.contains("Enchant Item"), "/etable", new ItemStack(Items.ENCHANTING_TABLE)));
            buttons.add(new QuickNavButton(10, title.contains("Anvil"), "/anvil", new ItemStack(Items.ANVIL)));
            buttons.add(new QuickNavButton(11, title.contains("Craft Item"), "/craft", new ItemStack(Items.CRAFTING_TABLE)));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return buttons;
    }
}
