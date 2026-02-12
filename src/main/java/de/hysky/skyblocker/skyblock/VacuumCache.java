package de.hysky.skyblocker.skyblock;


import java.nio.file.Path;

import org.jspecify.annotations.Nullable;

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

public class VacuumCache {
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("vacuum_cache.json");
	private static final ProfiledData<String> CACHED_VINYL = new ProfiledData<>(FILE, Codec.STRING);

	private VacuumCache() {}

	@Init
	public static void init() {
		CACHED_VINYL.load();

		ScreenEvents.BEFORE_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			if (Utils.isOnSkyblock() && screen instanceof ContainerScreen genericContainerScreen) {
				if (genericContainerScreen.getTitle().getString().startsWith("Stereo Harmony")) {
					ScreenEvents.afterTick(screen).register(screen1 -> {
						boolean noneSelected = true;

						for (Slot slot : genericContainerScreen.getMenu().slots) {
							ItemStack stack = slot.getItem();

							if (!stack.isEmpty() && ItemUtils.getLoreLineIf(stack, line -> line.equals("Click to stop playing!")) != null) {
								setVinyl(stack.getSkyblockId());
								noneSelected = false;

								break;
							}
						}

						if (noneSelected) setVinyl(null);
					});
				}
			}
		});
	}

	private static void setVinyl(@Nullable String skyblockId) {
		if (Utils.getProfileId().isEmpty()) return;

		if (skyblockId == null) {
			if (getVinyl() != null) {
				CACHED_VINYL.remove();
				CACHED_VINYL.save();
			}
		} else {
			@Nullable String current = getVinyl();

			if (current == null || !current.equals(skyblockId)) {
				CACHED_VINYL.put(skyblockId);
				CACHED_VINYL.save();
			}
		}
	}

	public static @Nullable String getVinyl() {
		return CACHED_VINYL.get();
	}
}
