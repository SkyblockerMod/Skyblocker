package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScreenIds {
	public static final Codec<ScreenId> CODEC = Codec.lazyInitialized(() -> StringRepresentable.fromValues(ScreenIds::getScreenIds));
	private static final Set<ScreenId> SCREEN_IDS = new ObjectOpenHashSet<>();
	private static final Map<Location, ScreenId> LOCATIONS = new EnumMap<>(Arrays.stream(Location.values()).collect(Collectors.toMap(
			Function.identity(),
			ScreenId.Loc::new
	)));

	@Init(priority = -10) // get this done quickly before ScreenIds#CODEC is used
	public static void init() {
		SCREEN_IDS.addAll(LOCATIONS.values());
	}

	static ScreenId[] getScreenIds() {
		return SCREEN_IDS.toArray(ScreenId[]::new);
	}

	private static ScreenId register(ScreenId screenId) {
		SCREEN_IDS.add(screenId);
		return screenId;
	}

	public static final ScreenId EVERYWHERE = register(new ScreenId.Named("everywhere", Component.literal("Everywhere")));

	public static ScreenId ofLocation(Location location) {
		return LOCATIONS.get(location);
	}

	public static ScreenId ofCurrentLocation() {
		return ofLocation(Utils.getLocation());
	}
}
