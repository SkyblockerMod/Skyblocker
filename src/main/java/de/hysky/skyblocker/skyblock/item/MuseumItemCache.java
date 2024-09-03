package de.hysky.skyblocker.skyblock.item;

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
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Http.ApiResponse;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MuseumItemCache {
	private static final Logger LOGGER = LoggerFactory.getLogger(MuseumItemCache.class);
	private static final Path CACHE_FILE = SkyblockerMod.CONFIG_DIR.resolve("museum_item_cache.json");
	private static final Map<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>> MUSEUM_ITEM_CACHE = new Object2ObjectOpenHashMap<>();
	private static final String ERROR_LOG_TEMPLATE = "[Skyblocker] Failed to refresh museum item data for profile {}";
	public static final String DONATION_CONFIRMATION_SCREEN_TITLE = "Confirm Donation";
	private static final int CONFIRM_DONATION_BUTTON_SLOT = 20;

	private static CompletableFuture<Void> loaded;

	@Init
	public static void init() {
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

	public static void handleClick(Slot slot, int slotId, DefaultedList<Slot> slots) {
		if (slotId == CONFIRM_DONATION_BUTTON_SLOT) {
			//Slots 0 to 17 can have items, well not all but thats the general range
			for (int i = 0; i < 17; i++) {
				ItemStack stack = slots.get(i).getStack();

				if (!stack.isEmpty()) {
					String itemId = ItemUtils.getItemId(stack);
					String profileId = Utils.getProfileId();

					if (!itemId.isEmpty() && !profileId.isEmpty()) {
						String uuid = Utils.getUndashedUuid();
						//Be safe about access to avoid NPEs
						Map<String, ProfileMuseumData> playerData =  MUSEUM_ITEM_CACHE.computeIfAbsent(uuid, _uuid -> new Object2ObjectOpenHashMap<>());
						playerData.putIfAbsent(profileId, ProfileMuseumData.EMPTY.get());

						playerData.get(profileId).collectedItemIds().add(itemId);
						save();
					}
				}
			}
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

						for (Map.Entry<String, JsonElement> donatedSet : donatedSets.entrySet()) {
							//Item is plural here because the nbt is a list
							String itemsData = donatedSet.getValue().getAsJsonObject().get("items").getAsJsonObject().get("data").getAsString();
							NbtList items = NbtIo.readCompressed(new ByteArrayInputStream(Base64.getDecoder().decode(itemsData)), NbtSizeTracker.ofUnlimitedBytes()).getList("i", NbtElement.COMPOUND_TYPE);

							for (int i = 0; i < items.size(); i++) {
								NbtCompound tag = items.getCompound(i).getCompound("tag");

								if (tag.contains("ExtraAttributes")) {
									NbtCompound extraAttributes = tag.getCompound("ExtraAttributes");

									if (extraAttributes.contains("id")) itemIds.add(extraAttributes.getString("id"));
								}
							}
						}

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
