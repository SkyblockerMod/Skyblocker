package me.xmrvizzy.skyblocker.skyblock.itemlist;

import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public class ItemFixerUpper {
    private final static Map<String, String> MAPPING = Map.ofEntries(
            Map.entry("minecraft:golden_rail", "minecraft:powered_rail"),
            Map.entry("minecraft:lit_pumpkin", "minecraft:jack_o_lantern"),
            Map.entry("minecraft:snow_layer", "minecraft:snow"),
            Map.entry("minecraft:hardened_clay", "minecraft:terracotta"),
            Map.entry("minecraft:speckled_melon", "minecraft:glistering_melon_slice"),
            Map.entry("minecraft:mob_spawner", "minecraft:spawner"),
            Map.entry("minecraft:brick_block", "minecraft:bricks"),
            Map.entry("minecraft:deadbush", "minecraft:dead_bush"),
            Map.entry("minecraft:slime", "minecraft:slime_block"),
            Map.entry("minecraft:melon_block", "minecraft:melon"),
            Map.entry("minecraft:reeds", "minecraft:sugar_cane"),
            Map.entry("minecraft:yellow_flower", "minecraft:dandelion"),
            Map.entry("minecraft:firework_charge", "minecraft:firework_star"),
            Map.entry("minecraft:noteblock", "minecraft:note_block"),
            Map.entry("minecraft:web", "minecraft:cobweb"),
            Map.entry("minecraft:fireworks", "minecraft:firework_rocket"),
            Map.entry("minecraft:netherbrick", "minecraft:nether_brick"),
            Map.entry("minecraft:stained_hardened_clay", "minecraft:terracotta"),
            Map.entry("minecraft:quartz_ore", "minecraft:nether_quartz_ore"),
            Map.entry("minecraft:tallgrass", "minecraft:grass"),
            Map.entry("minecraft:stone_slab2", "minecraft:red_sandstone_slab"),
            Map.entry("minecraft:waterlily", "minecraft:lily_pad"),
            Map.entry("minecraft:stone_stairs", "minecraft:cobblestone_stairs")
    );

    private final static String[] DYE_COLORS = {
            "minecraft:ink_sac",
            "minecraft:red_dye",
            "minecraft:green_dye",
            "minecraft:cocoa_beans",
            "minecraft:lapis_lazuli",
            "minecraft:purple_dye",
            "minecraft:cyan_dye",
            "minecraft:light_gray_dye",
            "minecraft:gray_dye",
            "minecraft:pink_dye",
            "minecraft:lime_dye",
            "minecraft:yellow_dye",
            "minecraft:light_blue_dye",
            "minecraft:magenta_dye",
            "minecraft:orange_dye",
            "minecraft:bone_meal"
    };

    private final static String[] BLOCK_COLORS = {
            "white_",
            "orange_",
            "magenta_",
            "light_blue_",
            "yellow_",
            "lime_",
            "pink_",
            "gray_",
            "light_gray_",
            "cyan_",
            "purple_",
            "blue_",
            "brown_",
            "green_",
            "red_",
            "black_"
    };

    private final static String[] TREE_VARIANTS = {
            "oak_",
            "spruce_",
            "birch_",
            "jungle_",
            "acacia_",
            "dark_oak_"
    };

    private final static String[] STONE_BRICK_VARIANTS = {
            "minecraft:stone_bricks",
            "minecraft:mossy_stone_bricks",
            "minecraft:cracked_stone_bricks",
            "minecraft:chiseled_stone_bricks"
    };

    private final static String[] RED_FLOWER_VARIANTS = {
            "minecraft:poppy",
            "minecraft:blue_orchid",
            "minecraft:allium",
            "minecraft:azure_bluet",
            "minecraft:red_tulip",
            "minecraft:orange_tulip",
            "minecraft:white_tulip",
            "minecraft:pink_tulip",
            "minecraft:oxeye_daisy"
    };

    private final static String[] DOUBLE_PLANT_VARIANTS = {
            "minecraft:sunflower",
            "minecraft:lilac",
            "minecraft:tall_grass",
            "minecraft:large_fern",
            "minecraft:rose_bush",
            "minecraft:peony"
    };

    private final static Map<Integer, String> SPAWN_EGG_VARIANTS = Map.ofEntries(
            Map.entry(50, "minecraft:creeper_spawn_egg"),
            Map.entry(51, "minecraft:skeleton_spawn_egg"),
            Map.entry(52, "minecraft:spider_spawn_egg"),
            Map.entry(54, "minecraft:zombie_spawn_egg"),
            Map.entry(55, "minecraft:slime_spawn_egg"),
            Map.entry(56, "minecraft:ghast_spawn_egg"),
            Map.entry(57, "minecraft:zombified_piglin_spawn_egg"),
            Map.entry(58, "minecraft:enderman_spawn_egg"),
            Map.entry(59, "minecraft:cave_spider_spawn_egg"),
            Map.entry(60, "minecraft:silverfish_spawn_egg"),
            Map.entry(61, "minecraft:blaze_spawn_egg"),
            Map.entry(62, "minecraft:magma_cube_spawn_egg"),
            Map.entry(65, "minecraft:bat_spawn_egg"),
            Map.entry(66, "minecraft:witch_spawn_egg"),
            Map.entry(67, "minecraft:endermite_spawn_egg"),
            Map.entry(68, "minecraft:guardian_spawn_egg"),
            Map.entry(90, "minecraft:pig_spawn_egg"),
            Map.entry(91, "minecraft:sheep_spawn_egg"),
            Map.entry(92, "minecraft:cow_spawn_egg"),
            Map.entry(93, "minecraft:chicken_spawn_egg"),
            Map.entry(94, "minecraft:squid_spawn_egg"),
            Map.entry(95, "minecraft:wolf_spawn_egg"),
            Map.entry(96, "minecraft:mooshroom_spawn_egg"),
            Map.entry(98, "minecraft:ocelot_spawn_egg"),
            Map.entry(100, "minecraft:horse_spawn_egg"),
            Map.entry(101, "minecraft:rabbit_spawn_egg"),
            Map.entry(120, "minecraft:villager_spawn_egg")
    );

    private final static String[] SKULL_VARIANTS = {
            "minecraft:skeleton_skull",
            "minecraft:wither_skeleton_skull",
            "minecraft:zombie_head",
            "minecraft:player_head",
            "minecraft:creeper_head"
    };

    private final static String[] FISH_VARIANTS = {
            "minecraft:cod",
            "minecraft:salmon",
            "minecraft:tropical_fish",
            "minecraft:pufferfish"
    };

    private final static String[] COOKED_FISH_VARIANTS = {
            "minecraft:cooked_cod",
            "minecraft:cooked_salmon"
    };

    private final static String[] STONE_VARIANTS = {
            "minecraft:stone",
            "minecraft:granite",
            "minecraft:polished_granite",
            "minecraft:diorite",
            "minecraft:polished_diorite",
            "minecraft:andesite",
            "minecraft:polished_andesite"
    };

    private final static String[] STONE_SLAB_VARIANTS = {
            "minecraft:smooth_stone_slab",
            "minecraft:sandstone_slab",
            "minecraft:barrier", // doesn't exist
            "minecraft:cobblestone_slab",
            "minecraft:brick_slab",
            "minecraft:stone_brick_slab",
            "minecraft:nether_brick_slab",
            "minecraft:quartz_slab"
    };

    private final static String[] COBBLESTONE_WALL_VARIANTS = {
            "minecraft:cobblestone_wall",
            "minecraft:mossy_cobblestone_wall"
    };

    private final static String[] DIRT_VARIANTS = {
            "minecraft:dirt",
            "minecraft:coarse_dirt",
            "minecraft:podzol"
    };

    private final static String[] SPONGE_VARIANTS = {
            "minecraft:sponge",
            "minecraft:wet_sponge"
    };

    private final static String[] PRISMARINE_VARIANTS = {
            "minecraft:prismarine",
            "minecraft:prismarine_bricks",
            "minecraft:dark_prismarine"
    };

    // TODO: map potions to their correct colors

    public static String convertItemId(String id, int damage) {
        if (id.equals("minecraft:dye")) return DYE_COLORS[damage];
        if (id.equals("minecraft:log2")) return "minecraft:" + TREE_VARIANTS[damage + 4] + "log";
        if (id.equals("minecraft:leaves2")) return "minecraft:" + TREE_VARIANTS[damage + 4] + "leaves";
        if (id.equals("minecraft:stonebrick")) return STONE_BRICK_VARIANTS[damage];
        if (id.equals("minecraft:red_flower")) return RED_FLOWER_VARIANTS[damage];
        if (id.equals("minecraft:double_plant")) return DOUBLE_PLANT_VARIANTS[damage];
        if (id.equals("minecraft:spawn_egg")) return SPAWN_EGG_VARIANTS.getOrDefault(damage, "minecraft:ghast_spawn_egg");
        if (id.equals("minecraft:banner")) return "minecraft:" + BLOCK_COLORS[15 - damage] + "banner";
        if (id.equals("minecraft:skull")) return SKULL_VARIANTS[damage];
        if (id.equals("minecraft:fish")) return FISH_VARIANTS[damage];
        if (id.equals("minecraft:cooked_fish")) return COOKED_FISH_VARIANTS[damage];
        if (id.equals("minecraft:stone")) return STONE_VARIANTS[damage];
        if (id.equals("minecraft:stone_slab")) return STONE_SLAB_VARIANTS[damage];
        if (id.equals("minecraft:cobblestone_wall")) return COBBLESTONE_WALL_VARIANTS[damage];
        if (id.equals("minecraft:dirt")) return DIRT_VARIANTS[damage];
        if (id.equals("minecraft:sponge")) return SPONGE_VARIANTS[damage];
        if (id.equals("minecraft:prismarine")) return PRISMARINE_VARIANTS[damage];

        id = MAPPING.getOrDefault(id, id);
        if (Registry.ITEM.get(new Identifier(id)).equals(Items.AIR)) {
            String shortId = id.split(":")[1];
            if (damage < BLOCK_COLORS.length && !Registry.ITEM.get(new Identifier("minecraft:" + BLOCK_COLORS[damage] + shortId)).equals(Items.AIR))
                return "minecraft:" + BLOCK_COLORS[damage] + shortId;
            if (damage < TREE_VARIANTS.length && !Registry.ITEM.get(new Identifier("minecraft:" + TREE_VARIANTS[damage] + shortId)).equals(Items.AIR))
                return "minecraft:" + TREE_VARIANTS[damage] + shortId;

            if (id.contains("wooden_")) return id.replaceFirst("wooden_", TREE_VARIANTS[damage]);
            if (id.contains("minecraft:record")) return id.replaceFirst("minecraft:record", "minecraft:music_disc");
        }
        return id;
    }
}
