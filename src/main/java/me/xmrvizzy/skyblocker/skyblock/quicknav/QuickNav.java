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

        buttons.add(new QuickNavButton(left_x + 29 * 0, top_y, QuickNavButton.Type.TOP, title.contains("Your Skills"), "/skills", new ItemStack(Items.DIAMOND_SWORD)));
        buttons.add(new QuickNavButton(left_x + 29 * 1, top_y, QuickNavButton.Type.TOP, title.contains("Collection"), "/collection", new ItemStack(Items.PAINTING)));

        buttons.add(new QuickNavButton(right_x - 29 * 1, bottom_y, QuickNavButton.Type.BOTTOM, title.contains("Craft Item"), "/craft", new ItemStack(Items.CRAFTING_TABLE)));
        buttons.add(new QuickNavButton(right_x - 29 * 2, bottom_y, QuickNavButton.Type.BOTTOM, title.contains("Anvil"), "/anvil", new ItemStack(Items.ANVIL)));
        buttons.add(new QuickNavButton(right_x - 29 * 3, bottom_y, QuickNavButton.Type.BOTTOM, title.contains("Enchant Item"), "/etable", new ItemStack(Items.ENCHANTING_TABLE)));

        buttons.add(new QuickNavButton(left_x + 29 * 0, bottom_y, QuickNavButton.Type.BOTTOM, false, "/warp hub", new ItemStack(Items.COMPASS)));
        buttons.add(new QuickNavButton(left_x + 29 * 1, bottom_y, QuickNavButton.Type.BOTTOM, false, "/warp dungeon_hub", new ItemStack(Items.WITHER_SKELETON_SKULL)));

        buttons.add(new QuickNavButton(right_x - 29 * 1, top_y, QuickNavButton.Type.TOP, title.contains("Storage"), "/storage", new ItemStack(Items.ENDER_CHEST)));
        try {
            buttons.add(new QuickNavButton(right_x - 29 * 2, top_y, QuickNavButton.Type.TOP, title.contains("Wardrobe"), "/wardrobe", ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:leather_chestplate\", Count:1, tag:{display:{color:8991416}}}"))));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        buttons.add(new QuickNavButton(right_x - 29 * 3, top_y, QuickNavButton.Type.TOP, title.contains("Pets"), "/pets", new ItemStack(Items.BONE)));

        return buttons;
    }
}
