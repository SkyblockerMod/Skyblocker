package de.hysky.skyblocker.skyblock.garden;

import com.mojang.serialization.Codec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;

public class VacuumCache {
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("vacuum_cache.json");
	private static final ProfiledData<String> CACHED_VINYL = new ProfiledData<>(FILE, Codec.STRING);

	private VacuumCache() {}

	@Init
	public static void init() {
		CACHED_VINYL.load();

		ScreenEvents.BEFORE_INIT.register((_, screen, _, _) -> {
			if (!Utils.isOnSkyblock() || !(screen instanceof ContainerScreen containerScreen)) {
				return;
			}

			if (!containerScreen.getTitle().getString().equals("Stereo Harmony")) {
				return;
			}

			ScreenEvents.remove(screen).register(_ -> VacuumCache.update(containerScreen));
		});
	}

	private static void update(ContainerScreen containerScreen) {
		for (Slot slot : containerScreen.getMenu().slots) {
			ItemStack stack = slot.getItem();

			if (!stack.isEmpty() && ItemUtils.getLoreLineIf(stack, line -> line.equals("Click to stop playing!")) != null) {
				setVinyl(stack.getSkyblockId());

				return;
			}
		}

		setVinyl("");
	}

	private static void setVinyl(String skyblockId) {
		if (Utils.getProfileId().isEmpty()) return;

		if (skyblockId.isEmpty()) {
			CACHED_VINYL.remove();
		} else {
			CACHED_VINYL.put(skyblockId);
		}

		CACHED_VINYL.save();
	}

	public static String getVinyl() {
		return CACHED_VINYL.containsKey() ? CACHED_VINYL.get() : "";
	}
}
