package de.hysky.skyblocker.skyblock.itemlist;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Map;

public class ItemFixerUpper {
    private final static String[] ANVIL_VARIANTS = {
            "minecraft:anvil",
            "minecraft:chipped_anvil",
            "minecraft:damaged_anvil"
    };

    private final static String[] COAL_VARIANTS = {
            "minecraft:coal",
            "minecraft:charcoal"
    };

    private final static String[] COBBLESTONE_WALL_VARIANTS = {
            "minecraft:cobblestone_wall",
            "minecraft:mossy_cobblestone_wall"
    };

    private final static String[] COOKED_FISH_VARIANTS = {
            "minecraft:cooked_cod",
            "minecraft:cooked_salmon"
    };

    private final static String[] DIRT_VARIANTS = {
            "minecraft:dirt",
            "minecraft:coarse_dirt",
            "minecraft:podzol"
    };

    private final static String[] DOUBLE_PLANT_VARIANTS = {
            "minecraft:sunflower",
            "minecraft:lilac",
            "minecraft:tall_grass",
            "minecraft:large_fern",
            "minecraft:rose_bush",
            "minecraft:peony"
    };

    private final static String[] DYE_VARIANTS = {
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

    private final static String[] FISH_VARIANTS = {
            "minecraft:cod",
            "minecraft:salmon",
            "minecraft:tropical_fish",
            "minecraft:pufferfish"
    };

    private final static String[] GOLDEN_APPLE_VARIANTS = {
            "minecraft:golden_apple",
            "minecraft:enchanted_golden_apple"
    };

    private final static String[] LOG_VARIANTS = {
            "minecraft:oak_log",
            "minecraft:spruce_log",
            "minecraft:birch_log",
            "minecraft:jungle_log",
            "minecraft:oak_wood",
            "minecraft:spruce_wood",
            "minecraft:birch_wood",
            "minecraft:jungle_wood",
    };

    private final static String[] LOG2_VARIANTS = {
            "minecraft:acacia_log",
            "minecraft:dark_oak_log",
            "minecraft:acacia_wood",
            "minecraft:dark_oak_wood"
    };

    private final static String[] MONSTER_EGG_VARIANTS = {
            "minecraft:infested_stone",
            "minecraft:infested_cobblestone",
            "minecraft:infested_stone_bricks",
            "minecraft:infested_mossy_stone_bricks",
            "minecraft:infested_cracked_stone_bricks",
            "minecraft:infested_chiseled_stone_bricks"
    };

    private final static String[] PRISMARINE_VARIANTS = {
            "minecraft:prismarine",
            "minecraft:prismarine_bricks",
            "minecraft:dark_prismarine"
    };

    private final static String[] QUARTZ_BLOCK_VARIANTS = {
            "minecraft:quartz_block",
            "minecraft:chiseled_quartz_block",
            "minecraft:quartz_pillar"
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

    private final static String[] SAND_VARIANTS = {
            "minecraft:sand",
            "minecraft:red_sand"
    };

    private final static String[] SKULL_VARIANTS = {
            "minecraft:skeleton_skull",
            "minecraft:wither_skeleton_skull",
            "minecraft:zombie_head",
            "minecraft:player_head",
            "minecraft:creeper_head"
    };

    private final static String[] SPONGE_VARIANTS = {
            "minecraft:sponge",
            "minecraft:wet_sponge"
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
            "minecraft:petrified_oak_slab",
            "minecraft:cobblestone_slab",
            "minecraft:brick_slab",
            "minecraft:stone_brick_slab",
            "minecraft:nether_brick_slab",
            "minecraft:quartz_slab"
    };

    private final static String[] STONEBRICK_VARIANTS = {
            "minecraft:stone_bricks",
            "minecraft:mossy_stone_bricks",
            "minecraft:cracked_stone_bricks",
            "minecraft:chiseled_stone_bricks"
    };

    private final static String[] TALLGRASS_VARIANTS = {
            "minecraft:dead_bush",
            "minecraft:short_grass",
            "minecraft:fern"
    };

    private final static Int2ObjectMap<String> SPAWN_EGG_VARIANTS = Int2ObjectMaps.unmodifiable(new Int2ObjectOpenHashMap<>(Map.ofEntries(
            //This entry 0 is technically not right but Hypixel decided to make it polar bear so well we use that
            Map.entry(0, "minecraft:polar_bear_spawn_egg"),
            //This entry 4 does not actually exist, Hypixel uses it as a placeholder for elder guardians
            Map.entry(4, "minecraft:elder_guardian_spawn_egg"),
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
    )));

    private final static String[] SANDSTONE_VARIANTS = {
            ":",
            ":chiseled_",
            ":cut_"
    };

    private final static String[] COLOR_VARIANTS = {
            ":white_",
            ":orange_",
            ":magenta_",
            ":light_blue_",
            ":yellow_",
            ":lime_",
            ":pink_",
            ":gray_",
            ":light_gray_",
            ":cyan_",
            ":purple_",
            ":blue_",
            ":brown_",
            ":green_",
            ":red_",
            ":black_"
    };

    private final static String[] WOOD_VARIANTS = {
            ":oak_",
            ":spruce_",
            ":birch_",
            ":jungle_",
            ":acacia_",
            ":dark_oak_"
    };

    //this is the map of all renames
    private final static Map<String, String> RENAMED = Map.ofEntries(
            Map.entry("minecraft:bed", "minecraft:red_bed"),
            Map.entry("minecraft:boat", "minecraft:oak_boat"),
            Map.entry("minecraft:brick_block", "minecraft:bricks"),
            Map.entry("minecraft:deadbush", "minecraft:dead_bush"),
            Map.entry("minecraft:fence_gate", "minecraft:oak_fence_gate"),
            Map.entry("minecraft:fence", "minecraft:oak_fence"),
            Map.entry("minecraft:firework_charge", "minecraft:firework_star"),
            Map.entry("minecraft:fireworks", "minecraft:firework_rocket"),
            Map.entry("minecraft:golden_rail", "minecraft:powered_rail"),
            Map.entry("minecraft:grass", "minecraft:grass_block"),
            Map.entry("minecraft:hardened_clay", "minecraft:terracotta"),
            Map.entry("minecraft:lit_pumpkin", "minecraft:jack_o_lantern"),
            Map.entry("minecraft:melon_block", "minecraft:melon"),
            Map.entry("minecraft:melon", "minecraft:melon_slice"),
            Map.entry("minecraft:mob_spawner", "minecraft:spawner"),
            Map.entry("minecraft:nether_brick", "minecraft:nether_bricks"),
            Map.entry("minecraft:netherbrick", "minecraft:nether_brick"),
            Map.entry("minecraft:noteblock", "minecraft:note_block"),
            Map.entry("minecraft:piston_extension", "minecraft:moving_piston"),
            Map.entry("minecraft:portal", "minecraft:nether_portal"),
            Map.entry("minecraft:pumpkin", "minecraft:carved_pumpkin"),
            Map.entry("minecraft:quartz_ore", "minecraft:nether_quartz_ore"),
            Map.entry("minecraft:record_11", "minecraft:music_disc_11"),
            Map.entry("minecraft:record_13", "minecraft:music_disc_13"),
            Map.entry("minecraft:record_blocks", "minecraft:music_disc_blocks"),
            Map.entry("minecraft:record_cat", "minecraft:music_disc_cat"),
            Map.entry("minecraft:record_chirp", "minecraft:music_disc_chirp"),
            Map.entry("minecraft:record_far", "minecraft:music_disc_far"),
            Map.entry("minecraft:record_mall", "minecraft:music_disc_mall"),
            Map.entry("minecraft:record_mellohi", "minecraft:music_disc_mellohi"),
            Map.entry("minecraft:record_stal", "minecraft:music_disc_stal"),
            Map.entry("minecraft:record_strad", "minecraft:music_disc_strad"),
            Map.entry("minecraft:record_wait", "minecraft:music_disc_wait"),
            Map.entry("minecraft:record_ward", "minecraft:music_disc_ward"),
            Map.entry("minecraft:red_nether_brick", "minecraft:red_nether_bricks"),
            Map.entry("minecraft:reeds", "minecraft:sugar_cane"),
            Map.entry("minecraft:sign", "minecraft:oak_sign"),
            Map.entry("minecraft:slime", "minecraft:slime_block"),
            Map.entry("minecraft:snow_layer", "minecraft:snow"),
            Map.entry("minecraft:snow", "minecraft:snow_block"),
            Map.entry("minecraft:speckled_melon", "minecraft:glistering_melon_slice"),
            Map.entry("minecraft:stone_slab2", "minecraft:red_sandstone_slab"),
            Map.entry("minecraft:stone_stairs", "minecraft:cobblestone_stairs"),
            Map.entry("minecraft:trapdoor", "minecraft:oak_trapdoor"),
            Map.entry("minecraft:waterlily", "minecraft:lily_pad"),
            Map.entry("minecraft:web", "minecraft:cobweb"),
            Map.entry("minecraft:wooden_button", "minecraft:oak_button"),
            Map.entry("minecraft:wooden_door", "minecraft:oak_door"),
            Map.entry("minecraft:wooden_pressure_plate", "minecraft:oak_pressure_plate"),
            Map.entry("minecraft:yellow_flower", "minecraft:dandelion")
    );

    //TODO : Add mushroom block variants
    //i'll do it later because it isn't used and unlike the other, it's not just a rename or a separate, it's a separate and a merge

    public static String convertItemId(String id, int damage) {
        return switch (id) {
            //all the case are simple separate
            case "minecraft:anvil" -> ANVIL_VARIANTS[damage];
            case "minecraft:coal" -> COAL_VARIANTS[damage];
            case "minecraft:cobblestone_wall" -> COBBLESTONE_WALL_VARIANTS[damage];
            case "minecraft:cooked_fish" -> COOKED_FISH_VARIANTS[damage];
            case "minecraft:dirt" -> DIRT_VARIANTS[damage];
            case "minecraft:double_plant" -> DOUBLE_PLANT_VARIANTS[damage];
            case "minecraft:dye" -> DYE_VARIANTS[damage];
            case "minecraft:fish" -> FISH_VARIANTS[damage];
            case "minecraft:golden_apple" -> GOLDEN_APPLE_VARIANTS[damage];
            case "minecraft:log" -> LOG_VARIANTS[damage];
            case "minecraft:log2" -> LOG2_VARIANTS[damage];
            case "minecraft:monster_egg" -> MONSTER_EGG_VARIANTS[damage];
            case "minecraft:prismarine" -> PRISMARINE_VARIANTS[damage];
            case "minecraft:quartz_block" -> QUARTZ_BLOCK_VARIANTS[damage];
            case "minecraft:red_flower" -> RED_FLOWER_VARIANTS[damage];
            case "minecraft:sand" -> SAND_VARIANTS[damage];
            case "minecraft:skull" -> SKULL_VARIANTS[damage];
            case "minecraft:sponge" -> SPONGE_VARIANTS[damage];
            case "minecraft:stone" -> STONE_VARIANTS[damage];
            case "minecraft:stone_slab" -> STONE_SLAB_VARIANTS[damage];
            case "minecraft:stonebrick" -> STONEBRICK_VARIANTS[damage];
            case "minecraft:tallgrass" -> TALLGRASS_VARIANTS[damage];
            //we use a Map from int to str instead of an array because numbers are not consecutive
            case "minecraft:spawn_egg" -> SPAWN_EGG_VARIANTS.get(damage);
            //when we use the generalized variant we need to replaceFirst
            case "minecraft:sandstone", "minecraft:red_sandstone" -> id.replaceFirst(":", SANDSTONE_VARIANTS[damage]);
            //to use the general color variants we need to reverse the order because Minecraft decided so for some reason
            case "minecraft:banner" -> id.replaceFirst(":", COLOR_VARIANTS[15 - damage]);
            case "minecraft:carpet", "minecraft:stained_glass", "minecraft:stained_glass_pane", "minecraft:wool" -> id.replaceFirst(":", COLOR_VARIANTS[damage]);
            //for the terracotta we replace the whole name by the color and append "terracotta" at the end
            case "minecraft:stained_hardened_clay" -> id.replaceFirst(":stained_hardened_clay", COLOR_VARIANTS[damage]) + "terracotta";
            //for the wooden slab we need to remove the "wooden_" prefix, but otherwise it's the same, so I just combined them anyway
            case "minecraft:leaves", "minecraft:planks", "minecraft:sapling", "minecraft:wooden_slab" -> id.replaceFirst(":(?:wooden_)?", WOOD_VARIANTS[damage]);
            //here we replace the 2 by nothing to remove it as it's not needed anymore
            case "minecraft:leaves2" -> id.replaceFirst(":", WOOD_VARIANTS[damage + 4]).replaceFirst("2", "");
            //the default case is just a rename or no change
            default -> RENAMED.getOrDefault(id, id);
        };
    }
}
