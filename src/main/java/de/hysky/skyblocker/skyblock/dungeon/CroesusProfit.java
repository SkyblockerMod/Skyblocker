package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CroesusProfit extends SimpleContainerSolver implements TooltipAdder {
	public static final CroesusProfit INSTANCE = new CroesusProfit();
	private static final Pattern ESSENCE_PATTERN = Pattern.compile("(?<type>[A-Za-z]+) Essence x(?<amount>\\d+)");
	private static final Pattern CHEST_PATTERN = Pattern.compile("^(?:Wood|Gold|Diamond|Emerald|Obsidian|Bedrock)$");

	public CroesusProfit() {
		super("(Master )?Catacombs - Flo.*");
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().dungeons.dungeonChestProfit.croesusProfit;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		List<ColorHighlight> highlights = new ArrayList<>();
		ItemStack bestChest = null, secondBestChest = null;
		double bestValue = 0, secondBestValue = 0;    // If negative value of chest - it is out of the question
		double dungeonKeyPriceData = getItemPrice("DUNGEON_CHEST_KEY") * 2; // lesser ones are not worth the hassle

		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			ItemStack stack = entry.getValue();
			if (CHEST_PATTERN.matcher(stack.getName().getString()).matches()) {
				double value = getChestValue(stack);
				if (value <= 0) continue;

				if (value > bestValue) {
					secondBestChest = bestChest;
					secondBestValue = bestValue;
					bestChest = stack;
					bestValue = value;
				} else if (value > secondBestValue) {
					secondBestChest = stack;
					secondBestValue = value;
				}
			}
		}

		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			ItemStack stack = entry.getValue();
			if (stack != null) {
				if (stack.equals(bestChest)) {
					highlights.add(ColorHighlight.green(entry.getIntKey()));
				} else if (stack.equals(secondBestChest) && secondBestValue > dungeonKeyPriceData) {
					highlights.add(ColorHighlight.yellow(entry.getIntKey()));
				}
			}
		}
		return highlights;
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		if (focusedSlot == null || !focusedSlot.hasStack()) return;
		if (!focusedSlot.getStack().isOf(Items.PLAYER_HEAD)) return;

		double value = getChestValue(focusedSlot.getStack());
		lines.add(Constants.PREFIX.get().append(
				Text.translatable("skyblocker.dungeons.croesusHelper.chestValue", Formatters.INTEGER_NUMBERS.format(value))
		));
	}

	@Override
	public int getPriority() {
		return 16;
	}

	private double getChestValue(@NotNull ItemStack chest) {
		double chestValue = 0;
		int chestPrice = 0;

		boolean processingContents = false;
		for (Text line : ItemUtils.getLore(chest)) {
			String lineString = line.getString();
			if (lineString.contains("Contents")) {
				processingContents = true;
				continue;
			} else if (lineString.isEmpty()) {
				processingContents = false;
			} else if (!processingContents && lineString.endsWith("Coins")) {
				String chestCost = lineString.replace(",", "").replaceAll("\\D", "");
				if (!NumberUtils.isCreatable(chestCost)) continue;
				chestPrice = Integer.parseInt(chestCost);
			}

			if (processingContents) {
				if (lineString.contains("Essence") && SkyblockerConfigManager.get().dungeons.dungeonChestProfit.includeEssence) {
					Matcher matcher = ESSENCE_PATTERN.matcher(lineString);
					if (matcher.matches()) {    // add to chest value result of multiplying price of essence on it's amount
						chestValue += getItemPrice(("ESSENCE_" + matcher.group("type")).toUpperCase(Locale.ENGLISH)) * Integer.parseInt(matcher.group("amount"));
					}
				} else if (lineString.equals("Spirit") && line.getStyle().getColor() == TextColor.fromFormatting(Formatting.DARK_PURPLE)) {    // TODO: make code like this to detect recombed gear (it can drop with 1% chance, according to wiki, tho I never saw any?)
					chestValue += getItemPrice("Spirit Epic");
				} else {
					chestValue += getItemPrice(lineString);
				}
			}
		}

		return chestValue - chestPrice;
	}


	/**
	 * @param itemName The item's display name or API Id
	 *                 The API id is used for Essences and the Dungeon Chest Key.
	 */
	private double getItemPrice(String itemName) {
		return ItemUtils.getItemPrice(DUNGEON_DROPS_NAME_TO_API_ID.getOrDefault(itemName, itemName)).leftDouble();
	}

	// I did a thing :(
	private static final Map<String, String> DUNGEON_DROPS_NAME_TO_API_ID = Map.ofEntries(
		Map.entry("Enchanted Book (Ultimate Jerry I)", "ENCHANTMENT_ULTIMATE_JERRY_1"),    // ultimate books start
		Map.entry("Enchanted Book (Ultimate Jerry II)", "ENCHANTMENT_ULTIMATE_JERRY_2"),
		Map.entry("Enchanted Book (Ultimate Jerry III)", "ENCHANTMENT_ULTIMATE_JERRY_3"),
		Map.entry("Enchanted Book (Bank I)", "ENCHANTMENT_ULTIMATE_BANK_1"),
		Map.entry("Enchanted Book (Bank II)", "ENCHANTMENT_ULTIMATE_BANK_2"),
		Map.entry("Enchanted Book (Bank III)", "ENCHANTMENT_ULTIMATE_BANK_3"),
		Map.entry("Enchanted Book (Combo I)", "ENCHANTMENT_ULTIMATE_COMBO_1"),
		Map.entry("Enchanted Book (Combo II)", "ENCHANTMENT_ULTIMATE_COMBO_2"),
		Map.entry("Enchanted Book (No Pain No Gain I)", "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_1"),
		Map.entry("Enchanted Book (No Pain No Gain II)", "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_2"),
		Map.entry("Enchanted Book (Ultimate Wise I)", "ENCHANTMENT_ULTIMATE_WISE_1"),
		Map.entry("Enchanted Book (Ultimate Wise II)", "ENCHANTMENT_ULTIMATE_WISE_2"),
		Map.entry("Enchanted Book (Wisdom I)", "ENCHANTMENT_ULTIMATE_WISDOM_1"),
		Map.entry("Enchanted Book (Wisdom II)", "ENCHANTMENT_ULTIMATE_WISDOM_2"),
		Map.entry("Enchanted Book (Last Stand I)", "ENCHANTMENT_ULTIMATE_LAST_STAND_1"),
		Map.entry("Enchanted Book (Last Stand II)", "ENCHANTMENT_ULTIMATE_LAST_STAND_2"),
		Map.entry("Enchanted Book (Rend I)", "ENCHANTMENT_ULTIMATE_REND_1"),
		Map.entry("Enchanted Book (Rend II)", "ENCHANTMENT_ULTIMATE_REND_2"),
		Map.entry("Enchanted Book (Legion I)", "ENCHANTMENT_ULTIMATE_LEGION_1"),
		Map.entry("Enchanted Book (Swarm I)", "ENCHANTMENT_ULTIMATE_SWARM_1"),
		Map.entry("Enchanted Book (One For All I)", "ENCHANTMENT_ULTIMATE_ONE_FOR_ALL_1"),
		Map.entry("Enchanted Book (Soul Eater I)", "ENCHANTMENT_ULTIMATE_SOUL_EATER_1"),  // ultimate books end
		Map.entry("Enchanted Book (Infinite Quiver VI)", "ENCHANTMENT_INFINITE_QUIVER_6"),  // enchanted books start
		Map.entry("Enchanted Book (Infinite Quiver VII)", "ENCHANTMENT_INFINITE_QUIVER_7"),
		Map.entry("Enchanted Book (Feather Falling VI)", "ENCHANTMENT_FEATHER_FALLING_6"),
		Map.entry("Enchanted Book (Feather Falling VII)", "ENCHANTMENT_FEATHER_FALLING_7"),
		Map.entry("Enchanted Book (Rejuvenate I)", "ENCHANTMENT_REJUVENATE_1"),
		Map.entry("Enchanted Book (Rejuvenate II)", "ENCHANTMENT_REJUVENATE_2"),
		Map.entry("Enchanted Book (Rejuvenate III)", "ENCHANTMENT_REJUVENATE_3"),
		Map.entry("Enchanted Book (Overload I)", "ENCHANTMENT_OVERLOAD_1"),
		Map.entry("Enchanted Book (Lethality VI)", "ENCHANTMENT_LETHALITY_6"),
		Map.entry("Enchanted Book (Thunderlord VII)", "ENCHANTMENT_THUNDERLORD_7"),  // enchanted books end

		Map.entry("Hot Potato Book", "HOT_POTATO_BOOK"),  // HPB, FPB, Recomb (universal drops)
		Map.entry("Fuming Potato Book", "FUMING_POTATO_BOOK"),
		Map.entry("Recombobulator 3000", "RECOMBOBULATOR_3000"),

		Map.entry("Necromancer's Brooch", "NECROMANCER_BROOCH"), // F1 to F4

		Map.entry("Bonzo's Staff", "BONZO_STAFF"),    // F1 M1
		Map.entry("Master Skull - Tier 1", "MASTER_SKULL_TIER_1"),
		Map.entry("Bonzo's Mask", "BONZO_MASK"),
		Map.entry("Balloon Snake", "BALLOON_SNAKE"),
		Map.entry("Red Nose", "RED_NOSE"),

		Map.entry("Red Scarf", "RED_SCARF"),  // F2 M2
		Map.entry("Adaptive Blade", "STONE_BLADE"),
		Map.entry("Master Skull - Tier 2", "MASTER_SKULL_TIER_2"),
		Map.entry("Adaptive Belt", "ADAPTIVE_BELT"),
		Map.entry("Scarf's Studies", "SCARF_STUDIES"),

		Map.entry("First Master Star", "FIRST_MASTER_STAR"),  // F3 M3
		Map.entry("Adaptive Helmet", "ADAPTIVE_HELMET"),
		Map.entry("Adaptive Chestplate", "ADAPTIVE_CHESTPLATE"),
		Map.entry("Adaptive Leggings", "ADAPTIVE_LEGGINGS"),
		Map.entry("Adaptive Boots", "ADAPTIVE_BOOTS"),
		Map.entry("Master Skull - Tier 3", "MASTER_SKULL_TIER_3"),
		Map.entry("Suspicious Vial", "SUSPICIOUS_VIAL"),

		Map.entry("Spirit Sword", "SPIRIT_SWORD"),    // F4 M4
		Map.entry("Spirit Shortbow", "ITEM_SPIRIT_BOW"),
		Map.entry("Spirit Boots", "THORNS_BOOTS"),
		Map.entry("Spirit", "LVL_1_LEGENDARY_SPIRIT"),    // Spirit pet (Legendary)
		Map.entry("Spirit Epic", "LVL_1_EPIC_SPIRIT"),

		Map.entry("Second Master Star", "SECOND_MASTER_STAR"),
		Map.entry("Spirit Wing", "SPIRIT_WING"),
		Map.entry("Spirit Bone", "SPIRIT_BONE"),
		Map.entry("Spirit Stone", "SPIRIT_DECOY"),

		Map.entry("Shadow Fury", "SHADOW_FURY"),  // F5 M5
		Map.entry("Last Breath", "LAST_BREATH"),
		Map.entry("Third Master Star", "THIRD_MASTER_STAR"),
		Map.entry("Warped Stone", "AOTE_STONE"),
		Map.entry("Livid Dagger", "LIVID_DAGGER"),
		Map.entry("Shadow Assassin Helmet", "SHADOW_ASSASSIN_HELMET"),
		Map.entry("Shadow Assassin Chestplate", "SHADOW_ASSASSIN_CHESTPLATE"),
		Map.entry("Shadow Assassin Leggings", "SHADOW_ASSASSIN_LEGGINGS"),
		Map.entry("Shadow Assassin Boots", "SHADOW_ASSASSIN_BOOTS"),
		Map.entry("Shadow Assassin Cloak", "SHADOW_ASSASSIN_CLOAK"),
		Map.entry("Master Skull - Tier 4", "MASTER_SKULL_TIER_4"),
		Map.entry("Dark Orb", "DARK_ORB"),

		Map.entry("Precursor Eye", "PRECURSOR_EYE"),  // F6 M6
		Map.entry("Giant's Sword", "GIANTS_SWORD"),
		Map.entry("Necromancer Lord Helmet", "NECROMANCER_LORD_HELMET"),
		Map.entry("Necromancer Lord Chestplate", "NECROMANCER_LORD_CHESTPLATE"),
		Map.entry("Necromancer Lord Leggings", "NECROMANCER_LORD_LEGGINGS"),
		Map.entry("Necromancer Lord Boots", "NECROMANCER_LORD_BOOTS"),
		Map.entry("Fourth Master Star", "FOURTH_MASTER_STAR"),
		Map.entry("Summoning Ring", "SUMMONING_RING"),
		Map.entry("Fel Skull", "FEL_SKULL"),
		Map.entry("Necromancer Sword", "NECROMANCER_SWORD"),
		Map.entry("Soulweaver Gloves", "SOULWEAVER_GLOVES"),
		Map.entry("Sadan's Brooch", "SADAN_BROOCH"),
		Map.entry("Giant Tooth", "GIANT_TOOTH"),

		Map.entry("Precursor Gear", "PRECURSOR_GEAR"),    // F7 M7
		Map.entry("Necron Dye", "DYE_NECRON"),
		Map.entry("Storm the Fish", "STORM_THE_FISH"),
		Map.entry("Maxor the Fish", "MAXOR_THE_FISH"),
		Map.entry("Goldor the Fish", "GOLDOR_THE_FISH"),
		Map.entry("Dark Claymore", "DARK_CLAYMORE"),
		Map.entry("Necron's Handle", "NECRON_HANDLE"),
		Map.entry("Master Skull - Tier 5", "MASTER_SKULL_TIER_5"),
		Map.entry("Shadow Warp", "SHADOW_WARP_SCROLL"),
		Map.entry("Wither Shield", "WITHER_SHIELD_SCROLL"),
		Map.entry("Implosion", "IMPLOSION_SCROLL"),
		Map.entry("Fifth Master Star", "FIFTH_MASTER_STAR"),
		Map.entry("Auto Recombobulator", "AUTO_RECOMBOBULATOR"),
		Map.entry("Wither Helmet", "WITHER_HELMET"),
		Map.entry("Wither Chestplate", "WITHER_CHESTPLATE"),
		Map.entry("Wither Leggings", "WITHER_LEGGINGS"),
		Map.entry("Wither Boots", "WITHER_BOOTS"),
		Map.entry("Wither Catalyst", "WITHER_CATALYST"),
		Map.entry("Wither Cloak Sword", "WITHER_CLOAK"),
		Map.entry("Wither Blood", "WITHER_BLOOD"),

		Map.entry("Shiny Wither Helmet", "SHINY_WITHER_HELMET"),  // M7 shiny drops
		Map.entry("Shiny Wither Chestplate", "SHINY_WITHER_CHESTPLATE"),
		Map.entry("Shiny Wither Leggings", "SHINY_WITHER_LEGGINGS"),
		Map.entry("Shiny Wither Boots", "SHINY_WITHER_BOOTS"),
		Map.entry("Shiny Necron's Handle", "SHINY_NECRON_HANDLE"),    // cool thing

		Map.entry("Dungeon Disc", "DUNGEON_DISC_1"),
		Map.entry("Clown Disc", "DUNGEON_DISC_2"),
		Map.entry("Watcher Disc", "DUNGEON_DISC_3"),
		Map.entry("Old Disc", "DUNGEON_DISC_4"),
		Map.entry("Necron Disc", "DUNGEON_DISC_5"),

		Map.entry("Scarf Shard", "SHARD_SCARF"),
		Map.entry("Thorn Shard", "SHARD_THORN"),
		Map.entry("Wither Shard", "SHARD_WITHER"),
		Map.entry("Apex Dragon Shard", "SHARD_APEX_DRAGON"),
		Map.entry("Power Dragon Shard", "SHARD_POWER_DRAGON")
	);
}
