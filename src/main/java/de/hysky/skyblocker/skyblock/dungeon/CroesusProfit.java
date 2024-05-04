package de.hysky.skyblocker.skyblock.dungeon;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CroesusProfit extends ContainerSolver {
    private static final Pattern ESSENCE_PATTERN = Pattern.compile("(?<type>[A-Za-z]+) Essence x(?<amount>[0-9]+)");
    public CroesusProfit() {
        super(".*Catacombs - Floor.*");
    }

    @Override
    protected boolean isEnabled() {
        return SkyblockerConfigManager.get().locations.dungeons.dungeonChestProfit.croesusProfit;
    }

    @Override
    protected List<ColorHighlight> getColors(String[] groups, Int2ObjectMap<ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        ItemStack bestChest = null, secondBestChest = null;
        long bestValue = 0, secondBestValue = 0;    // If negative value of chest - it is out of the question
        long dungeonKeyPriceData = getItemPrice("DUNGEON_CHEST_KEY") * 2; // lesser ones don't worth the hassle

        for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
            ItemStack stack = entry.getValue();
            if (stack.getName().getString().contains("Chest")) {
                long value = valueChest(stack);
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


    private long valueChest(@NotNull ItemStack chest) {
        long chestValue = 0;
        int chestPrice = 0;
        List<String> chestItems = new ArrayList<>();

        boolean processingContents = false;
        for (Text line : ItemUtils.getLore(chest)) {
            String lineString = line.getString();
            if (lineString.contains("Contents")) {
                processingContents = true;
                continue;
            } else if (lineString.isEmpty()) {
                processingContents = false;
            } else if (lineString.contains("Coins") && !processingContents) {
                chestPrice = Integer.parseInt(lineString.replaceAll(",", "").replaceAll("\\D", ""));
            }

            if (processingContents) {
                if (lineString.contains("Essence")) {
                    Matcher matcher = ESSENCE_PATTERN.matcher(lineString);
                    if (matcher.matches()) {    // add to chest value result of multiplying price of essence on it's amount
                        chestValue += getItemPrice(("ESSENCE_" + matcher.group("type")).toUpperCase()) * Integer.parseInt(matcher.group("amount"));
                    }
                } else {
                    if (lineString.contains("Spirit")) {    // TODO: make code like this to detect recombed gear (it can drop with 1% chance, according to wiki, tho I never saw any?)
                        chestValue += line.getStyle().toString().contains("color=dark_purple") ? getItemPrice("Spirit Epic") : getItemPrice(lineString);
                    } else {
                        chestItems.add(lineString);
                    }
                }
            }
        }
        for (String item : chestItems){
            chestValue += getItemPrice(item);
        }
        return chestValue-chestPrice;
    }


    private long getItemPrice(String itemDisplayName) {
        JsonObject bazaarPrices = TooltipInfoType.BAZAAR.getData();
        JsonObject lbinPrices = TooltipInfoType.LOWEST_BINS.getData();
        long itemValue = 0;
        String id = dungeonDropsNameToId.get(itemDisplayName);

        if (bazaarPrices == null || lbinPrices == null) return 0;

        if (bazaarPrices.has(id)) {
            JsonObject item = bazaarPrices.get(id).getAsJsonObject();
            boolean isPriceNull = item.get("sellPrice").isJsonNull();
            return (isPriceNull ? 0L : item.get("sellPrice").getAsLong());
        } else if (lbinPrices.has(id)) {
            return lbinPrices.get(id).getAsLong();
        }
        return itemValue;
    }


    // I did a thing :(
    private final Map<String, String> dungeonDropsNameToId = Util.make(new HashMap<>(), map -> {
    	map.put("Enchanted Book (Ultimate Jerry I)", "ENCHANTMENT_ULTIMATE_JERRY_1");    // ultimate books start
    	map.put("Enchanted Book (Ultimate Jerry II)", "ENCHANTMENT_ULTIMATE_JERRY_2");
    	map.put("Enchanted Book (Ultimate Jerry III)", "ENCHANTMENT_ULTIMATE_JERRY_3");
    	map.put("Enchanted Book (Bank I)", "ENCHANTMENT_ULTIMATE_BANK_1");
    	map.put("Enchanted Book (Bank II)", "ENCHANTMENT_ULTIMATE_BANK_2");
    	map.put("Enchanted Book (Bank III)", "ENCHANTMENT_ULTIMATE_BANK_3");
    	map.put("Enchanted Book (Combo I)", "ENCHANTMENT_ULTIMATE_COMBO_1");
    	map.put("Enchanted Book (Combo II)", "ENCHANTMENT_ULTIMATE_COMBO_2");
    	map.put("Enchanted Book (No Pain No Gain I)", "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_1");
    	map.put("Enchanted Book (No Pain No Gain II)", "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_2");
    	map.put("Enchanted Book (Ultimate Wise I)", "ENCHANTMENT_ULTIMATE_WISE_1");
    	map.put("Enchanted Book (Ultimate Wise II)", "ENCHANTMENT_ULTIMATE_WISE_2");
    	map.put("Enchanted Book (Wisdom I)", "ENCHANTMENT_ULTIMATE_WISDOM_1");
        map.put("Enchanted Book (Wisdom II)", "ENCHANTMENT_ULTIMATE_WISDOM_2");
        map.put("Enchanted Book (Last Stand I)", "ENCHANTMENT_ULTIMATE_LAST_STAND_1");
        map.put("Enchanted Book (Last Stand II)", "ENCHANTMENT_ULTIMATE_LAST_STAND_2");
        map.put("Enchanted Book (Rend I)", "ENCHANTMENT_ULTIMATE_REND_1");
        map.put("Enchanted Book (Rend II)", "ENCHANTMENT_ULTIMATE_REND_2");
        map.put("Enchanted Book (Legion I)", "ENCHANTMENT_ULTIMATE_LEGION_1");
        map.put("Enchanted Book (Swarm I)", "ENCHANTMENT_ULTIMATE_SWARM_1");
        map.put("Enchanted Book (One For All I)", "ENCHANTMENT_ULTIMATE_ONE_FOR_ALL_1");
        map.put("Enchanted Book (Soul Eater I)", "ENCHANTMENT_ULTIMATE_SOUL_EATER_1");  // ultimate books end
        map.put("Enchanted Book (Infinite Quiver VI)", "ENCHANTMENT_INFINITE_QUIVER_6");  // enchanted books start
        map.put("Enchanted Book (Infinite Quiver VII)", "ENCHANTMENT_INFINITE_QUIVER_7");
        map.put("Enchanted Book (Feather Falling VI)", "ENCHANTMENT_FEATHER_FALLING_6");
        map.put("Enchanted Book (Feather Falling VII)", "ENCHANTMENT_FEATHER_FALLING_7");
        map.put("Enchanted Book (Rejuvenate I)", "ENCHANTMENT_REJUVENATE_1");
        map.put("Enchanted Book (Rejuvenate II)", "ENCHANTMENT_REJUVENATE_2");
        map.put("Enchanted Book (Rejuvenate III)", "ENCHANTMENT_REJUVENATE_3");
        map.put("Enchanted Book (Overload)", "ENCHANTMENT_OVERLOAD_1");
        map.put("Enchanted Book (Lethality VI)", "ENCHANTMENT_LETHALITY_6");
        map.put("Enchanted Book (Thunderlord VII)", "ENCHANTMENT_THUNDERLORD_7");  // enchanted books end

        map.put("Hot Potato Book", "HOT_POTATO_BOOK");  // HPB, FPB, Recomb (universal drops)
        map.put("Fuming Potato Book", "FUMING_POTATO_BOOK");
        map.put("Recombobulator 3000", "RECOMBOBULATOR_3000");
        map.put("Necromancer's Brooch", "NECROMANCER_BROOCH");
        map.put("ESSENCE_WITHER","ESSENCE_WITHER");     // Essences. Really stupid way of doing this
        map.put("ESSENCE_UNDEAD", "ESSENCE_UNDEAD");
        map.put("ESSENCE_DRAGON", "ESSENCE_DRAGON");
        map.put("ESSENCE_SPIDER", "ESSENCE_SPIDER");
        map.put("ESSENCE_ICE", "ESSENCE_ICE");
        map.put("ESSENCE_DIAMOND", "ESSENCE_DIAMOND");
        map.put("ESSENCE_GOLD", "ESSENCE_GOLD");
        map.put("ESSENCE_CRIMSON", "ESSENCE_CRIMSON");
        map.put("DUNGEON_CHEST_KEY", "DUNGEON_CHEST_KEY");

        map.put("Bonzo's Staff", "BONZO_STAFF");    // F1 M1
        map.put("Master Skull - Tier 1", "MASTER_SKULL_TIER_1");
        map.put("Bonzo's Mask", "BONZO_MASK");
        map.put("Balloon Snake", "BALLOON_SNAKE");
        map.put("Red Nose", "RED_NOSE");

        map.put("Red Scarf", "RED_SCARF");  // F2 M2
        map.put("Adaptive Blade", "STONE_BLADE");
        map.put("Master Skull - Tier 2", "MASTER_SKULL_TIER_2");
        map.put("Adaptive Belt", "ADAPTIVE_BELT");
        map.put("Scarf's Studies", "SCARF_STUDIES");

        map.put("First Master Star", "FIRST_MASTER_STAR");  // F3 M3
        map.put("Adaptive Helmet", "ADAPTIVE_HELMET");
        map.put("Adaptive Chestplate", "ADAPTIVE_CHESTPLATE");
        map.put("Adaptive Leggings", "ADAPTIVE_LEGGINGS");
        map.put("Adaptive Boots", "ADAPTIVE_BOOTS");
        map.put("Master Skull - Tier 3", "MASTER_SKULL_TIER_3");
        map.put("Suspicious Vial", "SUSPICIOUS_VIAL");

        map.put("Spirit Sword", "SPIRIT_SWORD");    // F4 M4
        map.put("Spirit Shortbow", "ITEM_SPIRIT_BOW");
        map.put("Spirit Boots", "THORNS_BOOTS");
        map.put("Spirit", "LVL_1_LEGENDARY_SPIRIT");    // Spirit pet (Legendary)
        map.put("Spirit Epic", "LVL_1_EPIC_SPIRIT");

        map.put("Second Master Star", "SECOND_MASTER_STAR");
        map.put("Spirit Wing", "SPIRIT_WING");
        map.put("Spirit Bone", "SPIRIT_BONE");
        map.put("Spirit Stone", "SPIRIT_DECOY");

        map.put("Shadow Fury", "SHADOW_FURY");  // F5 M5
        map.put("Last Breath", "LAST_BREATH");
        map.put("Third Master Star", "THIRD_MASTER_STAR");
        map.put("Warped Stone", "AOTE_STONE");
        map.put("Livid Dagger", "LIVID_DAGGER");
        map.put("Shadow Assassin Helmet", "SHADOW_ASSASSIN_HELMET");
        map.put("Shadow Assassin Chestplate", "SHADOW_ASSASSIN_CHESTPLATE");
        map.put("Shadow Assassin Leggings", "SHADOW_ASSASSIN_LEGGINGS");
        map.put("Shadow Assassin Boots", "SHADOW_ASSASSIN_BOOTS");
        map.put("Shadow Assassin Cloak", "SHADOW_ASSASSIN_CLOAK");
        map.put("Master Skull - Tier 4", "MASTER_SKULL_TIER_4");
        map.put("Dark Orb", "DARK_ORB");

        map.put("Precursor Eye", "PRECURSOR_EYE");  // F6 M6
        map.put("Giant's Sword", "GIANTS_SWORD");
        map.put("Necromancer Lord Helmet", "NECROMANCER_LORD_HELMET");
        map.put("Necromancer Lord Chestplate", "NECROMANCER_LORD_CHESTPLATE");
        map.put("Necromancer Lord Leggings", "NECROMANCER_LORD_LEGGINGS");
        map.put("Necromancer Lord Boots", "NECROMANCER_LORD_BOOTS");
        map.put("Fourth Master Star", "FOURTH_MASTER_STAR");
        map.put("Summoning Ring", "SUMMONING_RING");
        map.put("Fel Skull", "FEL_SKULL");
        map.put("Necromancer Sword", "NECROMANCER_SWORD");
        map.put("Soulweaver Gloves", "SOULWEAVER_GLOVES");
        map.put("Sadan's Brooch", "SADAN_BROOCH");
        map.put("Giant Tooth", "GIANT_TOOTH");

        map.put("Precursor Gear", "PRECURSOR_GEAR");    // F7 M7
        map.put("Necron Dye", "DYE_NECRON");
        map.put("Storm the Fish", "STORM_THE_FISH");
        map.put("Maxor the Fish", "MAXOR_THE_FISH");
        map.put("Goldor the Fish", "GOLDOR_THE_FISH");
        map.put("Dark Claymore", "DARK_CLAYMORE");
        map.put("Necron's Handle", "NECRON_HANDLE");
        map.put("Master Skull - Tier 5", "MASTER_SKULL_TIER_5");
        map.put("Shadow Warp", "SHADOW_WARP_SCROLL");
        map.put("Wither Shield", "WITHER_SHIELD_SCROLL");
        map.put("Implosion", "IMPLOSION_SCROLL");
        map.put("Fifth Master Star", "FIFTH_MASTER_STAR");
        map.put("Auto Recombobulator", "AUTO_RECOMBOBULATOR");
        map.put("Wither Helmet", "WITHER_HELMET");
        map.put("Wither Chestplate", "WITHER_CHESTPLATE");
        map.put("Wither Leggings", "WITHER_LEGGINGS");
        map.put("Wither Boots", "WITHER_BOOTS");
        map.put("Wither Catalyst", "WITHER_CATALYST");
        map.put("Wither Cloak Sword", "WITHER_CLOAK");
        map.put("Wither Blood", "WITHER_BLOOD");

        map.put("Shiny Wither Helmet", "SHINY_WITHER_HELMET");  // M7 shiny drops
        map.put("Shiny Wither Chestplate", "SHINY_WITHER_CHESTPLATE");
        map.put("Shiny Wither Leggings", "SHINY_WITHER_LEGGINGS");
        map.put("Shiny Wither Boots", "SHINY_WITHER_BOOTS");
        map.put("Shiny Necron's Handle", "SHINY_NECRON_HANDLE");    // cool thing

        map.put("Dungeon Disc", "DUNGEON_DISC_1");
        map.put("Clown Disc", "DUNGEON_DISC_2");
        map.put("Watcher Disc", "DUNGEON_DISC_3");
        map.put("Old Disc", "DUNGEON_DISC_4");
        map.put("Necron Disc", "DUNGEON_DISC_5");
    });
}

