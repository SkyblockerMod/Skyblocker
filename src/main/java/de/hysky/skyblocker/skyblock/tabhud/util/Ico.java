package de.hysky.skyblocker.skyblock.tabhud.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;

/**
 * Stores convenient shorthands for common ItemStack definitions
 */
public class Ico {
    private Ico(){}
    public static final ItemStack MAP = new ItemStack(Items.FILLED_MAP);
    public static final ItemStack NTAG = new ItemStack(Items.NAME_TAG);
    public static final ItemStack EMERALD = new ItemStack(Items.EMERALD);
    public static final ItemStack AMETHYST_SHARD = new ItemStack(Items.AMETHYST_SHARD);
    public static final ItemStack CLOCK = new ItemStack(Items.CLOCK);
    public static final ItemStack DIASWORD = new ItemStack(Items.DIAMOND_SWORD);
    public static final ItemStack DBUSH = new ItemStack(Items.DEAD_BUSH);
    public static final ItemStack VILLAGER = new ItemStack(Items.VILLAGER_SPAWN_EGG);
    public static final ItemStack MOREGOLD = new ItemStack(Items.GOLDEN_APPLE);
    public static final ItemStack COMPASS = new ItemStack(Items.COMPASS);
    public static final ItemStack SUGAR = new ItemStack(Items.SUGAR);
    public static final ItemStack HOE = new ItemStack(Items.IRON_HOE);
    public static final ItemStack GOLD = new ItemStack(Items.GOLD_INGOT);
    public static final ItemStack BONE = new ItemStack(Items.BONE);
    public static final ItemStack SIGN = new ItemStack(Items.OAK_SIGN);
    public static final ItemStack FISH_ROD = new ItemStack(Items.FISHING_ROD);
    public static final ItemStack SWORD = new ItemStack(Items.IRON_SWORD);
    public static final ItemStack LANTERN = new ItemStack(Items.LANTERN);
    public static final ItemStack COOKIE = new ItemStack(Items.COOKIE);
    public static final ItemStack POTION = new ItemStack(Items.POTION);
    public static final ItemStack BARRIER = new ItemStack(Items.BARRIER);
    public static final ItemStack PLAYER = new ItemStack(Items.PLAYER_HEAD);
    public static final ItemStack WATER = new ItemStack(Items.WATER_BUCKET);
    public static final ItemStack LEATHER = new ItemStack(Items.LEATHER);
    public static final ItemStack MITHRIL = new ItemStack(Items.PRISMARINE_CRYSTALS);
    public static final ItemStack REDSTONE = new ItemStack(Items.REDSTONE);
    public static final ItemStack FIRE = new ItemStack(Items.CAMPFIRE);
    public static final ItemStack STRING = new ItemStack(Items.STRING);
    public static final ItemStack WITHER = new ItemStack(Items.WITHER_SKELETON_SKULL);
    public static final ItemStack FLESH = new ItemStack(Items.ROTTEN_FLESH);
    public static final ItemStack DRAGON = new ItemStack(Items.DRAGON_HEAD);
    public static final ItemStack DIAMOND = new ItemStack(Items.DIAMOND);
    public static final ItemStack ICE = new ItemStack(Items.ICE);
    public static final ItemStack CHEST = new ItemStack(Items.CHEST);
    public static final ItemStack COMMAND = new ItemStack(Items.COMMAND_BLOCK);
    public static final ItemStack SKULL = new ItemStack(Items.SKELETON_SKULL);
    public static final ItemStack BOOK = new ItemStack(Items.WRITABLE_BOOK);
    public static final ItemStack FURNACE = new ItemStack(Items.FURNACE);
    public static final ItemStack CHESTPLATE = new ItemStack(Items.IRON_CHESTPLATE);
    public static final ItemStack B_ROD = new ItemStack(Items.BLAZE_ROD);
    public static final ItemStack BOW = new ItemStack(Items.BOW);
    public static final ItemStack COPPER = new ItemStack(Items.COPPER_INGOT);
    public static final ItemStack NETHERITE_UPGRADE_ST = new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
    public static final ItemStack COMPOSTER = new ItemStack(Items.COMPOSTER);
    public static final ItemStack SAPLING = new ItemStack(Items.OAK_SAPLING);
    public static final ItemStack SEEDS = new ItemStack(Items.WHEAT_SEEDS);
    public static final ItemStack MILESTONE = new ItemStack(Items.LODESTONE);
    public static final ItemStack PICKAXE = new ItemStack(Items.IRON_PICKAXE);
    public static final ItemStack NETHER_STAR = new ItemStack(Items.NETHER_STAR);
    public static final ItemStack HEART_OF_THE_SEA = new ItemStack(Items.HEART_OF_THE_SEA);
    public static final ItemStack EXPERIENCE_BOTTLE = new ItemStack(Items.EXPERIENCE_BOTTLE);
    public static final ItemStack PINK_DYE = new ItemStack(Items.PINK_DYE);
    public static final ItemStack ENCHANTED_BOOK = new ItemStack(Items.ENCHANTED_BOOK);
    public static final ItemStack DUNG = new ItemStack(Items.BROWN_WOOL);
    public static final ItemStack PLANT_MATTER = new ItemStack(Items.JUNGLE_LEAVES);
    public static final ItemStack HONEY_JAR;
    public static final ItemStack TASTY_CHEESE;
    public static final ItemStack COMPOST;

	static {
		try {
			HONEY_JAR = ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:player_head\",Count:1,tag:{SkullOwner:{Id:[I;-652963588,-1250610904,-1528623936,-101592516],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE1MzlkYmNkMzZmODc3MjYzMmU1NzM5ZTJlNTE0ODRlZGYzNzNjNTU4ZDZmYjJjNmI2MWI3MmI3Y2FhIn19fQ\"}]}}}}"));
            TASTY_CHEESE = ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:player_head\",Count:1,tag:{SkullOwner:{Id:[I;-652963588,-1250610904,-1528623936,-101592516],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE1MzlkYmNkMzZmODc3MjYzMmU1NzM5ZTJlNTE0ODRlZGYzNzNjNTU4ZDZmYjJjNmI2MWI3MmI3Y2FhIn19fQ\"}]}}}}"));
            COMPOST = ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:player_head\",Count:1,tag:{SkullOwner:{Id:[I;-1629895565,1464677902,-1385612327,-737550603],Properties:{textures:[{Value:\"ewogICJ0aW1lc3RhbXAiIDogMTY2MjUwMjg5OTMyNiwKICAicHJvZmlsZUlkIiA6ICI5MWYwNGZlOTBmMzY0M2I1OGYyMGUzMzc1Zjg2ZDM5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9ybVN0b3JteSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iYTM5ZGYzNmE2NjY1ZTlkYzMzZjM0MTM3MTdkYWVmYWZkMWY3OGI5N2VlZjI0ZjNjYWU5ZTNiYmUzYzc3YjliIgogICAgfQogIH0KfQ\"}]}}}}"));
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
