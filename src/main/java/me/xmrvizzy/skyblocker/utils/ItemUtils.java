package me.xmrvizzy.skyblocker.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ItemUtils {
    private final static Pattern WHITESPACES = Pattern.compile("^\\s*$");

    public static List<Text> getTooltip(ItemStack item) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player == null || item == null ? Collections.emptyList() : item.getTooltip(client.player, TooltipContext.Default.BASIC);
    }

    public static List<String> getTooltipStrings(ItemStack item) {
        List<Text> lines = getTooltip(item);
        List<String> list = new ArrayList<>();

        for (Text line : lines) {
            String string = line.getString();
            if (!WHITESPACES.matcher(string).matches())
                list.add(string);
        }

        return list;
    }

    public static ItemStack getSkyblockerStack() {
        try {
            return ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:player_head\",Count:1,tag:{SkullOwner:{Id:[I;-300151517,-631415889,-1193921967,-1821784279],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}}}}"));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getItemId(ItemStack itemStack) {
        if (itemStack == null) return null;

        NbtCompound nbt = itemStack.getNbt();
        if (nbt != null && nbt.contains("ExtraAttributes")) {
            NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
            if (extraAttributes.contains("id")) {
                return extraAttributes.getString("id");
            }
        }

        return null;
    }
}
