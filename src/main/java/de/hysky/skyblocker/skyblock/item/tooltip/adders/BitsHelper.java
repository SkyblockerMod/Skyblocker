package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitsHelper extends SimpleContainerSolver implements TooltipAdder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern BITS_PATTERN = Pattern.compile("Cost (?<amount>[\\d,]+) Bits");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("Click to browse!");
    @Language("RegExp") private static final String TITLE_PATTERN = ".*(?:Community Shop|Bits Shop).*";
    private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getInstance(Locale.US);
    private static final String LOGS_PREFIX  = "[Skyblocker Bits Helper] ";

    public static final BitsHelper INSTANCE = new BitsHelper();

    private BitsHelper() {
        super(TITLE_PATTERN);
    }

    private Map<String, ObjectLongImmutablePair <String>> categoryOutput = new HashMap<>();
    private int bestSlotIndexSelling = -1;
    private int bestSlotIndexAll = -1;

    @Override
    public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        BestItemsResult bestItemsResult = calculateBestItems(slots);

        bestSlotIndexSelling = bestItemsResult.bestSlotIndexSelling;
        bestSlotIndexAll = bestItemsResult.bestSlotIndexAll;

        // If best selling = best overall, only green will appear. This is intended
        if (bestSlotIndexSelling != -1 && bestSlotIndexSelling == bestSlotIndexAll) {
            highlights.add(ColorHighlight.green(bestSlotIndexSelling));
        } else {
            if (bestSlotIndexSelling != -1) {
                highlights.add(ColorHighlight.green(bestSlotIndexSelling));
            }
            if (bestSlotIndexAll != -1) {
                highlights.add(ColorHighlight.yellow(bestSlotIndexAll));
            }
        }
        return highlights;
    }

    /**
     * Why there is no simpler way to get those?
     */
    private boolean megamindInLogs = false;
    private Int2ObjectMap<ItemStack> getSlots() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof HandledScreen) {
            HandledScreen<?> screen = (HandledScreen<?>) client.currentScreen;
            ScreenHandler handler = screen.getScreenHandler();

            Int2ObjectMap<ItemStack> slots = new Int2ObjectOpenHashMap<>();
            for (int i = 0; i < handler.slots.size(); i++) {
                slots.put(i, handler.getSlot(i).getStack());
            }
            if (!megamindInLogs) {
                LOGGER.info(LOGS_PREFIX + """
                                No slots?
                    ⠀⣞⢽⢪⢣⢣⢣⢫⡺⡵⣝⡮⣗⢷⢽⢽⢽⣮⡷⡽⣜⣜⢮⢺⣜⢷⢽⢝⡽⣝
                    ⠸⡸⠜⠕⠕⠁⢁⢇⢏⢽⢺⣪⡳⡝⣎⣏⢯⢞⡿⣟⣷⣳⢯⡷⣽⢽⢯⣳⣫⠇
                    ⠀⠀⢀⢀⢄⢬⢪⡪⡎⣆⡈⠚⠜⠕⠇⠗⠝⢕⢯⢫⣞⣯⣿⣻⡽⣏⢗⣗⠏⠀
                    ⠀⠪⡪⡪⣪⢪⢺⢸⢢⢓⢆⢤⢀⠀⠀⠀⠀⠈⢊⢞⡾⣿⡯⣏⢮⠷⠁⠀⠀
                    ⠀⠀⠀⠈⠊⠆⡃⠕⢕⢇⢇⢇⢇⢇⢏⢎⢎⢆⢄⠀⢑⣽⣿⢝⠲⠉⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠀⡿⠂⠠⠀⡇⢇⠕⢈⣀⠀⠁⠡⠣⡣⡫⣂⣿⠯⢪⠰⠂⠀⠀⠀⠀
                    ⠀⠀⠀⠀⡦⡙⡂⢀⢤⢣⠣⡈⣾⡃⠠⠄⠀⡄⢱⣌⣶⢏⢊⠂⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⢝⡲⣜⡮⡏⢎⢌⢂⠙⠢⠐⢀⢘⢵⣽⣿⡿⠁⠁⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠨⣺⡺⡕⡕⡱⡑⡆⡕⡅⡕⡜⡼⢽⡻⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⣼⣳⣫⣾⣵⣗⡵⡱⡡⢣⢑⢕⢜⢕⡝⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⣴⣿⣾⣿⣿⣿⡿⡽⡑⢌⠪⡢⡣⣣⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⡟⡾⣿⢿⢿⢵⣽⣾⣼⣘⢸⢸⣞⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠁⠇⠡⠩⡫⢿⣝⡻⡮⣒⢽⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀""");
                megamindInLogs = true;
            }
            return slots;
        }
        return null;
    }

    @Override
    public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
        if (focusedSlot == null) return;

        String lore = ItemUtils.concatenateLore(lines);
        Matcher bitsMatcher = BITS_PATTERN.matcher(lore);
        long coinsPerBit = 0L;
        String itemID = "";

        boolean isGreen = false;
        if (!CATEGORY_PATTERN.matcher(lore).find()) {
            if (!bitsMatcher.find()) return;

            long bitsCost = Long.parseLong(bitsMatcher.group("amount").replace(",", ""));
            //double itemCost = getPrice(stack) * stack.getCount();
            double itemCost = ItemUtils.getItemPrice(stack).keyDouble() * stack.getCount();

            if (itemCost == 0) return;

            coinsPerBit = Math.round(itemCost / bitsCost);
        } else {
            String outerKey = stack.getName().getString();
            ObjectLongPair<String> innerMap, innerMapGreen;
            if (bestSlotIndexSelling != -1) if (bestSlotIndexSelling == focusedSlot.getIndex()) isGreen = true;

            BestItemsResult result = calculateBestItems(getSlots());
            innerMap = categoryOutput.get(outerKey);
            innerMapGreen = categoryOutput.get(outerKey + "GREEN");

            if (innerMap != null) {
//                LOGGER.info(LOGS_PREFIX + "innerMap STATE: {}, {}", innerMap.left(), innerMap.rightLong());
                if (isGreen) {  // this is here so green highlighted category won't show yellow stuff
                    itemID = innerMapGreen.left();
                    coinsPerBit = innerMapGreen.rightLong();
                } else {
                    itemID = innerMap.left();
                    coinsPerBit = innerMap.rightLong();
                }
            }
        }
