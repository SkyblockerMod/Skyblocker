package me.xmrvizzy.skyblocker.skyblock.api.records;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public record Items(
        Item[] armor,
        Item[][] wardrobe,
        Item[] inventory,
        Item[] enderchest,
        @SerializedName("talisman_bag") Item[] talismanBag,
        @SerializedName("fishing_bag") Item[] fishingBag,
        Item[] quiver,
        @SerializedName("potion_bag") Item[] potionBag,
        @SerializedName("personal_vault") Item[] personalVault,
        Item[] storage,
        Item[] weapons,
        Item[] hoes,
        Item[] pickaxes,
        Item[] rods,
        @SerializedName("highest_rarity_sword") Item highestRaritySword,
        @SerializedName("highest_rarity_bow") Item highestRarityBow,
        @SerializedName("highest_rarity_rod") Item highestRarityRod,
        @SerializedName("armor_set_rarity") String armorSetRarity
){
    public record Item(
            @SerializedName("Count") byte count,
            int damage,
            Tag tag,
            boolean isInactive,
            boolean inBackpack,
            Item[] containsItems
    ){
        public record Tag(
                @SerializedName("ExtraAttributes") ExtraAttributes extraAttributes,
                Display display,
                @SerializedName("SkullOwner") SkullOwner skullOwner,
                Enchant[] ench
        ){
            public record ExtraAttributes(String id, HashMap<String, Integer> enchantments){}
            public record Display(@SerializedName("Name") String name, @SerializedName("Lore") String[] lore, Integer color){}
            public record SkullOwner(
                    @SerializedName("Id") String id,
                    @SerializedName("Properties") Properties properties
            ){
                public record Properties(HashMap<String, String>[] textures){}
            }
            public record Enchant(int lvl, int id){}
        }
    }
}
