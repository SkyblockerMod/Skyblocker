package de.hysky.skyblocker.skyblock.item;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mojang.util.UndashedUuid;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Http.ApiResponse;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;

public class MuseumItemCache {
	private static final Logger LOGGER = LoggerFactory.getLogger(MuseumItemCache.class);
	private static final Path CACHE_FILE = SkyblockerMod.CONFIG_DIR.resolve("museum_item_cache.json");
	private static final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>> MUSEUM_ITEM_CACHE = new Object2ObjectOpenHashMap<>();
	private static final Type MAP_TYPE = new TypeToken<Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>>>() {}.getType();
	
	private static CompletableFuture<Void> loaded;

	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(MuseumItemCache::load);
	}
	
	private static void load(MinecraftClient client) {
		loaded = CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(CACHE_FILE)) {
				Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, ProfileMuseumData>> cachedData = SkyblockerMod.GSON.fromJson(reader, MAP_TYPE);
				
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
				SkyblockerMod.GSON.toJson(MUSEUM_ITEM_CACHE, writer);
			} catch (IOException e) {
				LOGGER.error("[Skyblocker] Failed to save cached museum items!", e);
			}
		});
	}
	
	private static void updateData4ProfileMember(String uuid, String profileId) {
		CompletableFuture.runAsync(() -> {
			try (ApiResponse response = Http.sendHypixelRequest("skyblock/museum", "?profile=" + profileId)) {				
				//The request was successful
				if (response.ok()) {
					JsonObject profileData = JsonParser.parseString(response.content()).getAsJsonObject();
					JsonObject memberData = profileData.get("members").getAsJsonObject().get(uuid).getAsJsonObject();
					
					//We call them sets because it could either be a singular item or an entire armour set
					Map<String, JsonElement> donatedSets = memberData.get("items").getAsJsonObject().asMap();
					
					//Set of all found item ids on profile
					ObjectOpenHashSet<String> itemIds = new ObjectOpenHashSet<>();
					
					for (Map.Entry<String, JsonElement> donatedSet : donatedSets.entrySet()) {
						//Item is plural here because the nbt is a list
						String itemsData = donatedSet.getValue().getAsJsonObject().get("items").getAsJsonObject().get("data").getAsString();
						NbtList items = NbtIo.readCompressed(new ByteArrayInputStream(Base64.getDecoder().decode(itemsData))).getList("i", NbtElement.COMPOUND_TYPE);
						
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
					
					LOGGER.info("[Skyblocker] Successfully updated museum item cache for profile {}", profileId);
				}
			} catch (Exception e) {
				LOGGER.error("[Skyblocker] Failed to refresh museum item data for profile {}", profileId, e);
			}
		});
	}
	
	/**
	 * The cache is ticked upon switching skyblock servers
	 */
	public static void tick(String profileId) {
		if (loaded.isDone()) {
			String uuid = UndashedUuid.toString(MinecraftClient.getInstance().getSession().getUuidOrNull());
			Object2ObjectOpenHashMap<String, ProfileMuseumData> playerData = MUSEUM_ITEM_CACHE.computeIfAbsent(uuid, uuid1 -> Util.make(new Object2ObjectOpenHashMap<>(), map -> {
				map.put(profileId, ProfileMuseumData.EMPTY);
			}));
			
			if (playerData.get(profileId).stale()) updateData4ProfileMember(uuid, profileId);
		}
	}
	
	public static boolean hasItemInMuseum(String id) {
		String uuid = UndashedUuid.toString(MinecraftClient.getInstance().getSession().getUuidOrNull());
		ObjectOpenHashSet<String> collectedItemIds = MUSEUM_ITEM_CACHE.get(uuid).get(Utils.getProfileId()).collectedItemIds();
		
		return collectedItemIds != null && collectedItemIds.contains(id);
	}
	
	private record ProfileMuseumData(long lastUpdated, ObjectOpenHashSet<String> collectedItemIds) {
		private static final ProfileMuseumData EMPTY = new ProfileMuseumData(0L, null);
		private static final long MAX_AGE = 86_400_000;
		
		private boolean stale() {
			return System.currentTimeMillis() > lastUpdated + MAX_AGE;
		}
	}
}
