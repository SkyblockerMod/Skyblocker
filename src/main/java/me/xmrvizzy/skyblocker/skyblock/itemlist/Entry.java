package me.xmrvizzy.skyblocker.skyblock.itemlist;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Entry implements Comparable<Entry> {
    final ItemStack itemStack;
    final String internalName;
    final String familyName;
    final int tierNumber;
    final String clickCommand;

    public Entry(JsonObject obj) {
        this.internalName = obj.get("internalname").getAsString();
        this.familyName = this.internalName.replaceAll(".(\\d+)$", "");
        this.tierNumber = Integer.parseInt("0" + this.internalName.replaceAll("[^\\d]", ""));
        String cmd = obj.get("clickcommand").getAsString();
        this.clickCommand = cmd.isBlank() ? null : "/" + cmd + " " + this.internalName.replaceAll(";(\\d+)$", "");

        List<Pair<String, String>> injectors = new ArrayList<>();
        injectors.addAll(petData(internalName));

        NbtCompound root = new NbtCompound();
        root.put("Count", NbtByte.of((byte)1));

        String id = obj.get("itemid").getAsString();
        int damage = obj.get("damage").getAsInt();
        root.put("id", NbtString.of(ItemFixerUpper.convert(id, damage)));

        NbtCompound tag = new NbtCompound();
        root.put("tag", tag);

        NbtCompound extra = new NbtCompound();
        tag.put("ExtraAttributes", extra);
        extra.put("id", NbtString.of(internalName));

        // add enchantment glint
        if (internalName.contains("ENCHANTED")) {
            NbtList enchantments = new NbtList();
            enchantments.add(new NbtCompound());
            tag.put("Enchantments", enchantments);
        }

        NbtCompound display = new NbtCompound();
        tag.put("display", display);

        String name = injectData(obj.get("displayname").getAsString(), injectors);
        display.put("Name", NbtString.of(Text.Serializer.toJson(Text.of(name))));

        NbtList lore = new NbtList();
        display.put("Lore", lore);
        obj.get("lore").getAsJsonArray().forEach(el ->
                lore.add(NbtString.of(Text.Serializer.toJson(Text.of(injectData(el.getAsString(), injectors)))))
        );

        String nbttag = obj.get("nbttag").getAsString();
        // add skull texture
        Matcher skullMatcher = Pattern.compile("SkullOwner:\\{Id:\"(.{36})\",Properties:\\{textures:\\[0:\\{Value:\"(.+)\"}]}}").matcher(nbttag);
        if (skullMatcher.find()) {
            NbtCompound skullOwner = new NbtCompound();
            tag.put("SkullOwner", skullOwner);
            UUID uuid = UUID.fromString(skullMatcher.group(1));
            skullOwner.put("Id", NbtHelper.fromUuid(uuid));
            skullOwner.put("Name", NbtString.of(this.internalName));

            NbtCompound properties = new NbtCompound();
            skullOwner.put("Properties", properties);
            NbtList textures = new NbtList();
            properties.put("textures", textures);
            NbtCompound texture = new NbtCompound();
            textures.add(texture);
            texture.put("Value", NbtString.of(skullMatcher.group(2)));
        }
        // add leather armor dye color
        Matcher colorMatcher = Pattern.compile("color:(\\d+)").matcher(nbttag);
        if (colorMatcher.find()) {
            NbtInt color = NbtInt.of(Integer.parseInt(colorMatcher.group(1)));
            display.put("color", color);
        }

        this.itemStack = ItemStack.fromNbt(root);
        if (this.itemStack.getItem().equals(Items.AIR)) {
            System.err.println("ItemList: cannot register: " + this.internalName + " (" + id + ")");
        }
    }

    // TODO: fix stats for GOLDEN_DRAGON (lv1 -> lv200)
    private static List<Pair<String, String>> petData(String internalName) {
        List<Pair<String, String>> list = new ArrayList<>();

        String petName = internalName.split(";")[0];
        if (!internalName.contains(";") || !ItemRegistry.petNums.has(petName)) return list;

        list.add(new Pair<>("\\{LVL\\}", "1 ➡ 100"));

        final String[] rarities = {
                "COMMON",
                "UNCOMMON",
                "RARE",
                "EPIC",
                "LEGENDARY",
                "MYTHIC"
        };
        String rarity = rarities[Integer.parseInt(internalName.split(";")[1])];
        JsonObject data = ItemRegistry.petNums.get(petName).getAsJsonObject().get(rarity).getAsJsonObject();

        JsonObject statNumsMin = data.get("1").getAsJsonObject().get("statNums").getAsJsonObject();
        JsonObject statNumsMax = data.get("100").getAsJsonObject().get("statNums").getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entrySet = statNumsMin.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String key = entry.getKey();
            String left = "\\{" + key+ "\\}";
            String right = statNumsMin.get(key).getAsString() + " ➡ " + statNumsMax.get(key).getAsString();
            list.add(new Pair<>(left, right));
        }

        JsonArray otherNumsMin = data.get("1").getAsJsonObject().get("otherNums").getAsJsonArray();
        JsonArray otherNumsMax = data.get("100").getAsJsonObject().get("otherNums").getAsJsonArray();
        for (int i = 0; i < otherNumsMin.size(); ++i) {
            String left = "\\{" + i + "\\}";
            String right = otherNumsMin.get(i).getAsString() + " ➡ " + otherNumsMax.get(i).getAsString();
            list.add(new Pair(left, right));
        }

        return list;
    }

    private static String injectData(String string, List<Pair<String, String>> injectors) {
        for (Pair<String, String> injector : injectors)
            string = string.replaceAll(injector.getLeft(), injector.getRight());
        return string;
    }

    @Override
    public int compareTo(@NotNull Entry rhs) {
        if (this.familyName.equals(rhs.familyName)) return this.tierNumber - rhs.tierNumber;
        else return this.familyName.compareTo(rhs.familyName);
    }
}
