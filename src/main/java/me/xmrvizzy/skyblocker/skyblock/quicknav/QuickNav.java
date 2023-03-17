package me.xmrvizzy.skyblocker.skyblock.quicknav;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;

import java.util.ArrayList;
import java.util.List;

public class QuickNav {
    private static final String skyblockHubIconNbt = "{id:\"minecraft:player_head\",Count:1,tag:{SkullOwner:{Id:[I;-300151517,-631415889,-1193921967,-1821784279],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}}}}";
    private static final String dungeonHubIconNbt = "{id:\"minecraft:player_head\",Count:1,tag:{SkullOwner:{Id:[I;1605800870,415127827,-1236127084,15358548],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MWQ1YjI3M2ZmMGJjNTBjOTYwYjJjZDg2ZWVmMWM0MGExYjk0MDMyYWU3MWU3NTQ3NWE1NjhhODI1NzQyMSJ9fX0=\"}]}}}}";
    public static List<QuickNavButton> init(String screenTitle) {
        List<QuickNavButton> buttons = new ArrayList<>();
        try {
            buttons.add(new QuickNavButton(0, screenTitle.contains("Your Skills"), "skills", new ItemStack(Items.DIAMOND_SWORD)));
            buttons.add(new QuickNavButton(1, screenTitle.contains("Collection"), "collection", new ItemStack(Items.PAINTING)));

            buttons.add(new QuickNavButton(3, screenTitle.contains("Pets"), "pets", new ItemStack(Items.BONE)));
            buttons.add(new QuickNavButton(4, screenTitle.contains("Wardrobe"), "wardrobe", ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:leather_chestplate\", Count:1, tag:{display:{color:8991416}}}"))));
            buttons.add(new QuickNavButton(5, screenTitle.contains("Storage"), "storage", new ItemStack(Items.ENDER_CHEST)));

            buttons.add(new QuickNavButton(6, false, "warp hub", ItemStack.fromNbt(StringNbtReader.parse(skyblockHubIconNbt))));
            buttons.add(new QuickNavButton(7, false, "warp dungeon_hub", ItemStack.fromNbt(StringNbtReader.parse(dungeonHubIconNbt))));

            buttons.add(new QuickNavButton(9, screenTitle.contains("Enchant Item"), "etable", new ItemStack(Items.ENCHANTING_TABLE)));
            buttons.add(new QuickNavButton(10, screenTitle.contains("Anvil"), "anvil", new ItemStack(Items.ANVIL)));
            buttons.add(new QuickNavButton(11, screenTitle.contains("Craft Item"), "craft", new ItemStack(Items.CRAFTING_TABLE)));
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return buttons;
    }
}
