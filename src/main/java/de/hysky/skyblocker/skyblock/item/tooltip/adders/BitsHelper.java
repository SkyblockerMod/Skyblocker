package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLongImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitsHelper extends SimpleContainerSolver implements TooltipAdder {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Pattern BITS_PATTERN = Pattern.compile("Cost (?<amount>[\\d,]+) Bits");
	private static final Pattern CATEGORY_PATTERN = Pattern.compile("Click to browse!");
	@Language("RegExp")
	private static final String TITLE_PATTERN = ".*(?:Community Shop|Bits Shop).*";
	private static final String LOGS_PREFIX = "[Skyblocker Bits Helper] ";

	//region Constants
	/**
	 * List of items that sell well. No, it is not automated, but that stuff is unlikely to change unless some update
	 * Definition of "good selling item": 300+ daily sales, 12+ hourly sales
	 * I would personally also demand good price per item as filling up all ah slots with catalyst would suck
	 * But newer players would find a good use of it so ima keep my preferences away
	 * Actual on 14.08.2024
	 */
	private static final List<String> GOOD_SELLING_ITEMS = List.of(
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
	private static final Object2IntMap<String> CAT_KAT = Util.make(new Object2IntOpenHashMap<>(), map -> {
		map.put("KAT_FLOWER", 500);
		map.put("KAT_BOUQUET", 2500);
	});

	private static final Object2IntMap<String> CAT_UPGRADE_COMPONENTS = Util.make(new Object2IntOpenHashMap<>(), map -> {
		map.put("HEAT_CORE", 3000);
		map.put("HYPER_CATALYST_UPGRADE", 300);
		map.put("ULTIMATE_CARROT_CANDY_UPGRADE", 8000);
		map.put("COLOSSAL_EXP_BOTTLE_UPGRADE", 1200);
		map.put("JUMBO_BACKPACK_UPGRADE", 4000);
		map.put("MINION_STORAGE_EXPANDER", 1500);
	});

	private static final Object2IntMap<String> CAT_SACKS = Util.make(new Object2IntOpenHashMap<>(), map -> {
		map.put("POCKET_SACK_IN_A_SACK", 8000);
		map.put("LARGE_DUNGEON_SACK", 14000);
		map.put("RUNE_SACK", 14000);
		map.put("FLOWER_SACK", 14000);
		map.put("DWARVEN_MINES_SACK", 14000);
		map.put("CRYSTAL_HOLLOWS_SACK", 14000);
	});

	private static final Object2IntMap<String> CAT_ABIPHONE = Util.make(new Object2IntOpenHashMap<>(), map -> {
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

	private static final Object2IntMap<String> CAT_DYES = Util.make(new Object2IntOpenHashMap<>(), map -> {
		map.put("DYE_PURE_WHITE", 250000);
		map.put("DYE_PURE_BLACK", 250000);
	});

	private static final Object2IntMap<String> CAT_ENCHANTS = Util.make(new Object2IntOpenHashMap<>(), map -> {
		map.put("ENCHANTMENT_HECATOMB_1", 6000);
		map.put("ENCHANTMENT_EXPERTISE_1", 4000);
		map.put("ENCHANTMENT_COMPACT_1", 4000);
		map.put("ENCHANTMENT_CULTIVATING_1", 4000);
		map.put("ENCHANTMENT_CHAMPION_1", 4000);
		map.put("ENCHANTMENT_TOXOPHILITE_1", 4000);
	});

	private static final Object2IntMap<String> CAT_ENRICHMENTS = Util.make(new Object2IntOpenHashMap<>(), map -> {
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

	private static final Map<String, Map<String, Integer>> CATEGORIES = Map.of(
			"Kat Items", CAT_KAT,
			"Upgrade Components", CAT_UPGRADE_COMPONENTS,
			"Sacks", CAT_SACKS,
			"Abiphone Supershop", CAT_ABIPHONE,
			"Dyes", CAT_DYES,
			"Stacking Enchants", CAT_ENCHANTS,
			"Enrichments", CAT_ENRICHMENTS
	);
	//endregion

	public static final BitsHelper INSTANCE = new BitsHelper();

	private final Map<String, ObjectLongImmutablePair<String>> categoryOutput = new HashMap<>();
	private int bestSlotIndexSelling = -1;  // index of slot that has the best item from "white list" of good selling items (= items that sell quick)
	private int bestSlotIndexAll = -1;  // index of slot that has the best item overall. May has same result as bestSlotIndexSelling - it is intended
	private boolean megamindInLogs = false; // comedy

	private BitsHelper() {
		super(TITLE_PATTERN);
	}

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

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		String lore = ItemUtils.concatenateLore(lines);
		Matcher bitsMatcher = BITS_PATTERN.matcher(lore);
		long coinsPerBit = 0L;
		String itemID = "";
		boolean isGreen = false;
		if (focusedSlot == null) return;

		if (!CATEGORY_PATTERN.matcher(lore).find()) {
			if (!bitsMatcher.find()) return;

			long bitsCost = Long.parseLong(bitsMatcher.group("amount").replace(",", ""));
			double itemCost = ItemUtils.getItemPrice(stack).keyDouble() * stack.getCount();

			if (itemCost == 0) return;

			coinsPerBit = Math.round(itemCost / bitsCost);
		} else {
			String outerKey = stack.getHoverName().getString();
			ObjectLongPair<String> innerMap, innerMapGreen;

			BestItemsResult bestItemsResult = calculateBestItems(getSlots());
			bestSlotIndexSelling = bestItemsResult.bestSlotIndexSelling;
			bestSlotIndexAll = bestItemsResult.bestSlotIndexAll;
			if (bestSlotIndexSelling != -1 && bestSlotIndexSelling == focusedSlot.getContainerSlot()) isGreen = true;
			innerMap = categoryOutput.get(outerKey);
			innerMapGreen = categoryOutput.get(outerKey + "GREEN");

			if (innerMap != null) {
				if (isGreen) {  // this is here so green highlighted category won't show yellow stuff
					itemID = innerMapGreen.left();
					coinsPerBit = innerMapGreen.rightLong();
				} else {
					itemID = innerMap.left();
					coinsPerBit = innerMap.rightLong();
				}
			}
		}
		ItemStack foundItemStack = ItemRepository.getItemStack(itemID);
		if (itemID.isEmpty()) {   // a bit dirty, but basically if itemID is empty then it is normal item and NOT category
			lines.add(Component.empty()
					.append(Component.literal("Bits Cost: ").withStyle(ChatFormatting.AQUA))
					.append(Component.literal(Formatters.INTEGER_NUMBERS.format(coinsPerBit) + " Coins per bit").withStyle(ChatFormatting.DARK_AQUA)));
		} else if (foundItemStack != null && isGreen) {
			lines.add(Component.empty()
					.append(Component.literal("Bits Cost: ").withStyle(ChatFormatting.AQUA))
					.append(Component.literal(Formatters.INTEGER_NUMBERS.format(coinsPerBit) + " Coins per bit").withStyle(ChatFormatting.DARK_AQUA)));
			lines.add(Component.literal("From " + foundItemStack.getHoverName().getString()).withStyle(ChatFormatting.GREEN));
		} else if (foundItemStack != null) {
			lines.add(Component.empty()
					.append(Component.literal("Bits Cost: ").withStyle(ChatFormatting.AQUA))
					.append(Component.literal(Formatters.INTEGER_NUMBERS.format(coinsPerBit) + " Coins per bit").withStyle(ChatFormatting.DARK_AQUA)));
			lines.add(Component.literal("From " + foundItemStack.getHoverName().getString()).withStyle(ChatFormatting.DARK_AQUA));
		} else {    // if below so it won't clog logs with that cursed enchanted book
			if (!stack.getHoverName().getString().equals("Stacking Enchants")) LOGGER.warn(LOGS_PREFIX + "ItemStack not found for {}", itemID);
			lines.add(Component.empty()
					.append(Component.literal("Bits Cost: ").withStyle(ChatFormatting.AQUA))
					.append(Component.literal(Formatters.INTEGER_NUMBERS.format(coinsPerBit) + " Coins per bit").withStyle(ChatFormatting.DARK_AQUA)));
			lines.add(Component.literal("From " + itemID).withStyle(ChatFormatting.DARK_AQUA));
		}
	}

	/**
	 * Why there is no simpler way to get those?
	 * upd: there is, using `focusedSlot.inventory`, but I don't wanna touch code that already works just fine
	 */
	private Int2ObjectMap<ItemStack> getSlots() {
		Minecraft client = Minecraft.getInstance();
		if (client.screen instanceof AbstractContainerScreen<?> screen) {
			AbstractContainerMenu handler = screen.getMenu();

			Int2ObjectMap<ItemStack> slots = new Int2ObjectOpenHashMap<>();
			for (int i = 0; i < handler.slots.size(); i++) {
				slots.put(i, handler.getSlot(i).getItem());
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

	private BestItemsResult calculateBestItems(Int2ObjectMap<ItemStack> slots) {
		long bestCoinsPerBitSelling = 0L;
		long bestCoinsPerBitAll = 0L;
		int bestSlotIndexSelling = -1;
		int bestSlotIndexAll = -1;
		if (slots == null || slots.isEmpty()) return new BestItemsResult(bestSlotIndexSelling, bestSlotIndexAll, bestCoinsPerBitSelling, bestCoinsPerBitAll);

		// process categories first
		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			ItemStack stack = entry.getValue();
			if (stack == null || stack.isEmpty()) continue;

			if (CATEGORY_PATTERN.matcher(ItemUtils.getConcatenatedLore(stack)).find()) {
				ObjectLongImmutablePair<String> categoryResults = calculateBestInCategory(stack);

				String itemID = categoryResults.left();
				long coinsPerBit = categoryResults.rightLong();

				if (GOOD_SELLING_ITEMS.contains(itemID) && (coinsPerBit > bestCoinsPerBitSelling)) {
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
			String lore = ItemUtils.getConcatenatedLore(stack);
			Matcher bitsMatcher = BITS_PATTERN.matcher(lore);
			if (!bitsMatcher.find()) continue;

			long bitsCost = Long.parseLong(bitsMatcher.group("amount").replace(",", ""));
			double itemCost = ItemUtils.getItemPrice(stack).keyDouble() * stack.getCount();
			if (itemCost == 0 || bitsCost == 0) continue;

			long coinsPerBit = Math.round(itemCost / bitsCost);

			if (GOOD_SELLING_ITEMS.contains(itemId) && (coinsPerBit > bestCoinsPerBitSelling)) {
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

	private ObjectLongImmutablePair<String> calculateBestInCategory(ItemStack stack) {
		long bestCoinsPerBitSelling = 0L;
		long bestCoinsPerBitAll = 0L;
		String catName = stack.getHoverName().getString();
		categoryOutput.put(catName, ObjectLongImmutablePair.of("", 0L));
		categoryOutput.put(catName + "GREEN", ObjectLongImmutablePair.of("", 0L));
		Object2LongMap<String> categoryResults = processCategory(stack);

		for (Map.Entry<String, Long> categoryEntry : categoryResults.object2LongEntrySet()) {
			String itemID = categoryEntry.getKey();
			long coinsPerBit = categoryEntry.getValue();

			if (GOOD_SELLING_ITEMS.contains(itemID) && (coinsPerBit > bestCoinsPerBitSelling)) {
				categoryOutput.put(catName + "GREEN", ObjectLongImmutablePair.of(itemID, coinsPerBit));
				bestCoinsPerBitSelling = coinsPerBit;
			}

			if (coinsPerBit > bestCoinsPerBitAll) {
				categoryOutput.put(catName, ObjectLongImmutablePair.of(itemID, coinsPerBit));
				bestCoinsPerBitAll = coinsPerBit;
			}
		}
		return categoryOutput.get(catName);
	}

	private Object2LongMap<String> processCategory(ItemStack stack) {
		String categoryName = stack.getHoverName().getString();

		if (CATEGORIES.containsKey(categoryName)) {
			Object2LongMap<String> results = new Object2LongOpenHashMap<>();
			Map<String, Integer> category = CATEGORIES.get(categoryName);

			for (Map.Entry<String, Integer> entry : category.entrySet()) {
				String itemID = entry.getKey();
				Integer itemBitsPrice = entry.getValue();
				double itemCost = ItemUtils.getItemPrice(itemID).keyDouble();
				long coinsPerBit = Math.round(itemCost / itemBitsPrice);
				results.put(itemID, coinsPerBit);
			}
			return results;
		} else if (categoryName.contains("Fuel Blocks")) {
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

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.enableBitsTooltip;
	}

	@Override
	public int getPriority() {
		return 0; // Intended to show first
	}

	private record BestItemsResult(int bestSlotIndexSelling, int bestSlotIndexAll, long bestCoinsPerBitSelling, long bestCoinsPerBitAll) {}
}
