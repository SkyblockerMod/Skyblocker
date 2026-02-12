package de.hysky.skyblocker.skyblock;


import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class VacuumCache {
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("vacuum_cache.json");
	private static final ProfiledData<String> CACHED_VINYL = new ProfiledData<>(FILE, Codec.STRING);
	private static final Pattern VINYL_PATTERN = Pattern.compile("^When playing, (?<vinyl>[\\w\\s]+) Pests");

	private VacuumCache() {}

	@Init
	public static void init() {
		CACHED_VINYL.load();

		ScreenEvents.BEFORE_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			if (Utils.isOnSkyblock() && screen instanceof ContainerScreen genericContainerScreen) {
				if (genericContainerScreen.getTitle().getString().startsWith("Stereo Harmony")) {
					ScreenEvents.afterTick(screen).register(screen1 -> {
						for (Slot slot : genericContainerScreen.getMenu().slots) {
							ItemStack stack = slot.getItem();

							if (!stack.isEmpty() && ItemUtils.getLoreLineIf(stack, line -> line.equals("Click to stop playing!")) != null) {
								parseVinyl(stack, false);

								break;
							}
						}
					});
				}
			}
		});
	}

	public static void handleVinylSelect(Slot slot, int slotId) {
		ItemStack stack = slot.getItem();

		if (!stack.isEmpty()) parseVinyl(stack, true);
	}

	private static void parseVinyl(ItemStack stack, boolean clicked) {
		String profileId = Utils.getProfileId();

		if (stack.getSkyblockId().startsWith("VINYL_") && !profileId.isEmpty()) {
			@Nullable String vinyl = null;

			for (String line : stack.skyblocker$getLoreStrings()) {
				String stringified = ChatFormatting.stripFormatting(line);
				Matcher matcher = VINYL_PATTERN.matcher(stringified);

				if (matcher.matches()) {
					vinyl = matcher.group("vinyl");

					break;
				}
			}

			if (vinyl == null) return;

			if (clicked && getVinyl() != null && ItemUtils.getLoreLineIf(stack, line -> line.equals("Click to stop playing!")) != null) {
				CACHED_VINYL.remove();
			} else {
				CACHED_VINYL.put(vinyl);
			}

			CACHED_VINYL.save();
		}
	}

	public static @Nullable String getVinyl() {
		return CACHED_VINYL.get();
	}
}
