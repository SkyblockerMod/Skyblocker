package de.hysky.skyblocker.skyblock;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
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
		String profileId = Utils.getProfileId();

		if (stack.getSkyblockId().equals("PET") && !profileId.isEmpty()) {
			//I once hoped that all pets would have a petInfo field, but that turned out to be false ;(
			PetInfo petInfo = stack.getPetInfo();

			//This probably shouldn't happen since I would imagine pets inside of a pet menu would have a pet info but you never know...
			if (petInfo.isEmpty()) return;

			shouldLook4Pets = false;

			Object2ObjectOpenHashMap<String, PetInfo> playerData = CACHED_PETS.computeIfAbsent(Utils.getUndashedUuid(), _uuid -> new Object2ObjectOpenHashMap<>());

			//Handle deselecting pets
			if (clicked && getCurrentPet() != null && getCurrentPet().uuid().orElse("").equals(petInfo.uuid().orElse(""))) {
				playerData.remove(profileId);
			} else {
				playerData.put(profileId, petInfo);
			}

			save();
		}
	}

	@Nullable
	public static PetInfo getCurrentPet() {
		String uuid = Utils.getUndashedUuid();
		String profileId = Utils.getProfileId();

		return CACHED_PETS.containsKey(uuid) && CACHED_PETS.get(uuid).containsKey(profileId) ? CACHED_PETS.get(uuid).get(profileId) : null;
	}
}