//        LOGGER.info(LOGS_PREFIX + "itemID and coinsPerBit: {}, {}", itemID, coinsPerBit);
        ItemStack foundItemStack = ItemRepository.getItemStack(itemID);
        if (itemID.isEmpty()) {   // a bit dirty, but basically if itemID is empty then it is normal item and NOT category
            lines.add(Text.empty()
                    .append(Text.literal("Bits Cost: ").formatted(Formatting.AQUA))
                    .append(Text.literal(DECIMAL_FORMAT.format(coinsPerBit) + " Coins per bit").formatted(Formatting.DARK_AQUA)));
        } else if (foundItemStack != null && isGreen) {
            lines.add(Text.empty()
                    .append(Text.literal("Bits Cost: ").formatted(Formatting.AQUA))
                    .append(Text.literal(DECIMAL_FORMAT.format(coinsPerBit) + " Coins per bit").formatted(Formatting.DARK_AQUA)));
            lines.add(Text.literal("From " + ItemRepository.getItemStack(itemID).getName().getString()).formatted(Formatting.GREEN));
        } else if (foundItemStack != null) {
            lines.add(Text.empty()
                    .append(Text.literal("Bits Cost: ").formatted(Formatting.AQUA))
                    .append(Text.literal(DECIMAL_FORMAT.format(coinsPerBit) + " Coins per bit").formatted(Formatting.DARK_AQUA)));
            lines.add(Text.literal("From " + ItemRepository.getItemStack(itemID).getName().getString()).formatted(Formatting.DARK_AQUA));
        } else {    // if below so it won't clog logs with that cursed enchanted book
            if (!stack.getName().getString().equals("Stacking Enchants")) LOGGER.warn(LOGS_PREFIX + "ItemStack not found for {}", itemID);
            lines.add(Text.empty()
                    .append(Text.literal("Bits Cost: ").formatted(Formatting.AQUA))
                    .append(Text.literal(DECIMAL_FORMAT.format(coinsPerBit) + " Coins per bit").formatted(Formatting.DARK_AQUA)));
            lines.add(Text.literal("From " + itemID).formatted(Formatting.DARK_AQUA));
        }
    }

    private ObjectLongImmutablePair<String> calculateBestInCategory(ItemStack stack) {
        long bestCoinsPerBitSelling = 0L;
        long bestCoinsPerBitAll = 0L;
        String catName = stack.getName().getString();
        categoryOutput.put(catName, ObjectLongImmutablePair.of("", 0L));
        categoryOutput.put(catName + "GREEN", ObjectLongImmutablePair.of("", 0L));
        Object2LongMap<String> categoryResults = processCategory(stack);
//        LOGGER.info(LOGS_PREFIX + "TRIGGERED INNER CODE FOR PROCESSING: {}", catName);
//        LOGGER.info(LOGS_PREFIX + "categoryResults: {}", categoryResults.toString());

        for (Map.Entry<String, Long> categoryEntry : categoryResults.object2LongEntrySet()) {
            String itemID = categoryEntry.getKey();
            long coinsPerBit = categoryEntry.getValue();

            if (sellingItems.contains(itemID) && (coinsPerBit > bestCoinsPerBitSelling)) {
                categoryOutput.put(catName + "GREEN", ObjectLongImmutablePair.of(itemID, coinsPerBit));
                bestCoinsPerBitSelling = coinsPerBit;
            }

            if (coinsPerBit > bestCoinsPerBitAll) {
                categoryOutput.put(catName, ObjectLongImmutablePair.of(itemID, coinsPerBit));
                bestCoinsPerBitAll = coinsPerBit;
            }
        }
//        LOGGER.info(LOGS_PREFIX + "CURRENT STATE OF categoryOutput: {}, {}", categoryOutput.get(catName).left(), categoryOutput.get(catName).rightLong());
        return categoryOutput.get(catName);
    }

    private BestItemsResult calculateBestItems(Int2ObjectMap<ItemStack> slots) {
        long bestCoinsPerBitSelling = 0L;
        long bestCoinsPerBitAll = 0L;
        bestSlotIndexSelling = -1;
        bestSlotIndexAll = -1;
        if (slots == null || slots.isEmpty()) return new BestItemsResult(bestSlotIndexSelling, bestSlotIndexAll, bestCoinsPerBitSelling, bestCoinsPerBitAll);

        // process categories first
        for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
            ItemStack stack = entry.getValue();
            if (stack == null || stack.isEmpty()) continue;

            if (CATEGORY_PATTERN.matcher(ItemUtils.concatenateLore(ItemUtils.getLore(stack))).find()) {
                String catName = stack.getName().getString();
                ObjectLongImmutablePair<String> categoryResults = calculateBestInCategory(stack);
//                LOGGER.info(LOGS_PREFIX + "TRIGGERED OUTER CODE FOR PROCESSING: {}", catName);

                String itemID = categoryResults.left();
                long coinsPerBit = categoryResults.rightLong();

                if (sellingItems.contains(itemID) && (coinsPerBit > bestCoinsPerBitSelling)) {
                    bestCoinsPerBitSelling = coinsPerBit;
                    bestSlotIndexSelling = entry.getIntKey();
                }

                if (coinsPerBit > bestCoinsPerBitAll) {
                    bestCoinsPerBitAll = coinsPerBit;
                    bestSlotIndexAll = entry.getIntKey();
                }
            }
        }

        for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
            ItemStack stack = entry.getValue();
            if (stack == null || stack.isEmpty()) continue;

            String itemId = stack.getSkyblockApiId();
            String lore = ItemUtils.concatenateLore(ItemUtils.getLore(stack));
            Matcher bitsMatcher = BITS_PATTERN.matcher(lore);
            if (!bitsMatcher.find()) continue;

            long bitsCost = Long.parseLong(bitsMatcher.group("amount").replace(",", ""));
            double itemCost = ItemUtils.getItemPrice(stack).keyDouble() * stack.getCount();
            if (itemCost == 0 || bitsCost == 0) continue;

            long coinsPerBit = Math.round(itemCost / bitsCost);

            if (sellingItems.contains(itemId) && (coinsPerBit > bestCoinsPerBitSelling)) {
                bestCoinsPerBitSelling = coinsPerBit;
                bestSlotIndexSelling = entry.getIntKey();
            }

            if (coinsPerBit > bestCoinsPerBitAll) {
                bestCoinsPerBitAll = coinsPerBit;
                bestSlotIndexAll = entry.getIntKey();
            }
        }
        return new BestItemsResult(bestSlotIndexSelling, bestSlotIndexAll, bestCoinsPerBitSelling, bestCoinsPerBitAll);
    }

    private Object2LongMap<String> processCategory(ItemStack stack) {
        String categoryName = stack.getName().getString();
//      LOGGER.info(LOGS_PREFIX + "Detected category name: {}", categoryName);

        if (categories.containsKey(categoryName)) {
//          LOGGER.info(LOGS_PREFIX + "Key matched for: {}", categoryName);
            Object2LongMap<String> results = new Object2LongOpenHashMap<>();
            Map<String, Integer> category = categories.get(categoryName);

            for (Map.Entry<String, Integer> entry : category.entrySet()) {
                String itemID = entry.getKey();
                Integer itemBitsPrice = entry.getValue();
                double itemCost = ItemUtils.getItemPrice(itemID).keyDouble();
                long coinsPerBit = Math.round(itemCost / itemBitsPrice);
                results.put(itemID, coinsPerBit);
            }
            return results;
        } else if (categoryName.contains("Fuel Blocks")) {
//          LOGGER.info(LOGS_PREFIX + "Fuel Blocks code triggered"); // so that thing gets cheaper if player has blaze slayer 9
            String itemID = "INFERNO_FUEL_BLOCK";    // but I don't know if only 1x offer of 64x offer gets discount too
            int[] itemBitsPrice = {75, 3600};   // if only 1x gets discount then it doesn't matter as x64 would be ALWAYS better even with it
            double itemCost = ItemUtils.getItemPrice(itemID).keyDouble();   // TLDR: need blaze slayer 9 players to show their prices
            long coinsPerBit = (long) (Math.max(itemCost / itemBitsPrice[0], itemCost * 64 / itemBitsPrice[1]));
            Object2LongMap<String> fuelBlockResult = new Object2LongOpenHashMap<>();
            fuelBlockResult.put(itemID, coinsPerBit);
            return fuelBlockResult;
        } else {
            LOGGER.warn(LOGS_PREFIX + "Can't recognize category {} from bits shop! Consider reporting this to our discord!", categoryName);
        }
        return new Object2LongOpenHashMap<>();
    }

    /**
     * List of items that sell well. No, it is not automated, but that stuff is unlikely to change unless some update
     * Definition of "good selling item": 300+ daily sales, 12+ hourly sales
     * I would personally also demand good price per item as filling up all ah slots with catalyst would suck
     * But newer players would find a good use of it so ima keep my preferences away
     * Actual on 14.08.2024
     */
    private final List<String> sellingItems = Arrays.asList(
            "KAT_FLOWER",
            "KAT_BOUQUET",
            "HEAT_CORE",
            "ULTIMATE_CARROT_CANDY_UPGRADE",
            "TALISMAN_ENRICHMENT_SWAPPER",
            "GOD_POTION_2",
            "KISMET_FEATHER",
            "MATRIARCH_PARFUM",
            "AUTOPET_RULES_2"   // it barely made it here
    );

    /**
     * SKYBLOCK_ID, Bits Price
     * Yes I just hardcoded it. Why work smart when you can work hard?
     * Those are only from submenus (categories) as normal item is easy to parse
     * It probably could've been extracted from neu repo or something
     * Actual on 14.08.2024
     */
    private final Object2IntMap<String> CAT_KAT = Util.make(new Object2IntOpenHashMap<>(), map -> {
        map.put("KAT_FLOWER", 500);
        map.put("KAT_BOUQUET", 2500);
    });

    private final Object2IntMap<String> CAT_UPGRADE_COMPONENTS = Util.make(new Object2IntOpenHashMap<>(), map -> {
        map.put("HEAT_CORE", 3000);
        map.put("HYPER_CATALYST_UPGRADE", 300);
        map.put("ULTIMATE_CARROT_CANDY_UPGRADE", 8000);
        map.put("COLOSSAL_EXP_BOTTLE_UPGRADE", 1200);
        map.put("JUMBO_BACKPACK_UPGRADE", 4000);
        map.put("MINION_STORAGE_EXPANDER", 1500);
    });

    private final Object2IntMap<String> CAT_SACKS = Util.make(new Object2IntOpenHashMap<>(), map -> {
        map.put("POCKET_SACK_IN_A_SACK", 8000);
        map.put("LARGE_DUNGEON_SACK", 14000);
        map.put("RUNE_SACK", 14000);
        map.put("FLOWER_SACK", 14000);
        map.put("DWARVEN_MINES_SACK", 14000);
        map.put("CRYSTAL_HOLLOWS_SACK", 14000);
    });

    private final Object2IntMap<String> CAT_ABIPHONE = Util.make(new Object2IntOpenHashMap<>(), map -> {
        map.put("TRIO_CONTACTS_ADDON", 6450);
        map.put("ABICASE_SUMSUNG_1", 15000);
        map.put("ABICASE_SUMSUNG_2", 25000);
        map.put("ABICASE_REZAR", 26000);
        map.put("ABICASE_BLUE_AQUA", 17000);
        map.put("ABICASE_BLUE_BLUE", 17000);
        map.put("ABICASE_BLUE_GREEN", 17000);
        map.put("ABICASE_BLUE_RED", 17000);
        map.put("ABICASE_BLUE_YELLOW", 17000);
    });

    private final Object2IntMap<String> CAT_DYES = Util.make(new Object2IntOpenHashMap<>(), map -> {
        map.put("DYE_PURE_WHITE", 250000);
        map.put("DYE_PURE_BLACK", 250000);
    });

    private final Object2IntMap<String> CAT_ENCHANTS = Util.make(new Object2IntOpenHashMap<>(), map -> {
        map.put("ENCHANTMENT_HECATOMB_1", 6000);
        map.put("ENCHANTMENT_EXPERTISE_1", 4000);
        map.put("ENCHANTMENT_COMPACT_1", 4000);
        map.put("ENCHANTMENT_CULTIVATING_1", 4000);
        map.put("ENCHANTMENT_CHAMPION_1", 4000);
        map.put("ENCHANTMENT_TOXOPHILITE_1", 4000);
    });

    private final Object2IntMap<String> CAT_ENRICHMENTS = Util.make(new Object2IntOpenHashMap<>(), map -> {
        map.put("TALISMAN_ENRICHMENT_SWAPPER", 200);
        map.put("TALISMAN_ENRICHMENT_WALK_SPEED", 5000);
        map.put("TALISMAN_ENRICHMENT_INTELLIGENCE", 5000);
        map.put("TALISMAN_ENRICHMENT_CRITICAL_DAMAGE", 5000);
        map.put("TALISMAN_ENRICHMENT_CRITICAL_CHANCE", 5000);
        map.put("TALISMAN_ENRICHMENT_STRENGTH", 5000);
        map.put("TALISMAN_ENRICHMENT_DEFENSE", 5000);
        map.put("TALISMAN_ENRICHMENT_HEALTH", 5000);
        map.put("TALISMAN_ENRICHMENT_MAGIC_FIND", 5000);
        map.put("TALISMAN_ENRICHMENT_FEROCITY", 5000);
        map.put("TALISMAN_ENRICHMENT_SEA_CREATURE_CHANCE", 5000);
        map.put("TALISMAN_ENRICHMENT_ATTACK_SPEED", 5000);
    });


    private final Map<String, Map<String, Integer>> categories = Util.make(new HashMap<>(), map -> {
        map.put("Kat Items", CAT_KAT);
        map.put("Upgrade Components", CAT_UPGRADE_COMPONENTS);
        map.put("Sacks", CAT_SACKS);
        map.put("Abiphone Supershop", CAT_ABIPHONE);
        map.put("Dyes", CAT_DYES);
        map.put("Stacking Enchants", CAT_ENCHANTS);
        map.put("Enrichments", CAT_ENRICHMENTS);
    });

    private record BestItemsResult(
            int bestSlotIndexSelling,
            int bestSlotIndexAll,
            long bestCoinsPerBitSelling,
            long bestCoinsPerBitAll
    ) {}

    @Override
    public boolean isEnabled() {
        return SkyblockerConfigManager.get().helpers.enableBitsTooltip;
    }

    @Override
    public int getPriority() {
        return 0; // Intended to show first
    }
}