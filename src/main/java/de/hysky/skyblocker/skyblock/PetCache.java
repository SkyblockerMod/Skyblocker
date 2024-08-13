package de.hysky.skyblocker.skyblock;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Doesn't work with auto pet right now because thats complicated.
 * <p>
 * Want support? Ask the Admins for a Mod API event or open your pets menu.
 */
public class PetCache {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("pet_cache.json");
	private static final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, PetInfo>> CACHED_PETS = new Object2ObjectOpenHashMap<>();

	/**
	 * Used in case the server lags to prevent the screen tick check from overwriting the clicked pet logic
	 */
	private static boolean shouldLook4Pets;

	@Init
	public static void init() {
		load();

		ScreenEvents.BEFORE_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			if (Utils.isOnSkyblock() && screen instanceof GenericContainerScreen genericContainerScreen) {
				if (genericContainerScreen.getTitle().getString().startsWith("Pets")) {
					shouldLook4Pets = true;

					ScreenEvents.afterTick(screen).register(screen1 -> {
						if (shouldLook4Pets) {
							for (Slot slot : genericContainerScreen.getScreenHandler().slots) {
								ItemStack stack = slot.getStack();

								if (!stack.isEmpty() && ItemUtils.getLoreLineIf(stack, line -> line.equals("Click to despawn!")) != null) {
									shouldLook4Pets = false;
									parsePet(stack, false);

									break;
								}
							}
						}
					});
				}
			}
		});
	}

	private static void load() {
		CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(FILE)) {
				CACHED_PETS.putAll(PetInfo.SERIALIZATION_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow());
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Pet Cache] Failed to load saved pet!", e);
			}
		});
	}

	private static void save() {
		CompletableFuture.runAsync(() -> {
			try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
				SkyblockerMod.GSON.toJson(PetInfo.SERIALIZATION_CODEC.encodeStart(JsonOps.INSTANCE, CACHED_PETS).getOrThrow(), writer);
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Pet Cache] Failed to save pet data to the cache!", e);
			}
		});
	}

	public static void handlePetEquip(Slot slot, int slotId) {
		//Ignore inventory clicks
		if (slotId >= 0 && slotId <= 53) {
			ItemStack stack = slot.getStack();

			if (!stack.isEmpty()) parsePet(stack, true);
		}
	}

	private static void parsePet(ItemStack stack, boolean clicked) {
		String id = ItemUtils.getItemId(stack);
		String profileId = Utils.getProfileId();

		if (id.equals("PET") && !profileId.isEmpty()) {
			NbtCompound customData = ItemUtils.getCustomData(stack);

			//Should never fail, all pets must have this but you never know with Hypixel
			try {
				PetInfo petInfo = PetInfo.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(customData.getString("petInfo"))).getOrThrow();
				shouldLook4Pets = false;

				Object2ObjectOpenHashMap<String, PetInfo> playerData = CACHED_PETS.computeIfAbsent(Utils.getUndashedUuid(), _uuid -> new Object2ObjectOpenHashMap<>());

				//Handle deselecting pets
				if (clicked && getCurrentPet() != null && getCurrentPet().uuid().orElse("").equals(petInfo.uuid().orElse(""))) {
					playerData.remove(profileId);
				} else {
					playerData.put(profileId, petInfo);
				}

				save();
			} catch (Exception e) {
				LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Pet Cache] Failed to parse pet's pet info!", e);
			}
		}
	}

	@Nullable
	public static PetInfo getCurrentPet() {
		String uuid = Utils.getUndashedUuid();
		String profileId = Utils.getProfileId();

		return CACHED_PETS.containsKey(uuid) && CACHED_PETS.get(uuid).containsKey(profileId) ? CACHED_PETS.get(uuid).get(profileId) : null;
	}

	public record PetInfo(String type, double exp, String tier, Optional<String> uuid, Optional<String> item, Optional<String> skin) {
		// TODO: Combine with SkyblockItemRarity
		private static final String[] TIER_INDEX = {"COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC"};

		public static final Codec<PetInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("type").forGetter(PetInfo::type),
				Codec.DOUBLE.fieldOf("exp").forGetter(PetInfo::exp),
				Codec.STRING.fieldOf("tier").forGetter(PetInfo::tier),
				Codec.STRING.optionalFieldOf("uuid").forGetter(PetInfo::uuid),
				Codec.STRING.optionalFieldOf("heldItem").forGetter(PetInfo::item),
				Codec.STRING.optionalFieldOf("skin").forGetter(PetInfo::skin)
		).apply(instance, PetInfo::new));
		private static final Codec<Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, PetInfo>>> SERIALIZATION_CODEC = Codec.unboundedMap(Codec.STRING,
				Codec.unboundedMap(Codec.STRING, CODEC).xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new)
		).xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new);

		public int tierIndex() {
			return ArrayUtils.indexOf(TIER_INDEX, tier);
		}
	}
}
