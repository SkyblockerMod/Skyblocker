package de.hysky.skyblocker.skyblock.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.util.UndashedUuid;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Http.ApiResponse;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.profile.ProfiledData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MuseumItemCache {
	private static final Logger LOGGER = LoggerFactory.getLogger(MuseumItemCache.class);
	private static final Path CACHE_FILE = SkyblockerMod.CONFIG_DIR.resolve("museum_item_cache.json");
	private static final ProfiledData<ProfileMuseumData> MUSEUM_ITEM_CACHE = new ProfiledData<>(CACHE_FILE, ProfileMuseumData.CODEC, true, true);
	private static final String ERROR_LOG_TEMPLATE = "[Skyblocker] Failed to refresh museum item data for profile {}";
	public static final String DONATION_CONFIRMATION_SCREEN_TITLE = "Confirm Donation";
	private static final int CONFIRM_DONATION_BUTTON_SLOT = 20;

	private static CompletableFuture<Void> loaded;

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> loaded = MUSEUM_ITEM_CACHE.load());
		ClientCommandRegistrationCallback.EVENT.register(MuseumItemCache::registerCommands);
		SkyblockEvents.PROFILE_CHANGE.register((prev, profile) -> tick());
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

	public static void handleClick(Slot slot, int slotId, DefaultedList<Slot> slots) {
		if (slotId == CONFIRM_DONATION_BUTTON_SLOT) {
			//Slots 0 to 17 can have items, well not all but thats the general range
			for (int i = 0; i < 17; i++) {
				ItemStack stack = slots.get(i).getStack();

				if (!stack.isEmpty()) {
					String itemId = ItemUtils.getItemId(stack);
					String profileId = Utils.getProfileId();

					if (!itemId.isEmpty() && !profileId.isEmpty()) {
						MUSEUM_ITEM_CACHE.putIfAbsent(ProfileMuseumData.EMPTY.get()).collectedItemIds().add(itemId);
						MUSEUM_ITEM_CACHE.save();
					}
				}
			}
		}
	}

	private static void updateData4ProfileMember(UUID uuid, String profileId) {
		updateData4ProfileMember(uuid, profileId, null);
	}

	private static void updateData4ProfileMember(UUID uuid, String profileId, FabricClientCommandSource source) {
		CompletableFuture.runAsync(() -> {
			try (ApiResponse response = Http.sendHypixelRequest("skyblock/museum", "?profile=" + profileId)) {
				//The request was successful
				if (response.ok()) {
					JsonObject profileData = JsonParser.parseString(response.content()).getAsJsonObject();
					JsonObject members = profileData.getAsJsonObject("members");

					String uuidString = UndashedUuid.toString(uuid);
					if (members.has(uuidString)) {
						JsonObject memberData = members.get(uuidString).getAsJsonObject();

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

						MUSEUM_ITEM_CACHE.put(uuid, profileId, new ProfileMuseumData(System.currentTimeMillis(), itemIds));
						MUSEUM_ITEM_CACHE.save();

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

	private static void putEmpty(UUID uuid, String profileId) {
		//Only put new data if they didn't have any before
		MUSEUM_ITEM_CACHE.computeIfAbsent(uuid, profileId, () -> new ProfileMuseumData(System.currentTimeMillis(), ObjectOpenHashSet.of()));
		MUSEUM_ITEM_CACHE.save();
	}

	private static boolean tryResync(FabricClientCommandSource source) {
		UUID uuid = Utils.getUuid();
		String profileId = Utils.getProfileId();

		//Only allow resyncing if the data is actually present yet, otherwise the player needs to swap servers for the tick method to be called
		if (loaded.isDone() && !profileId.isEmpty() && MUSEUM_ITEM_CACHE.containsKey() && MUSEUM_ITEM_CACHE.get().canResync()) {
			updateData4ProfileMember(uuid, profileId, source);

			return true;
		}

		return false;
	}

	/**
	 * The cache is ticked upon switching Skyblock servers. Only loads from the API if the profile wasn't cached yet.
	 */
	public static void tick() {
		UUID uuid = Utils.getUuid();

		if (loaded.isDone() && !MUSEUM_ITEM_CACHE.containsKey()) {
			MUSEUM_ITEM_CACHE.putIfAbsent(ProfileMuseumData.EMPTY.get());

			updateData4ProfileMember(uuid, Utils.getProfileId());
		}
	}

	public static boolean hasItemInMuseum(String id) {
		return MUSEUM_ITEM_CACHE.containsKey() && MUSEUM_ITEM_CACHE.get().collectedItemIds().contains(id);
	}

	private record ProfileMuseumData(long lastResync, ObjectOpenHashSet<String> collectedItemIds) {
		private static final Supplier<ProfileMuseumData> EMPTY = () -> new ProfileMuseumData(0L, new ObjectOpenHashSet<>());
		private static final long TIME_BETWEEN_RESYNCING_ALLOWED = 600_000L;
		private static final Codec<ProfileMuseumData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.LONG.fieldOf("lastResync").forGetter(ProfileMuseumData::lastResync),
				Codec.STRING.listOf()
						.xmap(ObjectOpenHashSet::new, ObjectArrayList::new)
						.fieldOf("collectedItemIds")
						.forGetter(ProfileMuseumData::collectedItemIds)
		).apply(instance, ProfileMuseumData::new));

		private boolean canResync() {
			return this.lastResync + TIME_BETWEEN_RESYNCING_ALLOWED < System.currentTimeMillis();
		}
	}
}
