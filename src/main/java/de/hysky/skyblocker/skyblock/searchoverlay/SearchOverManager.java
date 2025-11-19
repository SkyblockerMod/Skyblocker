package de.hysky.skyblocker.skyblock.searchoverlay;

import com.google.common.collect.Streams;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.museum.Donation;
import de.hysky.skyblocker.skyblock.museum.MuseumItemCache;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.util.NEUId;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SearchOverManager {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Search Overlay");

	private static final String PET_NAME_START = "[Lvl {LVL}] ";

	private static @Nullable SignBlockEntity sign = null;
	private static boolean signFront = true;
	private static boolean isCommand;

	protected static SearchLocation location = SearchLocation.NONE;

	protected static String search = "";
	protected static Boolean maxPetLevel = false;
	protected static int dungeonStars = 0;

	// Use non-final variables and swap them to prevent concurrent modification
	private static HashSet<String> bazaarItems = new HashSet<>();
	private static HashSet<String> auctionItems = new HashSet<>();
	private static HashSet<String> auctionPets = new HashSet<>();
	private static HashSet<String> starableItems = new HashSet<>();
	private static HashMap<String, String> namesToNeuId = new HashMap<>();

	public static String[] suggestionsArray = {};

	/**
	 * uses the skyblock api and Moulberry auction to load a list of items in bazaar and auction house
	 */
	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(SearchOverManager::registerSearchCommands);
		NEURepoManager.runAsyncAfterLoad(SearchOverManager::loadItems);
	}

	private static void registerSearchCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		if (SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.enableCommands) {
			dispatcher.register(literal("ahs").executes(context -> startCommand(true, "")));
			dispatcher.register(literal("bzs").executes(context -> startCommand(false, "")));

			dispatcher.register(literal("ahs").then(argument("item", StringArgumentType.greedyString())
					.executes(context -> startCommand(true, StringArgumentType.getString(context, "item"))
					)));
			dispatcher.register(literal("bzs").then(argument("item", StringArgumentType.greedyString())
					.executes(context -> startCommand(false, StringArgumentType.getString(context, "item"))
					)));
		}

		if (!Debug.debugEnabled()) return;
		dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("debug")).then(literal("reloadSearchOverManager")).executes(ctx -> {
			SearchOverManager.loadItems();
			return Command.SINGLE_SUCCESS;
		}));
	}

	private static int startCommand(boolean isAuction, String itemName) {
		isCommand = true;
		SearchOverManager.location = isAuction ? SearchLocation.AUCTION : SearchLocation.BAZAAR;
		search = "";
		suggestionsArray = new String[]{};
		if (!itemName.isEmpty()) {
			updateSearch(itemName);
		}

		CLIENT.send(() -> CLIENT.setScreen(new OverlayScreen()));
		return Command.SINGLE_SUCCESS;
	}

	private static void loadItems() {
		HashSet<String> bazaarItems = new HashSet<>();
		HashSet<String> auctionItems = new HashSet<>();
		HashSet<String> auctionPets = new HashSet<>();
		HashSet<String> starableItems = new HashSet<>();
		HashMap<String, String> namesToNeuId = new HashMap<>();

		//get bazaar items
		try {
			if (TooltipInfoType.BAZAAR.getData() == null) TooltipInfoType.BAZAAR.run();

			Object2ObjectMap<String, BazaarProduct> products = TooltipInfoType.BAZAAR.getData();
			for (Map.Entry<String, BazaarProduct> entry : products.entrySet()) {
				BazaarProduct product = entry.getValue();
				String id = product.id();

				int sellVolume = product.sellVolume();
				if (sellVolume == 0)
					continue; //do not add items that do not sell e.g. they are not actual in the bazaar

				// Format Enchantments
				if (id.startsWith("ENCHANTMENT_") && ItemRepository.getBazaarStocks().containsKey(id)) {
					String neuId = ItemRepository.getBazaarStocks().get(id);
					NEUItem neuItem = NEURepoManager.getItemByNeuId(neuId);
					if (neuItem == null) continue;

					String name = Formatting.strip(neuItem.getLore().getFirst());
					bazaarItems.add(name);
					namesToNeuId.put(name, neuId);
					continue;
				}

				// Format Shards
				if (id.startsWith("SHARD_") && ItemRepository.getBazaarStocks().containsKey(id)) {
					id = ItemRepository.getBazaarStocks().get(id);
				}

				//look up id for name
				NEUItem neuItem = NEURepoManager.getItemByNeuId(id);
				if (neuItem != null) {
					String name = Formatting.strip(neuItem.getDisplayName());
					bazaarItems.add(name);
					namesToNeuId.put(name, id);
				}
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to load bazaar item list! ", e);
		}

		//get auction items
		try {
			Set<@NEUId String> essenceCosts = NEURepoManager.getConstants().getEssenceCost().getCosts().keySet();
			if (TooltipInfoType.THREE_DAY_AVERAGE.getData() == null) {
				TooltipInfoType.THREE_DAY_AVERAGE.run();
			}
			for (Object2DoubleMap.Entry<String> entry : TooltipInfoType.THREE_DAY_AVERAGE.getData().object2DoubleEntrySet()) {
				String id = entry.getKey();
				//look up in NEU repo.
				id = id.split("[+-]")[0];
				NEUItem neuItem = NEURepoManager.getItemByNeuId(id);
				if (neuItem != null) {
					String name = Formatting.strip(neuItem.getDisplayName());
					//add names that are pets to the list of pets to work with the lvl 100 button
					if (name != null && name.startsWith(PET_NAME_START)) {
						name = name.replace(PET_NAME_START, "");
						auctionPets.add(name.toLowerCase(Locale.ENGLISH));
					}
					//if it has essence cost add to starable items
					if (name != null && essenceCosts.contains(neuItem.getSkyblockItemId())) {
						starableItems.add(name.toLowerCase(Locale.ENGLISH));
					}
					auctionItems.add(name);
					namesToNeuId.put(name, id);
				}
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to load auction house item list! ", e);
		}

		SearchOverManager.bazaarItems = bazaarItems;
		SearchOverManager.auctionItems = auctionItems;
		SearchOverManager.auctionPets = auctionPets;
		SearchOverManager.starableItems = starableItems;
		SearchOverManager.namesToNeuId = namesToNeuId;
	}

	/**
	 * Receives data when a search is started and resets values
	 *
	 * @param sign  the sign that is being edited
	 * @param front if it's the front of the sign
	 */
	public static void updateSign(@NotNull SignBlockEntity sign, boolean front, SearchLocation location) {
		signFront = front;
		SearchOverManager.sign = sign;
		isCommand = false;
		SearchOverManager.location = location;
		if (SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.keepPreviousSearches) {
			Text[] messages = SearchOverManager.sign.getText(signFront).getMessages(CLIENT.shouldFilterText());
			search = messages[0].getString();
			if (!messages[1].getString().isEmpty()) {
				if (!search.endsWith(" ")) {
					search += " ";
				}
				search += messages[1].getString();
			}
		} else {
			search = "";
		}
		suggestionsArray = new String[]{};
	}

	/**
	 * Updates the search value and the suggestions based on that value.
	 *
	 * @param newValue new search value
	 */
	@SuppressWarnings("incomplete-switch")
	protected static void updateSearch(String newValue) {
		search = newValue;
		//update the suggestion values
		int totalSuggestions = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.maxSuggestions;
		if (newValue.isBlank() || totalSuggestions == 0) return; //do not search for empty value

		switch (location) {
			case SearchLocation.AUCTION -> suggestionsArray = updateSuggestions(auctionItems, totalSuggestions);
			case SearchLocation.BAZAAR -> suggestionsArray = updateSuggestions(bazaarItems, totalSuggestions);
			case SearchLocation.MUSEUM -> suggestionsArray = updateSuggestions(getMuseumSuggestions(), totalSuggestions);
		}
	}

	private static Set<String> getMuseumSuggestions() {
		// Since Armor Set IDs are not valid items, we have to split up the processing.
		Map<Boolean, List<Donation>> types = MuseumItemCache.MUSEUM_DONATIONS.stream().collect(Collectors.partitioningBy(Donation::isSet));

		// Find the item in the ItemRepository and get the name.
		Stream<String> items = types.get(false).stream()
				.map(Donation::getId)
				.filter(MuseumItemCache::hasItemInMuseum)
				.map(ItemRepository::getItemStack)
				.filter(Objects::nonNull)
				.map(ItemStack::getName)
				.map(Text::getString);

		// Get armor set name
		Stream<String> sets = types.get(true).stream()
				.map(Donation::getId)
				.filter(MuseumItemCache::hasItemInMuseum)
				.map(MuseumItemCache.ARMOR_NAMES::get);

		return Streams.concat(items, sets).collect(Collectors.toSet());
	}

	protected static String[] updateSuggestions(Set<String> items, int totalSuggestions) {
		return items.stream().sorted(Comparator.comparing(SearchOverManager::shouldFrontLoad, Comparator.reverseOrder())).filter(item -> item.toLowerCase(Locale.ENGLISH).contains(search.toLowerCase(Locale.ENGLISH))).limit(totalSuggestions).toArray(String[]::new);
	}

	/**
	 * determines if a value should be moved to the front of the search
	 *
	 * @param name name of the suggested item
	 * @return if the value should be at the front of the search queue
	 */
	private static boolean shouldFrontLoad(String name) {
		if (location != SearchLocation.AUCTION) return false;

		//do nothing to non pets
		if (!auctionPets.contains(name.toLowerCase(Locale.ENGLISH))) {
			return false;
		}
		//only front load pets when there is enough of the pet typed, so it does not spoil searching for other items
		return (double) search.length() / name.length() > 0.5;
	}

	/**
	 * Gets the suggestion in the suggestion array at the index
	 *
	 * @param index index of suggestion
	 */
	protected static String getSuggestion(int index) {
		if (suggestionsArray.length > index && suggestionsArray[index] != null) {
			return suggestionsArray[index];
		} else { //there are no suggestions yet
			return "";
		}
	}

	protected static String getSuggestionId(int index) {
		String name = getSuggestion(index);
		return getItemId(name);
	}

	private static String getItemId(String name) {
		if (location != SearchLocation.MUSEUM || !MuseumItemCache.ARMOR_NAMES.containsValue(name)) return namesToNeuId.get(name);

		// Handle Id for Armor/Equipment Donation Sets
		String armorId = "";

		for (Map.Entry<String, String> pair : MuseumItemCache.ARMOR_NAMES.entrySet()) {
			if (pair.getValue().equals(name)) {
				armorId = pair.getKey();
				break;
			}
		}

		return MuseumItemCache.ARMOR_TO_ID.getOrDefault(armorId, namesToNeuId.get(name));
	}

	/**
	 * Gets the item name in the history array at the index
	 *
	 * @param index index of suggestion
	 */
	protected static String getHistory(int index) {
		UIAndVisualsConfig.SearchOverlay config = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay;
		List<String> history;
		switch (location) {
			case AUCTION -> history = config.auctionHistory;
			case BAZAAR -> history = config.bazaarHistory;
			case MUSEUM -> history = config.museumHistory;
			default -> {
				return null;
			}
		}

		if (history.size() > index) return history.get(index);
		return null;
	}

	protected static String getHistoryId(int index) {
		return getItemId(getHistory(index));
	}

	protected static void removeHistoryItem(int index) {
		UIAndVisualsConfig.SearchOverlay config = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay;
		List<String> history;

		switch (location) {
			case AUCTION -> history = config.auctionHistory;
			case BAZAAR -> history = config.bazaarHistory;
			case MUSEUM -> history = config.museumHistory;
			default -> {
				return;
			}
		}

		if (history.size() > index) {
			history.remove(index);
		}
	}

	private static List<String> addToHistory(List<String> history, String search, int historyLength) {
		if (history.isEmpty() || !history.contains(search)) {
			// Add new search to history
			history.addFirst(search);
			if (history.size() > historyLength) {
				history = history.subList(0, historyLength);
			}
		} else {
			// Move existing search to the top of the history list
			history.remove(search);
			history.addFirst(search);
		}
		return history;
	}

	/**
	 * Add the current search value to the start of the history list and truncate to the max history value and save this to the config
	 */
	@SuppressWarnings("incomplete-switch")
	private static void saveHistory() {
		//save to history
		UIAndVisualsConfig.SearchOverlay config = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay;
		switch (location) {
			case AUCTION -> config.auctionHistory = addToHistory(config.auctionHistory, search, config.historyLength);
			case BAZAAR -> config.bazaarHistory = addToHistory(config.bazaarHistory, search, config.historyLength);
			case MUSEUM -> config.museumHistory = addToHistory(config.museumHistory, search, config.historyLength);
		}
		SkyblockerConfigManager.update(Consumers.nop());
	}

	/**
	 * Saves the current value of ({@link SearchOverManager#search}) then pushes it to a command or sign depending on how the gui was opened
	 */
	protected static void pushSearch() {
		//save to history
		if (!search.isEmpty()) {
			saveHistory();
		}
		//add pet level or dungeon starts if in ah
		if (location == SearchLocation.AUCTION) {
			addExtras();
		}
		// Fix Bazaar bug - Search doesn't work if input from sign contains "null" (blocks null ovoid, etc.)
		if (location == SearchLocation.BAZAAR && !isCommand && search.toLowerCase(Locale.ENGLISH).contains("null")) {
			search = "\"%s\"".formatted(search);
		}
		//push
		if (isCommand) {
			pushCommand();
		} else {
			pushSign();
		}
	}

	/**
	 * Adds pet level 100 or necessary dungeon starts if needed
	 */
	private static void addExtras() {
		// pet level
		if (maxPetLevel) {
			if (auctionPets.contains(search.toLowerCase(Locale.ENGLISH))) {
				if (search.equalsIgnoreCase("golden dragon") || search.equalsIgnoreCase("jade dragon")) {
					search = "[Lvl 200] " + search;
				} else {
					search = "[Lvl 100] " + search;
				}
			}
		} else {
			// still filter for only pets
			if (auctionPets.contains(search.toLowerCase(Locale.ENGLISH))) {
				// add bracket so only get pets
				search = "] " + search;
			}
		}

		// dungeon stars
		// check if it's a dungeon item and if so add correct stars
		if (dungeonStars > 0 && starableItems.contains(search.toLowerCase(Locale.ENGLISH))) {
			StringBuilder starString = new StringBuilder(" ");
			//add stars up to 5
			starString.append("✪".repeat(Math.max(0, Math.min(dungeonStars, 5))));
			//add number for other stars
			switch (dungeonStars) {
				case 6 -> starString.append("➊");
				case 7 -> starString.append("➋");
				case 8 -> starString.append("➌");
				case 9 -> starString.append("➍");
				case 10 -> starString.append("➎");
			}
			search += starString.toString();
		}
	}

	/**
	 * runs the command to search for the value in ({@link SearchOverManager#search})
	 */
	private static void pushCommand() {
		if (search.isEmpty()) return;
		String command;
		switch (location) {
			case AUCTION -> command = "/ahSearch " + search;
			case BAZAAR -> command = "/bz " + search;
			default -> {
				return;
			}
		}
		MessageScheduler.INSTANCE.sendMessageAfterCooldown(command, true);
	}

	/**
	 * pushes the ({@link SearchOverManager#search}) to the sign. It needs to split it over two lines without splitting a word
	 */
	private static void pushSign() {
		//splits text into 2 lines max = 30 chars
		Pair<String, String> split = splitString(search);

		// send packet to update sign
		if (CLIENT.player != null && sign != null) {
			Text[] messages = sign.getText(signFront).getMessages(CLIENT.shouldFilterText());
			CLIENT.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), signFront,
					split.left(),
					split.right(),
					messages[2].getString(),
					messages[3].getString()
			));
		}
	}

	public static Pair<String, String> splitString(String s) {
		if (s.length() <= 15) {
			return Pair.of(s, "");
		}
		int index = s.lastIndexOf(' ', 15);
		if (index == -1) {
			return Pair.of(s.substring(0, 15), "");
		}
		return Pair.of(s.substring(0, index), s.substring(index + 1, Math.min(index + 16, s.length())).trim());
	}

	public enum SearchLocation {
		NONE,
		AUCTION,
		BAZAAR,
		MUSEUM
	}
}
