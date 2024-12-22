package de.hysky.skyblocker.skyblock.museum;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.*;
import de.hysky.skyblocker.utils.Http.ApiResponse;
import io.github.moulberry.repo.NEURepoFile;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MuseumItemCache {
	private static final Logger LOGGER = LoggerFactory.getLogger(MuseumItemCache.class);
	private static final String ERROR_LOG_TEMPLATE = "[Skyblocker] Failed to refresh museum item data for profile {}";
	private static final int CONFIRM_DONATION_BUTTON_SLOT = 20;
	private static final String CONSTANTS_MUSEUM_DATA = "constants/museum.json";
	private static final Path CACHE_FILE = SkyblockerMod.CONFIG_DIR.resolve("museum_item_cache.json");
	private static final Map<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>> MUSEUM_ITEM_CACHE = new Object2ObjectOpenHashMap<>();
	public static final String DONATION_CONFIRMATION_SCREEN_TITLE = "Confirm Donation";
	public static final Map<String, String> ARMOR_NAMES = new Object2ObjectArrayMap<>();
	public static final Map<String, String> MAPPED_IDS = new Object2ObjectArrayMap<>();
	public static final ObjectArrayList<Donation> MUSEUM_DONATIONS = new ObjectArrayList<>();
	public static final ObjectArrayList<ObjectArrayList<String>> ORDERED_UPGRADES = new ObjectArrayList<>();
	private static CompletableFuture<Void> loaded;

	@Init
	public static void init() {
		loadMuseumItems();
		ClientLifecycleEvents.CLIENT_STARTED.register(MuseumItemCache::load);
		ClientCommandRegistrationCallback.EVENT.register(MuseumItemCache::registerCommands);
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("museum")
						.then(literal("resync")
								.executes(context -> {
									if (tryResync(context.getSource())) {
										context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.museum.attemptingResync")));
									} else {
										context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.museum.cannotResync")));
									}

									return Command.SINGLE_SUCCESS;
								}))));
	}

	private static void load(MinecraftClient client) {
		loaded = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(CACHE_FILE)) {
				Map<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>> cachedData = ProfileMuseumData.SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();

				MUSEUM_ITEM_CACHE.putAll(cachedData);
				LOGGER.info("[Skyblocker] Loaded museum items cache");
			} catch (NoSuchFileException ignored) {
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] Failed to load cached museum items", e);
			}
		});
	}

	private static void save() {
		CompletableFuture.runAsync(() -> {
			try (BufferedWriter writer = Files.newBufferedWriter(CACHE_FILE)) {
				SkyblockerMod.GSON.toJson(ProfileMuseumData.SERIALIZATION_CODEC.encodeStart(JsonOps.INSTANCE, MUSEUM_ITEM_CACHE).getOrThrow(), writer);
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] Failed to save cached museum items!", e);
			}
		});
	}

	/**
	 * Loads museum data from local repo.
	 */
	public static void loadMuseumItems() {
		NEURepoManager.runAsyncAfterLoad(() -> {
			NEURepoFile filePath = NEURepoManager.NEU_REPO.file(CONSTANTS_MUSEUM_DATA);
			if (filePath == null) return;
			try (BufferedReader reader = Files.newBufferedReader(filePath.getFsPath())) {
				// Parse the JSON file
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

				Map<String, JsonElement> setExceptions = json.get("set_exceptions").getAsJsonObject().asMap();
				Map<String, JsonElement> mappedIds = json.get("mapped_ids").getAsJsonObject().asMap();
				Map<String, JsonElement> itemToXp = json.get("itemToXp").getAsJsonObject().asMap();
				Map<String, JsonElement> setsToItems = json.get("sets_to_items").getAsJsonObject().asMap();
				Map<String, JsonElement> children = json.get("children").getAsJsonObject().asMap();

				Map<String, JsonArray> allDonations = Map.of(
						"weapons", json.get("weapons").getAsJsonArray(),
						"armor", json.get("armor").getAsJsonArray(),
						"rarities", json.get("rarities").getAsJsonArray()
				);

				mappedIds.forEach((s, jsonElement) -> MAPPED_IDS.put(s, jsonElement.getAsString()));

				for (Map.Entry<String, JsonArray> entry : allDonations.entrySet()) {
					String category = entry.getKey();
					JsonArray array = entry.getValue();

					for (JsonElement element : array) {
						String itemID = element.getAsString();
						List<ObjectObjectMutablePair<String, PriceData>> set = new ArrayList<>();
						if (category.equals("armor")) {
							boolean isEquipment = true;
							for (JsonElement jsonElement : setsToItems.get(itemID).getAsJsonArray()) {
								if (isEquipment) isEquipment = ItemUtils.isEquipment(jsonElement.getAsString());
								set.add(new ObjectObjectMutablePair<>(jsonElement.getAsString(), null));
							}
							String realId = itemID;
							for (Map.Entry<String, JsonElement> exception : setExceptions.entrySet()) {
								if (exception.getValue().getAsString().equals(itemID)) {
									realId = exception.getKey();
									break;
								}
							}
							ARMOR_NAMES.put(itemID, MuseumUtils.formatArmorName(realId, isEquipment));
						}
						int itemXP = itemToXp.get(itemID).getAsInt();
						List<String> upgrades = getUpgrades(children, itemID);

						if (!upgrades.isEmpty()) {
							// Try to find an existing upgrade list that either contains itemID or overlaps with upgrades
							Optional<ObjectArrayList<String>> matchingUpgrade = ORDERED_UPGRADES.stream()
									.filter(orderedUpgrade ->
											orderedUpgrade.contains(itemID) ||
													!Collections.disjoint(orderedUpgrade, upgrades))
									.findFirst();

							if (matchingUpgrade.isPresent()) {
								List<String> orderedUpgrade = matchingUpgrade.get();
								// If the matching list has fewer or equal items, replace it with the new upgrade list
								if (orderedUpgrade.size() <= upgrades.size()) {
									orderedUpgrade.clear();
									orderedUpgrade.add(itemID);
									orderedUpgrade.addAll(upgrades);
								}
							} else {
								// If no match, add a new upgrade list with itemID and upgrades
								ObjectArrayList<String> newUpgrade = new ObjectArrayList<>();
								newUpgrade.add(itemID);
								newUpgrade.addAll(upgrades);
								ORDERED_UPGRADES.add(newUpgrade);
							}
						}

						MUSEUM_DONATIONS.add(new Donation(category, itemID, set, itemXP));
					}
				}

				MUSEUM_DONATIONS.forEach(donation -> {
					for (List<String> list : ORDERED_UPGRADES) {
						int armorIndex = list.indexOf(donation.getId());
						if (armorIndex > 0) {
							for (int i = armorIndex - 1; i >= 0; i--) {
								donation.addDowngrade(list.get(i));
							}
						}
					}
				});

				LOGGER.info("[Skyblocker] Loaded museum data");
			} catch (NoSuchFileException ignored) {
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] Failed to load donations data", e);
			}
		});
	}

	/**
	 * Gets a list of all upgrades for a given item.
	 */
	public static List<String> getUpgrades(Map<String, JsonElement> children, String item) {
		List<String> upgrades = new ArrayList<>();
		String currentItem = item;

		while (true) {
			// Find the parent item
			String finalCurrentItem = currentItem;
			String parentItem = children.entrySet().stream()
					.filter(e -> e.getValue().getAsString().equals(finalCurrentItem))
					.map(Map.Entry::getKey)
					.findFirst()
					.orElse(null);

			if (parentItem == null) {
				break; // No more parents found, exit the loop
			}

			upgrades.add(parentItem); // Add the parent to the upgrades list
			currentItem = parentItem; // Move to the parent and repeat
		}

		return upgrades;
	}

	/**
	 * Retrieves a list of donations that the player has not yet contributed.
	 */
	public static List<Donation> getDonations() {
		List<Donation> uncontributedItems = new ArrayList<>();

		String uuid = Utils.getUndashedUuid();
		String profileId = Utils.getProfileId();

		if (MUSEUM_ITEM_CACHE.containsKey(uuid) && MUSEUM_ITEM_CACHE.get(uuid).containsKey(profileId)) {
			ObjectOpenHashSet<String> items = MUSEUM_ITEM_CACHE.get(uuid).get(profileId).collectedItemIds();
			for (Donation donation : MUSEUM_DONATIONS) {
				// Check if the donation id or his upgrades is not present in the collected items
				if (!items.contains(donation.getId())) {
					if (donation.isSet() && items.stream().anyMatch(i -> donation.getSet().stream().anyMatch(p -> p.left().equals(i)))) continue;
					donation.setPriceData();
					uncontributedItems.add(donation);
				}
			}

			// Check if the item has a donated downgrade
			uncontributedItems.forEach(donation -> donation.setDiscount(donation.getDowngrades().stream()
					.filter(downgrade -> donation.isCraftable())
					.filter(downgrade -> uncontributedItems.stream().noneMatch(item -> item.getId().equals(downgrade)))
					.map(downgrade -> ObjectDoublePair.of(downgrade, MuseumUtils.getSetCraftCost(downgrade)))
					.findFirst()
					.orElse(null)));

			uncontributedItems.sort(Comparator.comparing(Donation::getId)); //Sorting alphabetically
		}
		return uncontributedItems;
	}

	public static void handleClick(Slot ignored, int slotId, DefaultedList<Slot> slots) {
		if (slotId == CONFIRM_DONATION_BUTTON_SLOT) {
			//Slots 0 to 17 can have items, well not all but thats the general range
			for (int i = 0; i < 17; i++) {
				ItemStack stack = slots.get(i).getStack();

				if (!stack.isEmpty()) {
					String itemId = ItemUtils.getItemId(stack);
					String profileId = Utils.getProfileId();

					if (!itemId.isEmpty() && !profileId.isEmpty()) {
						if (MAPPED_IDS.containsKey(itemId)) itemId = MAPPED_IDS.get(itemId);
						String setId = MuseumUtils.getSetID(itemId);
						Donation donation = MuseumManager.getDonation(setId != null ? setId : itemId);
						String uuid = Utils.getUndashedUuid();
						//Be safe about access to avoid NPEs
						Map<String, ProfileMuseumData> playerData = MUSEUM_ITEM_CACHE.computeIfAbsent(uuid, _uuid -> new Object2ObjectOpenHashMap<>());
						playerData.putIfAbsent(profileId, ProfileMuseumData.EMPTY.get());

						playerData.get(profileId).collectedItemIds().add(itemId);
						if (setId != null) playerData.get(profileId).collectedItemIds().add(setId);
						if (donation != null && !donation.getDowngrades().isEmpty()) {
							for (String downgrade : donation.getDowngrades()) {
								if (donation.isSet()) {
									List<String> pieces = MuseumUtils.getPiecesBySetID(downgrade);
									playerData.get(profileId).collectedItemIds().addAll(pieces);
								}
								playerData.get(profileId).collectedItemIds().add(downgrade);
							}
						}
					}
				}
			}
			save();
		}
	}

	private static void updateData4ProfileMember(String uuid, String profileId) {
		updateData4ProfileMember(uuid, profileId, null);
	}

	private static void updateData4ProfileMember(String uuid, String profileId, FabricClientCommandSource source) {
		CompletableFuture.runAsync(() -> {
			try (ApiResponse response = Http.sendHypixelRequest("skyblock/museum", "?profile=" + profileId)) {
				//The request was successful
				if (response.ok()) {
					JsonObject profileData = JsonParser.parseString(response.content()).getAsJsonObject();
					JsonObject members = profileData.getAsJsonObject("members");

					if (members.has(uuid)) {
						JsonObject memberData = members.get(uuid).getAsJsonObject();
						//We call them sets because it could either be a singular item or an entire armour set
						Map<String, JsonElement> donatedSets = memberData.get("items").getAsJsonObject().asMap();
						//Set of all found item ids on profile
						ObjectOpenHashSet<String> itemIds = new ObjectOpenHashSet<>();

						donatedSets.forEach((s, __) -> {
							Optional<Donation> donation = MUSEUM_DONATIONS.stream().filter(d -> d.getId().equals(s)).findFirst();
							donation.ifPresent(value -> itemIds.addAll(value.getDowngrades()));
							if (donation.isPresent()) {
								if (donation.get().isSet()) {
									itemIds.addAll(donation.get().getSet().stream().map(ObjectObjectMutablePair::left).toList());
									donation.get().getDowngrades().forEach(downgrade -> itemIds.addAll(MuseumUtils.getPiecesBySetID(downgrade)));
								} else {
									itemIds.add(donation.get().getId().replace("STARRED_", ""));
									itemIds.addAll(donation.get().getDowngrades());
								}
							}
						});

						MUSEUM_ITEM_CACHE.get(uuid).put(profileId, new ProfileMuseumData(System.currentTimeMillis(), itemIds));
						save();

						if (source != null) source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.museum.resyncSuccess")));

						LOGGER.info("[Skyblocker] Successfully updated museum item cache for profile {}", profileId);
					} else {
						//If the player's Museum API is disabled
						putEmpty(uuid, profileId);

						if (source != null) source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.museum.resyncFailure")));

						LOGGER.warn(ERROR_LOG_TEMPLATE + " because the Museum API is disabled!", profileId);
					}
				} else {
					//If the request returns a non 200 status code
					putEmpty(uuid, profileId);

					if (source != null) source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.museum.resyncFailure")));

					LOGGER.error(ERROR_LOG_TEMPLATE + " because a non 200 status code was encountered! Status Code: {}", profileId, response.statusCode());
				}
			} catch (Exception e) {
				//If an exception was somehow thrown
				putEmpty(uuid, profileId);

				if (source != null) source.sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.museum.resyncFailure")));

				LOGGER.error(ERROR_LOG_TEMPLATE, profileId, e);
			}
		});
	}

	private static void putEmpty(String uuid, String profileId) {
		//Only put new data if they didn't have any before
		if (!MUSEUM_ITEM_CACHE.get(uuid).containsKey(profileId)) {
			MUSEUM_ITEM_CACHE.get(uuid).put(profileId, new ProfileMuseumData(System.currentTimeMillis(), ObjectOpenHashSet.of()));
		}

		save();
	}

	private static boolean tryResync(FabricClientCommandSource source) {
		String uuid = Utils.getUndashedUuid();
		String profileId = Utils.getProfileId();

		//Only allow resyncing if the data is actually present yet, otherwise the player needs to swap servers for the tick method to be called
		if (loaded.isDone() && !profileId.isEmpty() && MUSEUM_ITEM_CACHE.containsKey(uuid) && MUSEUM_ITEM_CACHE.get(uuid).containsKey(profileId) && MUSEUM_ITEM_CACHE.get(uuid).get(profileId).canResync()) {
			updateData4ProfileMember(uuid, profileId, source);

			return true;
		}

		return false;
	}

	/**
	 * The cache is ticked upon switching Skyblock servers. Only loads from the API if the profile wasn't cached yet.
	 */
	public static void tick(String profileId) {
		String uuid = Utils.getUndashedUuid();

		if (loaded.isDone() && (!MUSEUM_ITEM_CACHE.containsKey(uuid) || !MUSEUM_ITEM_CACHE.getOrDefault(uuid, new Object2ObjectOpenHashMap<>()).containsKey(profileId))) {
			Map<String, ProfileMuseumData> playerData = MUSEUM_ITEM_CACHE.computeIfAbsent(uuid, _uuid -> new Object2ObjectOpenHashMap<>());
			playerData.putIfAbsent(profileId, ProfileMuseumData.EMPTY.get());

			updateData4ProfileMember(uuid, profileId);
		}
	}

	public static boolean hasItemInMuseum(String id) {
		id = id.replace("STARRED_", "");
		String uuid = Utils.getUndashedUuid();
		ObjectOpenHashSet<String> collectedItemIds = (!MUSEUM_ITEM_CACHE.containsKey(uuid) || Utils.getProfileId().isBlank() || !MUSEUM_ITEM_CACHE.get(uuid).containsKey(Utils.getProfileId())) ? null : MUSEUM_ITEM_CACHE.get(uuid).get(Utils.getProfileId()).collectedItemIds();

		return collectedItemIds != null && collectedItemIds.contains(id);
	}

	private record ProfileMuseumData(long lastResync, ObjectOpenHashSet<String> collectedItemIds) {
		private static final Supplier<ProfileMuseumData> EMPTY = () -> new ProfileMuseumData(0L, new ObjectOpenHashSet<>());
		private static final long TIME_BETWEEN_RESYNCING_ALLOWED = 600_000L;
		private static final Codec<ProfileMuseumData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.LONG.fieldOf("lastResync").forGetter(ProfileMuseumData::lastResync),
				Codec.STRING.listOf()
						.xmap(ObjectOpenHashSet::new, ObjectArrayList::new)
						.fieldOf("collectedItemIds")
						.forGetter(i -> new ObjectOpenHashSet<>(i.collectedItemIds()))
		).apply(instance, ProfileMuseumData::new));
		//Mojang's internal Codec implementation uses ImmutableMaps so we'll just xmap those away and type safety while we're at it :')
		private static final Codec<Map<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>>> SERIALIZATION_CODEC = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, CODEC)
				.xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new)
		).xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new);

		private boolean canResync() {
			return this.lastResync + TIME_BETWEEN_RESYNCING_ALLOWED < System.currentTimeMillis();
		}
	}
}
