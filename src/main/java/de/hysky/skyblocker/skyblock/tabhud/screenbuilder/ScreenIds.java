package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScreenIds {
	private static final Map<Location, ScreenId> LOCATIONS = new EnumMap<>(Arrays.stream(Location.values()).collect(Collectors.toMap(
			Function.identity(),
			ScreenId.Loc::new
	)));

	public static final ScreenId EVERYWHERE = new ScreenId.Named("everywhere", Component.literal("Everywhere"));

	public static ScreenId ofLocation(Location location) {
		return LOCATIONS.get(location);
	}

	public static ScreenId ofCurrentLocation() {
		return ofLocation(Utils.getLocation());
	}
}
