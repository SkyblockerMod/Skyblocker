package me.xmrvizzy.skyblocker.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemUtils {

    public static List<String> getLore(ItemStack item) {
        if (item.hasTag() && item.getTag().contains("display", 10)) {
            CompoundTag tag = item.getTag().getCompound("display");

            if (tag.contains("Lore", 9)) {
                ListTag lore = tag.getList("Lore", 8);

                List<String> list = new ArrayList<>();
                for (int line = 0; line < lore.size(); line++) {
                    String string = lore.getString(line);
                    try {
                        Text text = Text.Serializer.fromJson(string);
                        if (text != null) {
                            string = text.getString();
                            if (!string.replaceAll("\\s+","").isEmpty())
                                list.add(string);
                        }
                    } catch (Exception e) {}
                }

                return list;
            }
        }

        return Collections.emptyList();
    }
}