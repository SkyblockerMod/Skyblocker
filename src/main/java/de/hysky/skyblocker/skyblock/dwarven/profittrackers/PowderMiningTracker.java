package de.hysky.skyblocker.skyblock.dwarven.profittrackers;

import com.mojang.brigadier.Command;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ItemPriceUpdateEvent;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class PowderMiningTracker extends AbstractProfitTracker {
	public static final PowderMiningTracker INSTANCE = new PowderMiningTracker();
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Powder Mining Tracker");
	private static final Object2ObjectArrayMap<String, String> NAME2ID_MAP = new Object2ObjectArrayMap<>(50);

	/**
	 * <p>
	 * Holds the total amount of each reward obtained for the current profile.
	 * If any items are filtered out, they are still added to this map but not to the {@link #shownRewards} map.
	 * Once the filter is changed, the {@link #shownRewards} map is cleared and recalculated based on this map.
	 * </p>
	 * <p>This is similar to how {@link ChatComponent#messages} and {@link ChatComponent#visibleMessages} behave.</p>
	 *
	 * @implNote This is a map of item IDs to the amount of that item obtained.
	 */
	@SuppressWarnings("JavadocReference")
	private Object2IntMap<String> currentProfileRewards = new Object2IntOpenHashMap<>();

	// This constructor takes in a comparator that is triggered to decide where to add the element in the tree map
	// This causes it to be sorted at all times. This is for rendering them in a sort of easy-to-read manner.
	private final Object2IntAVLTreeMap<Component> shownRewards = new Object2IntAVLTreeMap<>(Comparator.<Component>comparingInt(text -> comparePriority(text.getString())).thenComparing(c -> c.getString()));

	/**
	 * Holds the total reward maps for all accounts and profiles. {@link #currentProfileRewards} is a subset of this map, updated on profile change.
	 */
	private final ProfiledData<Object2IntMap<String>> allRewards = new ProfiledData<>(getRewardFilePath("powder-mining.json"), CodecUtils.object2IntMapCodec(Codec.STRING));
	private boolean insideChestMessage = false;
	private double profit = 0;

	private PowderMiningTracker() {} // Singleton

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().mining.crystalHollows.enablePowderTracker;
	}

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(INSTANCE::onChatMessage);
		ItemPriceUpdateEvent.ON_PRICE_UPDATE.register(INSTANCE::onPriceUpdate);

		INSTANCE.allRewards.init();

		// @formatter:off // Don't you hate it when your format style for chained method calls makes a chain like this incredibly ugly?
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
			literal(SkyblockerMod.NAMESPACE)
				.then(literal("rewardTrackers")
					.then(literal("powderMining")
						.then(literal("list")
							.executes(ctx -> {
								if (INSTANCE.currentProfileRewards.isEmpty()) {
									ctx.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.powderTracker.emptyHistory").withStyle(ChatFormatting.RED)));
									return Command.SINGLE_SUCCESS;
								} else if (INSTANCE.shownRewards.isEmpty()) {
									ctx.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.powderTracker.rewardsFilteredOut").withStyle(ChatFormatting.RED)));
									return Command.SINGLE_SUCCESS;
								}

								for (Entry<Component> entry : INSTANCE.shownRewards.object2IntEntrySet()) {
									ctx.getSource().sendFeedback(
											Component.empty()
												.append(entry.getKey())
												.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
												.append(Component.literal(String.valueOf(entry.getIntValue()))));
								}
								ctx.getSource().sendFeedback(Component.translatable("skyblocker.powderTracker.profit", NumberFormat.getInstance().format(INSTANCE.profit)).withStyle(ChatFormatting.GOLD));
								return Command.SINGLE_SUCCESS;
							})
						)
						.then(literal("reset")
							.executes(ctx -> {
								INSTANCE.currentProfileRewards.clear();
								INSTANCE.allRewards.save();
								INSTANCE.shownRewards.clear();
								ctx.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.powderTracker.historyReset").withStyle(ChatFormatting.GREEN)));
								return Command.SINGLE_SUCCESS;
							})
						)
					)
				)
		)); // @formatter:on

		SkyblockEvents.PROFILE_CHANGE.register(INSTANCE::onProfileChange);
	}

	private void onProfileChange(String prevProfileId, String newProfileId) {
		if (!isEnabled()) return;
		currentProfileRewards = allRewards.computeIfAbsent(Object2IntArrayMap::new);
		recalculateAll();
	}

	@SuppressWarnings("SameReturnValue")
	private boolean onChatMessage(Component text, boolean overlay) {
		if (Utils.getLocation() != Location.CRYSTAL_HOLLOWS || !INSTANCE.isEnabled() || overlay) return true;
		String message = text.getString();
		// Reward messages end with a separator like so
		if (insideChestMessage && message.equals("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) {
			insideChestMessage = false;
			return true;
		}

		if (!insideChestMessage && (message.equals("  CHEST LOCKPICKED ") || (SkyblockerConfigManager.get().mining.crystalHollows.countNaturalChestsInTracker && message.equals("  LOOT CHEST COLLECTED ")))) {
			insideChestMessage = true;
			return true;
		}

		if (!insideChestMessage) return true;
		Matcher matcher = REWARD_PATTERN.matcher(message);
		if (!matcher.matches()) return true;
		String itemName = matcher.group(1);
		int amount = NumberUtils.toInt(matcher.group(2).replace(",", ""), 1);

		String itemId = getItemId(itemName);
		if (itemId.isEmpty()) {
			LOGGER.error("No matching item id for name `{}`. Report this!", itemName);
			return true;
		}
		incrementReward(itemName, itemId, amount);
		calculateProfitForItem(itemId, amount);
		return true;
	}

	private void incrementReward(String itemName, String itemId, int amount) {
		currentProfileRewards.mergeInt(itemId, amount, Integer::sum);
		if (!SkyblockerConfigManager.get().mining.crystalHollows.powderTrackerFilter.contains(itemName)) {
			if (itemId.equals("GEMSTONE_POWDER")) {
				shownRewards.merge(Component.literal("Gemstone Powder").withStyle(ChatFormatting.LIGHT_PURPLE), amount, Integer::sum);
			} else {
				ItemStack stack = ItemRepository.getItemStack(itemId);
				if (stack == null) {
					LOGGER.warn("Item stack for id `{}` is null! This might be caused by failed item repository downloads.", itemId);
					return;
				}
				shownRewards.merge(stack.getHoverName(), amount, Integer::sum);
			}
		}
	}

	private static int comparePriority(String itemName) {
		// Puts gemstone powder at the top of the list, then gold and diamond essence, then gemstones by ascending rarity and then whatever else.
		return switch (replaceGemstoneSymbols(itemName)) {
			case "Gemstone Powder" -> 1;
			case "Gold Essence" -> 2;
			case "Diamond Essence" -> 3;
			case String s when s.startsWith("Rough") -> 4;
			case String s when s.startsWith("Flawed") -> 5;
			case String s when s.startsWith("Fine") -> 6;
			case String s when s.startsWith("Flawless") -> 7;
			default -> 8;
		};
	}

	private void onPriceUpdate() {
		if (isEnabled()) recalculatePrices();
	}

	/**
	 * Normally, the price is calculated on a per-reward basis as they are obtained. This is what this method does.
	 */
	private void calculateProfitForItem(String itemId, int amount) {
		DoubleBooleanPair price = ItemUtils.getItemPrice(itemId);
		if (price.rightBoolean()) profit += price.leftDouble() * amount;
	}

	/**
	 * When the bz/ah prices are updated, this method recalculates the profit for all rewards at once.
	 */
	private void recalculatePrices() {
		profit = 0;
		ObjectSortedSet<Entry<Component>> set = shownRewards.object2IntEntrySet();
		for (Entry<Component> entry : set) {
			calculateProfitForItem(getItemId(entry.getKey().getString()), entry.getIntValue());
		}
	}

	/**
	 * <p>Resets the shown rewards and profit to 0 and recalculates rewards for the current profile based on the config filter.</p>
	 * <p>This is also called from the config when the feature is enabled, as the periodic recalculation doesn't happen when the feature is disabled.</p>
	 */
	public void recalculateAll() {
		shownRewards.clear();
		ObjectSet<Entry<String>> set = currentProfileRewards.object2IntEntrySet();
		// The filters are actually item names so that they would look nice and not need a lot of mapping under the screen code
		// Here they are converted to item IDs for comparison
		List<String> filters = SkyblockerConfigManager.get().mining.crystalHollows.powderTrackerFilter.stream().map(INSTANCE::getItemId).toList();
		for (Entry<String> entry : set) {
			if (filters.contains(entry.getKey())) continue;

			if (entry.getKey().equals("GEMSTONE_POWDER")) {
				shownRewards.put(Component.literal("Gemstone Powder").withStyle(ChatFormatting.LIGHT_PURPLE), entry.getIntValue());
			} else {
				ItemStack stack = ItemRepository.getItemStack(entry.getKey());
				if (stack == null) {
					LOGGER.warn("Item stack for id `{}` is null! This might be caused by failed item repository downloads.", entry.getKey());
					continue;
				}
				shownRewards.put(stack.getHoverName(), entry.getIntValue());
			}
		}
		recalculatePrices();
	}

	@Unmodifiable
	public static Object2ObjectMap<String, String> getName2IdMap() {
		return Object2ObjectMaps.unmodifiable(NAME2ID_MAP);
	}

	// TODO: Perhaps make a little something in the skyblocker-assets repo for this in case it needs updating in the future
	static {
		NAME2ID_MAP.put("Gemstone Powder", "GEMSTONE_POWDER"); // Not an actual item, but since we're using IDs for mapping to colored text we need to have this here

		NAME2ID_MAP.put("❤ Rough Ruby Gemstone", "ROUGH_RUBY_GEM");
		NAME2ID_MAP.put("❤ Flawed Ruby Gemstone", "FLAWED_RUBY_GEM");
		NAME2ID_MAP.put("❤ Fine Ruby Gemstone", "FINE_RUBY_GEM");
		NAME2ID_MAP.put("❤ Flawless Ruby Gemstone", "FLAWLESS_RUBY_GEM");

		NAME2ID_MAP.put("❈ Rough Amethyst Gemstone", "ROUGH_AMETHYST_GEM");
		NAME2ID_MAP.put("❈ Flawed Amethyst Gemstone", "FLAWED_AMETHYST_GEM");
		NAME2ID_MAP.put("❈ Fine Amethyst Gemstone", "FINE_AMETHYST_GEM");
		NAME2ID_MAP.put("❈ Flawless Amethyst Gemstone", "FLAWLESS_AMETHYST_GEM");

		NAME2ID_MAP.put("☘ Rough Jade Gemstone", "ROUGH_JADE_GEM");
		NAME2ID_MAP.put("☘ Flawed Jade Gemstone", "FLAWED_JADE_GEM");
		NAME2ID_MAP.put("☘ Fine Jade Gemstone", "FINE_JADE_GEM");
		NAME2ID_MAP.put("☘ Flawless Jade Gemstone", "FLAWLESS_JADE_GEM");

		NAME2ID_MAP.put("⸕ Rough Amber Gemstone", "ROUGH_AMBER_GEM");
		NAME2ID_MAP.put("⸕ Flawed Amber Gemstone", "FLAWED_AMBER_GEM");
		NAME2ID_MAP.put("⸕ Fine Amber Gemstone", "FINE_AMBER_GEM");
		NAME2ID_MAP.put("⸕ Flawless Amber Gemstone", "FLAWLESS_AMBER_GEM");

		NAME2ID_MAP.put("✎ Rough Sapphire Gemstone", "ROUGH_SAPPHIRE_GEM");
		NAME2ID_MAP.put("✎ Flawed Sapphire Gemstone", "FLAWED_SAPPHIRE_GEM");
		NAME2ID_MAP.put("✎ Fine Sapphire Gemstone", "FINE_SAPPHIRE_GEM");
		NAME2ID_MAP.put("✎ Flawless Sapphire Gemstone", "FLAWLESS_SAPPHIRE_GEM");

		NAME2ID_MAP.put("✧ Rough Topaz Gemstone", "ROUGH_TOPAZ_GEM");
		NAME2ID_MAP.put("✧ Flawed Topaz Gemstone", "FLAWED_TOPAZ_GEM");
		NAME2ID_MAP.put("✧ Fine Topaz Gemstone", "FINE_TOPAZ_GEM");
		NAME2ID_MAP.put("✧ Flawless Topaz Gemstone", "FLAWLESS_TOPAZ_GEM");

		NAME2ID_MAP.put("❁ Rough Jasper Gemstone", "ROUGH_JASPER_GEM");
		NAME2ID_MAP.put("❁ Flawed Jasper Gemstone", "FLAWED_JASPER_GEM");
		NAME2ID_MAP.put("❁ Fine Jasper Gemstone", "FINE_JASPER_GEM");
		NAME2ID_MAP.put("❁ Flawless Jasper Gemstone", "FLAWLESS_JASPER_GEM");

		NAME2ID_MAP.put("Pickonimbus 2000", "PICKONIMBUS");
		NAME2ID_MAP.put("Ascension Rope", "ASCENSION_ROPE");
		NAME2ID_MAP.put("Wishing Compass", "WISHING_COMPASS");
		NAME2ID_MAP.put("Gold Essence", "ESSENCE_GOLD");
		NAME2ID_MAP.put("Diamond Essence", "ESSENCE_DIAMOND");
		NAME2ID_MAP.put("Prehistoric Egg", "PREHISTORIC_EGG");
		NAME2ID_MAP.put("Sludge Juice", "SLUDGE_JUICE");
		NAME2ID_MAP.put("Oil Barrel", "OIL_BARREL");
		NAME2ID_MAP.put("Jungle Heart", "JUNGLE_HEART");
		NAME2ID_MAP.put("Treasurite", "TREASURITE");
		NAME2ID_MAP.put("Yoggie", "YOGGIE");

		NAME2ID_MAP.put("Goblin Egg", "GOBLIN_EGG");
		NAME2ID_MAP.put("Green Goblin Egg", "GOBLIN_EGG_GREEN");
		NAME2ID_MAP.put("Blue Goblin Egg", "GOBLIN_EGG_BLUE");
		NAME2ID_MAP.put("Red Goblin Egg", "GOBLIN_EGG_RED");
		NAME2ID_MAP.put("Yellow Goblin Egg", "GOBLIN_EGG_YELLOW");

		NAME2ID_MAP.put("Control Switch", "CONTROL_SWITCH");
		NAME2ID_MAP.put("Electron Transmitter", "ELECTRON_TRANSMITTER");
		NAME2ID_MAP.put("FTX 3070", "FTX_3070");
		NAME2ID_MAP.put("Synthetic Heart", "SYNTHETIC_HEART");
		NAME2ID_MAP.put("Robotron Reflector", "ROBOTRON_REFLECTOR");
		NAME2ID_MAP.put("Superlite Motor", "SUPERLITE_MOTOR");
	}

	private String getItemId(String itemName) {
		return NAME2ID_MAP.getOrDefault(itemName, "");
	}

	public static double getProfit() {
		return INSTANCE.profit;
	}

	public static Object2IntMap<Component> getShownRewards() {
		return INSTANCE.shownRewards;
	}
}
