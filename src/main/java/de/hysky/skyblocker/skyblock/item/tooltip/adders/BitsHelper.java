package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.InventoryScreenMixin;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.Inventory;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitsHelper extends SimpleContainerSolver implements TooltipAdder {
    private static final Pattern BITS_PATTERN = Pattern.compile("Cost (?<amount>[\\d,]+) Bits");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("Click to browse!");
    @Language("RegExp") private static final String TITLE_PATTERN = ".*(?:Community Shop|Bits Shop).*";
    private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getInstance(Locale.US);
    private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Bits");

    public static final BitsHelper INSTANCE = new BitsHelper();

    public BitsHelper() {
        super(TITLE_PATTERN);
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfigManager.get().helpers.enableBitsTooltip;
    }

    /**
     * Gets price from ItemStack
     * extracts ID from stack
     */
    double getPrice(ItemStack stack) {
        double itemCost = 0;
        String itemID = stack.getSkyblockApiId();

        if (TooltipInfoType.BAZAAR.getData().has(itemID)) {
            itemCost = TooltipInfoType.BAZAAR.getData().getAsJsonObject(stack.getSkyblockApiId()).get("buyPrice").getAsDouble();
        } else if (TooltipInfoType.LOWEST_BINS.getData().has(itemID)) {
            itemCost = TooltipInfoType.LOWEST_BINS.getData().get(stack.getSkyblockApiId()).getAsDouble();
        }
        return itemCost;
    }

    /**
     * Gets price from itemID
     */
    double getPrice(String itemID) {
        double itemCost = 0;

        if (TooltipInfoType.BAZAAR.getData().has(itemID)) {
            itemCost = TooltipInfoType.BAZAAR.getData().getAsJsonObject(itemID).get("buyPrice").getAsDouble();
        } else if (TooltipInfoType.LOWEST_BINS.getData().has(itemID)) {
            itemCost = TooltipInfoType.LOWEST_BINS.getData().get(itemID).getAsDouble();
        }
        return itemCost;
    }

    private Map<String, Pair<String, Long>> categoryOutput = new HashMap<>();
    private int bestSlotIndexSelling = -1;
    private int bestSlotIndexAll = -1;

    // TODO: всё хуйня, переделывай
    @Override
    public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
//        categoryOutput.clear();     // we need it to allow this thing to refresh
        LOGGER.warn("getColors triggered!");
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
     * That thing is needed here to avoid NPE/0 coins per bit bs on first iteration
     */
    private Int2ObjectMap<ItemStack> getSlots() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof HandledScreen) {
            HandledScreen<?> screen = (HandledScreen<?>) client.currentScreen;
            ScreenHandler handler = screen.getScreenHandler();

            Int2ObjectMap<ItemStack> slots = new Int2ObjectOpenHashMap<>();
            for (int i = 0; i < handler.slots.size(); i++) {
                slots.put(i, handler.getSlot(i).getStack());
            }
            LOGGER.info("No slots?\n" +
                    "⠀⣞⢽⢪⢣⢣⢣⢫⡺⡵⣝⡮⣗⢷⢽⢽⢽⣮⡷⡽⣜⣜⢮⢺⣜⢷⢽⢝⡽⣝\n" +
                    "⠸⡸⠜⠕⠕⠁⢁⢇⢏⢽⢺⣪⡳⡝⣎⣏⢯⢞⡿⣟⣷⣳⢯⡷⣽⢽⢯⣳⣫⠇\n" +
                    "⠀⠀⢀⢀⢄⢬⢪⡪⡎⣆⡈⠚⠜⠕⠇⠗⠝⢕⢯⢫⣞⣯⣿⣻⡽⣏⢗⣗⠏⠀\n" +
                    "⠀⠪⡪⡪⣪⢪⢺⢸⢢⢓⢆⢤⢀⠀⠀⠀⠀⠈⢊⢞⡾⣿⡯⣏⢮⠷⠁⠀⠀\n" +
                    "⠀⠀⠀⠈⠊⠆⡃⠕⢕⢇⢇⢇⢇⢇⢏⢎⢎⢆⢄⠀⢑⣽⣿⢝⠲⠉⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠀⡿⠂⠠⠀⡇⢇⠕⢈⣀⠀⠁⠡⠣⡣⡫⣂⣿⠯⢪⠰⠂⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⡦⡙⡂⢀⢤⢣⠣⡈⣾⡃⠠⠄⠀⡄⢱⣌⣶⢏⢊⠂⠀⠀⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⢝⡲⣜⡮⡏⢎⢌⢂⠙⠢⠐⢀⢘⢵⣽⣿⡿⠁⠁⠀⠀⠀⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠨⣺⡺⡕⡕⡱⡑⡆⡕⡅⡕⡜⡼⢽⡻⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⣼⣳⣫⣾⣵⣗⡵⡱⡡⢣⢑⢕⢜⢕⡝⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⣴⣿⣾⣿⣿⣿⡿⡽⡑⢌⠪⡢⡣⣣⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⡟⡾⣿⢿⢿⢵⣽⣾⣼⣘⢸⢸⣞⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                    "⠀⠀⠀⠀⠁⠇⠡⠩⡫⢿⣝⡻⡮⣒⢽⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
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
            double itemCost = getPrice(stack) * stack.getCount();

            if (itemCost == 0) return;

            coinsPerBit = Math.round(itemCost / bitsCost);
        } else {
            String outerKey = stack.getName().getString();
            Pair<String, Long> innerMap = categoryOutput.get(outerKey);
            Pair<String, Long> innerMapGreen = categoryOutput.get(outerKey + "GREEN"); // not really null safe but we don't care as it should be triggered only when we KNOW there is a green thing
            if (bestSlotIndexSelling != -1) if (bestSlotIndexSelling == focusedSlot.getIndex()) isGreen = true;

            if (innerMap == null) {
                LOGGER.warn("TRIGGERED innerMap == null");
                BestItemsResult result = calculateBestItems(getSlots());
                innerMap = categoryOutput.get(outerKey);
                innerMapGreen = categoryOutput.get(outerKey + "GREEN");
            }

            if (innerMap != null) {
                LOGGER.info("innerMap STATE: {}, {}", innerMap.getLeft(), innerMap.getRight());
                if (isGreen) {  // this is here so green highlighted category won't show yellow stuff
                    itemID = innerMapGreen.getLeft();
                    coinsPerBit = innerMapGreen.getRight();
                } else {
                    itemID = innerMap.getLeft();
                    coinsPerBit = innerMap.getRight();
                }
            }
        }
        LOGGER.info("itemID and coinsPerBit: {}, {}", itemID, coinsPerBit);
        ItemStack foundItemStack = ItemRepository.getItemStack(itemID);
        if (Objects.equals(itemID, "")) {
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
        } else {
            LOGGER.warn("ItemStack not found for {}", itemID);
            lines.add(Text.empty()
                    .append(Text.literal("Bits Cost: ").formatted(Formatting.AQUA))
                    .append(Text.literal(DECIMAL_FORMAT.format(coinsPerBit) + " Coins per bit").formatted(Formatting.DARK_AQUA)));
            lines.add(Text.literal("From " + itemID).formatted(Formatting.DARK_AQUA));
        }
    }

    private Pair<String, Long> calculateBestInCategory(ItemStack stack) {
        long bestCoinsPerBitSelling = 0L;
        long bestCoinsPerBitAll = 0L;
        String catName = stack.getName().getString();
        categoryOutput.put(catName, new Pair<>("", 0L));
        categoryOutput.put(catName+"GREEN", new Pair<>("", 0L));
        Map<String, Long> categoryResults = processCategory(stack);
        LOGGER.warn("TRIGGERED INNER CODE FOR PROCESSING: {}", catName);
        LOGGER.info("categoryResults: {}", categoryResults.toString());

        for (Map.Entry<String, Long> categoryEntry : categoryResults.entrySet()) {
            String itemID = categoryEntry.getKey();
            long coinsPerBit = categoryEntry.getValue();

            if (sellingItems.contains(itemID) && (coinsPerBit > bestCoinsPerBitSelling)) {
                categoryOutput.get(catName + "GREEN").setLeft(itemID);
                categoryOutput.get(catName + "GREEN").setRight(coinsPerBit);
                bestCoinsPerBitSelling = coinsPerBit;
            }

            if (coinsPerBit > bestCoinsPerBitAll) {
                categoryOutput.get(catName).setLeft(itemID);
                categoryOutput.get(catName).setRight(coinsPerBit);
                bestCoinsPerBitAll = coinsPerBit;
            }
        }
        LOGGER.info("CURRENT STATE OF categoryOutput: {}, {}", categoryOutput.get(catName).getLeft(), categoryOutput.get(catName).getRight());
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
                categoryOutput.put(catName, new Pair<>("", 0L));
                categoryOutput.put(catName+"GREEN", new Pair<>("", 0L));
                Pair<String, Long> categoryResults = calculateBestInCategory(stack);
                LOGGER.warn("TRIGGERED OUTER CODE FOR PROCESSING: {}", catName);

                String itemID = categoryResults.getLeft();
                long coinsPerBit = categoryResults.getRight();

                if (sellingItems.contains(itemID) && (coinsPerBit > bestCoinsPerBitSelling)) {
//                    categoryOutput.get(catName + "GREEN").setLeft(itemID);
//                    categoryOutput.get(catName + "GREEN").setRight(coinsPerBit);
                    bestCoinsPerBitSelling = coinsPerBit;
                    bestSlotIndexSelling = entry.getIntKey();
                }

                if (coinsPerBit > bestCoinsPerBitAll) {
//                    categoryOutput.get(catName).setLeft(itemID);
//                    categoryOutput.get(catName).setRight(coinsPerBit);
                    bestCoinsPerBitAll = coinsPerBit;
                    bestSlotIndexAll = entry.getIntKey();
                }
//                LOGGER.info("CURRENT STATE OF categoryOutput: {}, {}", categoryOutput.get(catName + "GREEN").getLeft(), categoryOutput.get(catName + "GREEN").getRight());
            }
        }

        for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
            ItemStack stack = entry.getValue();
            if (stack == null || stack.isEmpty()) continue;

            String itemId = stack.getSkyblockApiId(); // Получаем ID предмета
            String lore = ItemUtils.concatenateLore(ItemUtils.getLore(stack));
            Matcher bitsMatcher = BITS_PATTERN.matcher(lore);
            if (!bitsMatcher.find()) continue;

            long bitsCost = Long.parseLong(bitsMatcher.group("amount").replace(",", ""));
            double itemCost = getPrice(stack) * stack.getCount();
            if (itemCost == 0 || bitsCost == 0) continue;

            long coinsPerBit = Math.round(itemCost / bitsCost);

            // Проверка на лучший предмет из хорошо продающихся
            if (sellingItems.contains(itemId) && (coinsPerBit > bestCoinsPerBitSelling)) {
                bestCoinsPerBitSelling = coinsPerBit;
                bestSlotIndexSelling = entry.getIntKey();
            }

            // Проверка на лучший предмет из всех
            if (coinsPerBit > bestCoinsPerBitAll) {
                bestCoinsPerBitAll = coinsPerBit;
                bestSlotIndexAll = entry.getIntKey();
            }
        }
        return new BestItemsResult(bestSlotIndexSelling, bestSlotIndexAll, bestCoinsPerBitSelling, bestCoinsPerBitAll);
    }


    private Map<String, Long> processCategory(ItemStack stack) {
        String categoryName = stack.getName().getString();
        LOGGER.info("Detected category name: {}", categoryName);

        if (categories.containsKey(categoryName)) {
            LOGGER.info("Key matched for: {}", categoryName);
            Map<String, Long> results = new HashMap<>();
            Map<String, Integer> category = categories.get(categoryName);

//            long bestCoinsPerBitSelling = 0L;
//            long bestCoinsPerBitAll = 0L;
            for (Map.Entry<String, Integer> entry : category.entrySet()) {
                String itemID = entry.getKey(); // ID предмета
                Integer itemBitsPrice = entry.getValue(); // цена в битах
                double itemCost = getPrice(itemID);
                LOGGER.info("Line processed: {} item, {} price in bits, {} price in coins", itemID, itemBitsPrice, itemCost);
                long coinsPerBit = Math.round(itemCost / itemBitsPrice);
                results.put(itemID, coinsPerBit); // Добавляем пару "ID, coinsPerBit" в результат
//                if (coinsPerBit > bestCoinsPerBitSelling) {
//                    categoryOutput.get(categoryName+"GREEN").setLeft(itemID);
//                    categoryOutput.get(categoryName+"GREEN").setRight(coinsPerBit);
//                }
//                if (coinsPerBit > bestCoinsPerBitAll) {
//                    categoryOutput.get(categoryName).setLeft(itemID);
//                    categoryOutput.get(categoryName).setRight(coinsPerBit);
//                }
            }
            return results;
        } else if (categoryName.contains("Fuel Blocks")) {
            LOGGER.info("Fuel Blocks code triggered");
            String itemID = "INFERNO_FUEL_BLOCK";
            int[] itemBitsPrice = {75, 3600};
            double itemCost = getPrice(itemID);
            long coinsPerBit = (long) (Math.max(itemCost / itemBitsPrice[0], itemCost * 64 / itemBitsPrice[1]));
            return Collections.singletonMap(itemID, coinsPerBit);
        } else {
            LOGGER.warn("Can't recognize category {} from bits shop! Consider reporting this to our discord!", categoryName);
        }
        return Collections.emptyMap();
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

    private String getGreen() {
        for (String item: sellingItems) {

        }
        return null;
    };


    /**
     * SKYBLOCK_ID, Bits Price
     * Yes I just hardcoded it. Why work smart when you can work hard?
     * It probably could've been extracted from neu repo or something
     * Actual on 14.08.2024
     */
    private final Map<String, Integer> catKat = Util.make(new HashMap<>(), map -> {
        map.put("KAT_FLOWER", 500);
        map.put("KAT_BOUQUET", 2500);
    });

    private final Map<String, Integer> catUpgradeComponents = Util.make(new HashMap<>(), map -> {
        map.put("HEAT_CORE", 3000);
        map.put("HYPER_CATALYST_UPGRADE", 300);
        map.put("ULTIMATE_CARROT_CANDY_UPGRADE", 8000);
        map.put("COLOSSAL_EXP_BOTTLE_UPGRADE", 1200);
        map.put("JUMBO_BACKPACK_UPGRADE", 4000);
        map.put("MINION_STORAGE_EXPANDER", 1500);
    });

    private final Map<String, Integer> catSacks = Util.make(new HashMap<>(), map -> {
        map.put("POCKET_SACK_IN_A_SACK", 8000);
        map.put("LARGE_DUNGEON_SACK", 14000);   //sacks can't be traded, but I will add them anyway in case they will be auctionable at some point
        map.put("RUNE_SACK", 14000);
        map.put("FLOWER_SACK", 14000);
        map.put("DWARVEN_MINES_SACK", 14000);
        map.put("CRYSTAL_HOLLOWS_SACK", 14000);
    });

    private final Map<String, Integer> catAbiphone = Util.make(new HashMap<>(), map -> {
        map.put("TRIO_CONTACTS_ADDON", 6450);
        map.put("ABICASE_SUMSUNG_1", 15000); // Original skibidiblock ID is all "ABICASE" with "Model" extra data. Admins why
        map.put("ABICASE_SUMSUNG_2", 25000);
        map.put("ABICASE_REZAR", 26000);
        map.put("ABICASE_BLUE_AQUA", 17000);
        map.put("ABICASE_BLUE_BLUE", 17000);
        map.put("ABICASE_BLUE_GREEN", 17000);
        map.put("ABICASE_BLUE_RED", 17000);
        map.put("ABICASE_BLUE_YELLOW", 17000);
    });

    private final Map<String, Integer> catDyes = Util.make(new HashMap<>(), map -> {
        map.put("DYE_PURE_WHITE", 250000);
        map.put("DYE_PURE_BLACK", 250000);
    });

    private final Map<String, Integer> catEnchants = Util.make(new HashMap<>(), map -> {
        map.put("ENCHANTMENT_HECATOMB_1", 6000);
        map.put("ENCHANTMENT_EXPERTISE_1", 4000);
        map.put("ENCHANTMENT_COMPACT_1", 4000);
        map.put("ENCHANTMENT_CULTIVATING_1", 4000);
        map.put("ENCHANTMENT_CHAMPION_1", 4000);
        map.put("ENCHANTMENT_TOXOPHILITE_1", 4000);
    });

    private final Map<String, Integer> catEnrichments = Util.make(new HashMap<>(), map -> {
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
        // Категории
        map.put("Kat Items", catKat);
        map.put("Upgrade Components", catUpgradeComponents);
        map.put("Sacks", catSacks);
        map.put("Abiphone Supershop", catAbiphone);
        map.put("Dyes", catDyes);
        map.put("Stacking Enchants", catEnchants);
        map.put("Enrichments", catEnrichments);
    });

    // I'm not sorry
    private static class BestItemsResult {
        int bestSlotIndexSelling;
        int bestSlotIndexAll;
        long bestCoinsPerBitSelling;
        long bestCoinsPerBitAll;

        BestItemsResult(int bestSlotIndexSelling, int bestSlotIndexAll, long bestCoinsPerBitSelling, long bestCoinsPerBitAll) {
            this.bestSlotIndexSelling = bestSlotIndexSelling;
            this.bestSlotIndexAll = bestSlotIndexAll;
            this.bestCoinsPerBitSelling = bestCoinsPerBitSelling;
            this.bestCoinsPerBitAll = bestCoinsPerBitAll;
        }
    }

    @Override
    public int getPriority() {
        return 0; // Intended to show first
    }
}