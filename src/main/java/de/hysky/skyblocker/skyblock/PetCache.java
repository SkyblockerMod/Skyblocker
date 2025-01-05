package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.profile.ProfiledData;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Doesn't work with auto pet right now because that's complicated.
 * <p>
 * Want support? Ask the Admins for a Mod API event or open your pets menu.
 */
public class PetCache {
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("pet_cache.json");
	private static final ProfiledData<PetInfo> CACHED_PETS = new ProfiledData<>(FILE, PetInfo.CODEC, true, true);

	/**
	 * Used in case the server lags to prevent the screen tick check from overwriting the clicked pet logic
	 */
	private static boolean shouldLook4Pets;

	@Init
	public static void init() {
		CACHED_PETS.load();

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

			//Handle deselecting pets
			if (clicked && getCurrentPet() != null && getCurrentPet().uuid().orElse("").equals(petInfo.uuid().orElse(""))) {
				CACHED_PETS.remove();
			} else {
				CACHED_PETS.put(petInfo);
			}

			CACHED_PETS.save();
		}
	}

	@Nullable
	public static PetInfo getCurrentPet() {
		return CACHED_PETS.get();
	}
}
