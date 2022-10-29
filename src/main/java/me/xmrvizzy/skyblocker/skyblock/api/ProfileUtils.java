package me.xmrvizzy.skyblocker.skyblock.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.skyblock.api.records.PlayerProfiles;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemFixerUpper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ProfileUtils {
    public static PlayerProfiles getProfiles(String name){
        try {
            URL url = new URL("https://sky.shiiyu.moe/api/v2/profile/" + name);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .create();
            return gson.fromJson(reader, PlayerProfiles.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<ItemStack> itemsFromApiInventory(me.xmrvizzy.skyblocker.skyblock.api.records.Items.Item[] items){
        List<ItemStack> inventory = new ArrayList<>();
        for (me.xmrvizzy.skyblocker.skyblock.api.records.Items.Item item : items){
            try{
                if (item.tag() != null){
                    JsonObject obj = new Gson().fromJson(Files.readString(Path.of("./config/skyblocker/items-repo/items/" + item.tag().extraAttributes().id() + ".json")), JsonObject.class);

                    NbtCompound root = new NbtCompound();
                    root.put("Count", NbtByte.of(item.count()));
                    root.put("id", NbtString.of(ItemFixerUpper.convertItemId(obj.get("itemid").getAsString(), obj.get("damage").getAsInt())));
                    NbtCompound tag = new NbtCompound();
                    root.put("tag", tag);

                    if (item.tag().ench() != null){
                        NbtList enchantments = new NbtList();
                        enchantments.add(new NbtCompound());
                        tag.put("Enchantments", enchantments);
                    }

                    NbtCompound extraAttributes = new NbtCompound();
                    tag.put("ExtraAttributes", extraAttributes);
                    extraAttributes.put("id", NbtString.of(item.tag().extraAttributes().id()));
                    if (item.tag().extraAttributes().enchantments() != null){
                        NbtCompound enchantments = new NbtCompound();
                        extraAttributes.put("enchantments", enchantments);
                        for (String enchant : item.tag().extraAttributes().enchantments().keySet()){
                            enchantments.put(enchant, NbtInt.of(item.tag().extraAttributes().enchantments().get(enchant)));
                        }
                    }

                    NbtCompound display = new NbtCompound();
                    tag.put("display", display);
                    display.put("Name", NbtString.of(Text.Serializer.toJson(Text.of(item.tag().display().name()))));
                    if (item.tag().display().lore() != null){
                        NbtList lore = new NbtList();
                        display.put("Lore", lore);
                        for (int i = 0; i < item.tag().display().lore().length; i++) {
                            if (i < item.tag().display().lore().length - 1)
                                lore.add(i, NbtString.of(Text.Serializer.toJson(Text.of(Arrays.stream(item.tag().display().lore()).toArray()[i].toString()))));
                        }
                    }
                    if (item.tag().display().color() != null){
                        display.put("color", NbtInt.of(item.tag().display().color()));
                    }

                    if (item.tag().skullOwner() != null){
                        NbtCompound skullOwner = new NbtCompound();
                        tag.put("SkullOwner", skullOwner);
                        UUID uuid = UUID.fromString(item.tag().skullOwner().id());
                        skullOwner.put("Id", NbtHelper.fromUuid(uuid));
                        skullOwner.put("Name", NbtString.of(item.tag().extraAttributes().id()));

                        NbtCompound properties = new NbtCompound();
                        skullOwner.put("Properties", properties);
                        NbtList textures = new NbtList();
                        properties.put("textures", textures);
                        NbtCompound texture = new NbtCompound();
                        textures.add(texture);
                        texture.put("Value", NbtString.of(item.tag().skullOwner().properties().textures()[0].get("Value")));
                    }
                    inventory.add(ItemStack.fromNbt(root));
                } else {
                    inventory.add(Items.AIR.getDefaultStack());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return inventory;
    }
}
