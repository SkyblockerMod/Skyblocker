package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import de.hysky.skyblocker.utils.ExclusiveGroupingSet;
import de.hysky.skyblocker.utils.Location;

/**
 * Tracks where widgets were copied to.
 * Used to have the option to remove the copies of a widget, and to pre-select other copies in the "Copy to" popup to easily "propagate" config changes
 */
public record CopyTracker(Layer hud, Layer tab, Layer secondaryTab) {
	public static final Codec<CopyTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Layer.CODEC.fieldOf("hud").forGetter(CopyTracker::hud),
			Layer.CODEC.fieldOf("tab").forGetter(CopyTracker::tab),
			Layer.CODEC.fieldOf("secondary_tab").forGetter(CopyTracker::secondaryTab)
	).apply(instance, CopyTracker::new));
	// Codec that accepts either a list of locations, or a string (contents doesn't matter) to represent "all locations"
	private static final Codec<Set<Location>> LOCATION_SET_CODEC = Codec.either(Location.CODEC.listOf(), Codec.STRING).xmap(
			// There was a bug where the Set<Location> could be empty which needs handling so the config can load (EnumSet#copyOf does not work with empty collections).
			// Versions after 6.10.1 should not have this issue anymore and won't serialize an empty set.
			e -> e.map(l -> l.isEmpty() ? EnumSet.noneOf(Location.class) : EnumSet.copyOf(l), _ -> EnumSet.copyOf(WidgetManager.ALLOWED_LOCATIONS)),
			s -> s.equals(WidgetManager.ALLOWED_LOCATIONS) ? Either.right("everywhere") : Either.left(List.copyOf(s))
	);
	private static final Codec<ExclusiveGroupingSet<Location>> LOCATION_SETS_CODEC = ExclusiveGroupingSet.getCodec(LOCATION_SET_CODEC);

	public CopyTracker() {
		this(new Layer(), new Layer(), new Layer());
	}

	public Layer get(WidgetManager.ScreenLayer layer) {
		return switch (layer) {
			case HUD -> hud;
			case MAIN_TAB ->  tab;
			case SECONDARY_TAB -> secondaryTab;
		};
	}

	public record Layer(Map<String, ExclusiveGroupingSet<Location>> map) {
		public static final Codec<Layer> CODEC = Codec.unboundedMap(Codec.STRING, LOCATION_SETS_CODEC)
				.<Map<String, ExclusiveGroupingSet<Location>>>xmap(Object2ObjectOpenHashMap::new, Function.identity())
				.xmap(Layer::new, Layer::map);

		public Optional<ExclusiveGroupingSet<Location>> get(String widgetId) {
			return Optional.ofNullable(map.get(widgetId));
		}

		public ExclusiveGroupingSet<Location> getOrCreate(String widgetId) {
			return map.computeIfAbsent(widgetId, _ -> new ExclusiveGroupingSet<>());
		}

		public Layer() {
			this(new Object2ObjectOpenHashMap<>());
		}
	}
}
