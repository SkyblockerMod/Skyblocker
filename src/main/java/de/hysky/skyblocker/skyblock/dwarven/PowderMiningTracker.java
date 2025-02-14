package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ChatEvents;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.events.ItemPriceUpdateEvent;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.profile.ProfiledData;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class PowderMiningTracker {
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Powder Mining Tracker");
	private static final Pattern GEMSTONE_SYMBOLS = Pattern.compile("[α☘☠✎✧❁❂❈❤⸕] ");
	private static final Pattern REWARD_PATTERN = Pattern.compile(" {4}(.*?) ?x?([\\d,]*)");
	private static final Codec<Object2IntMap<String>> REWARDS_CODEC = CodecUtils.object2IntMapCodec(Codec.STRING);
	private static final Object2ObjectArrayMap<String, String> NAME2ID_MAP = new Object2ObjectArrayMap<>(50);

	// This constructor takes in a comparator that is triggered to decide where to add the element in the tree map
	// This causes it to be sorted at all times. This is for rendering them in a sort of easy-to-read manner.
	private static final Object2IntAVLTreeMap<Text> SHOWN_REWARDS = new Object2IntAVLTreeMap<>(Comparator.<Text>comparingInt(text -> comparePriority(text.getString())).thenComparing(Text::getString));

	/**
	 * Holds the total reward maps for all accounts and profiles. {@link #currentProfileRewards} is a subset of this map, updated on profile change.
	 */
	private static final ProfiledData<Object2IntMap<String>> ALL_REWARDS = new ProfiledData<>(getRewardFilePath(), REWARDS_CODEC);

	/**
	 * <p>
	 * Holds the total amount of each reward obtained for the current profile.
	 * If any items are filtered out, they are still added to this map but not to the {@link #SHOWN_REWARDS} map.
	 * Once the filter is changed, the {@link #SHOWN_REWARDS} map is cleared and recalculated based on this map.
	 * </p>
	 * <p>This is similar to how {@link ChatHud#messages} and {@link ChatHud#visibleMessages} behave.</p>
	 *
	 * @implNote This is a map of item IDs to the amount of that item obtained.
	 */
	@SuppressWarnings("JavadocReference")
	private static Object2IntMap<String> currentProfileRewards = new Object2IntOpenHashMap<>();
	private static boolean insideChestMessage = false;
	private static double profit = 0;

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean isEnabled() {
		return SkyblockerConfigManager.get().mining.crystalHollows.enablePowderTracker;
	}

	@Init
	public static void init() {
		ChatEvents.RECEIVE_STRING.register(PowderMiningTracker::onChatMessage);
		HudRenderEvents.AFTER_MAIN_HUD.register(PowderMiningTracker::render);

		ItemPriceUpdateEvent.ON_PRICE_UPDATE.register(() -> {
			if (isEnabled()) recalculatePrices();
		});

		ALL_REWARDS.init();

		SkyblockEvents.PROFILE_CHANGE.register(PowderMiningTracker::onProfileChange);
		SkyblockEvents.PROFILE_INIT.register(PowderMiningTracker::onProfileInit);

		//TODO: Sort out proper commands for this
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				literal(SkyblockerMod.NAMESPACE)
						.then(
								literal("clearrewards")
										.executes(context -> {
											SHOWN_REWARDS.clear();
											currentProfileRewards.clear();
											profit = 0;
											return 1;
										})
						)
						.then(
								literal("listrewards")
										.executes(context -> {
											var set = SHOWN_REWARDS.object2IntEntrySet();
											for (Object2IntMap.Entry<Text> entry : set) {
												MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(entry.getKey().copy().append(" ").append(Text.of(String.valueOf(entry.getIntValue()))));
											}
											return 1;
										})
						)
		));
	}

	private static void onChatMessage(String text) {
		if (Utils.getLocation() != Location.CRYSTAL_HOLLOWS || !isEnabled()) return;
		// Reward messages end with a separator like so
		if (insideChestMessage && text.equals("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) {
			insideChestMessage = false;
			return;
		}

		if (!insideChestMessage && (text.equals("  CHEST LOCKPICKED ") || (SkyblockerConfigManager.get().mining.crystalHollows.countNaturalChestsInTracker && text.equals("  LOOT CHEST COLLECTED ")))) {
			insideChestMessage = true;
			return;
		}

		if (!insideChestMessage) return;
		Matcher matcher = REWARD_PATTERN.matcher(text);
		if (!matcher.matches()) return;
		String itemName = matcher.group(1);
		int amount = NumberUtils.toInt(matcher.group(2).replace(",", ""), 1);

		String itemId = getItemId(itemName);
		if (itemId.isEmpty()) {
			LOGGER.error("No matching item id for name `{}`. Report this!", itemName);
			return;
		}
		incrementReward(itemName, itemId, amount);
		calculateProfitForItem(itemId, amount);
	}

	private static void onProfileChange(String prevProfileId, String newProfileId) {
		onProfileInit(newProfileId);
	}

	private static void onProfileInit(String profileId) {
		if (!isEnabled()) return;
		currentProfileRewards = ALL_REWARDS.computeIfAbsent(Object2IntArrayMap::new);
		recalculateAll();
	}

	private static void incrementReward(String itemName, String itemId, int amount) {
		currentProfileRewards.mergeInt(itemId, amount, Integer::sum);
		if (SkyblockerConfigManager.get().mining.crystalHollows.powderTrackerFilter.contains(itemName)) return;
		if (itemId.equals("GEMSTONE_POWDER")) {
			SHOWN_REWARDS.merge(Text.literal("Gemstone Powder").formatted(Formatting.LIGHT_PURPLE), amount, Integer::sum);
		} else {
			ItemStack stack = ItemRepository.getItemStack(itemId);
			if (stack == null) {
				LOGGER.warn("Item stack for id `{}` is null! This might be caused by failed item repository downloads.", itemId);
				return;
			}
			SHOWN_REWARDS.merge(stack.getName(), amount, Integer::sum);
		}
	}

	private static int comparePriority(String string) {
		string = GEMSTONE_SYMBOLS.matcher(string).replaceAll(""); // Removes the gemstone symbol from the string to make it easier to compare
		// Puts gemstone powder at the top of the list, then gold and diamond essence, then gemstones by ascending rarity and then whatever else.
		return switch (string) {
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

	/**
	 * Normally, the price is calculated on a per-reward basis as they are obtained. This is what this method does.
	 */
	private static void calculateProfitForItem(String itemId, int amount) {
		DoubleBooleanPair price = ItemUtils.getItemPrice(itemId);
		if (price.rightBoolean()) profit += price.leftDouble() * amount;
	}

	/**
	 * When the bz/ah prices are updated, this method recalculates the profit for all rewards at once.
	 */
	private static void recalculatePrices() {
		profit = 0;
		ObjectSortedSet<Object2IntMap.Entry<Text>> set = SHOWN_REWARDS.object2IntEntrySet();
		for (Object2IntMap.Entry<Text> entry : set) {
			calculateProfitForItem(getItemId(entry.getKey().getString()), entry.getIntValue());
		}
	}

	/**
	 * Resets the shown rewards and profit to 0 and recalculates rewards for the current profile based on the config filter.
	 */
	public static void recalculateAll() {
		SHOWN_REWARDS.clear();
		ObjectSet<Object2IntMap.Entry<String>> set = currentProfileRewards.object2IntEntrySet();
		// The filters are actually item names so that they would look nice and not need a lot of mapping under the screen code
		// Here they are converted to item IDs for comparison
		List<String> filters = SkyblockerConfigManager.get().mining.crystalHollows.powderTrackerFilter.stream().map(PowderMiningTracker::getItemId).toList();
		for (Object2IntMap.Entry<String> entry : set) {
			if (filters.contains(entry.getKey())) continue;

			if (entry.getKey().equals("GEMSTONE_POWDER")) {
				SHOWN_REWARDS.put(Text.literal("Gemstone Powder").formatted(Formatting.LIGHT_PURPLE), entry.getIntValue());
			} else {
				ItemStack stack = ItemRepository.getItemStack(entry.getKey());
				if (stack == null) {
					LOGGER.warn("Item stack for id `{}` is null! This might be caused by failed item repository downloads.", entry.getKey());
					continue;
				}
				SHOWN_REWARDS.put(stack.getName(), entry.getIntValue());
			}
		}
		recalculatePrices();
	}

	@Unmodifiable
	public static Object2ObjectMap<String, String> getName2IdMap() {
		return Object2ObjectMaps.unmodifiable(NAME2ID_MAP);
	}

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

	@NotNull
	private static String getItemId(String itemName) {
		return NAME2ID_MAP.getOrDefault(itemName, "");
	}

	private static Path getRewardFilePath() {
		return SkyblockerMod.CONFIG_DIR.resolve("reward-trackers/powder-mining.json");
	}

	private static void render(DrawContext context, RenderTickCounter tickCounter) {
		if (Utils.getLocation() != Location.CRYSTAL_HOLLOWS || !isEnabled()) return;
		int y = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 - 100;
		var set = SHOWN_REWARDS.object2IntEntrySet();
		for (Object2IntMap.Entry<Text> entry : set) {
			context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, entry.getKey(), 5, y, 0xFFFFFF);
			context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of(NumberFormat.getInstance().format(entry.getIntValue())), 10 + MinecraftClient.getInstance().textRenderer.getWidth(entry.getKey()), y, 0xFFFFFF);
			y += 10;
		}
		if (!set.isEmpty()) context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Gain: " + NumberFormat.getInstance().format(profit) + " coins").formatted(Formatting.GOLD), 5, y + 10, 0xFFFFFF);
	}
}
