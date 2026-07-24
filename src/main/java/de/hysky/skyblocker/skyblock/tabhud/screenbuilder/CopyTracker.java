package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Tracks where widgets were copied to.
 * Used to have the option to remove the copies of a widget, and to pre-select other copies in the "Copy to" popup to easily "propagate" config changes
 */
public record CopyTracker(Layer hud, Layer tab, Layer secondaryTab) {
	public static final Codec<CopyTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Layer.CODEC.fieldOf("hud").forGetter(CopyTracker::hud),
			Layer.CODEC.fieldOf("tab").forGetter(CopyTracker::tab),
			Layer.CODEC.fieldOf("secondary_tab").forGetter(CopyTracker::secondaryTab)
			).apply(instance, CopyTracker::new)
	);

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

	public record Layer(Map<String, LocationSets> map) {
		public static final Codec<Layer> CODEC = Codec.unboundedMap(Codec.STRING, LocationSets.CODEC)
				.xmap(l -> (Map<String, LocationSets>) new Object2ObjectOpenHashMap<>(l), Function.identity())
				.xmap(Layer::new, Layer::map);

		public Optional<LocationSets> get(String widgetId) {
			return Optional.ofNullable(map.get(widgetId));
		}

		public LocationSets getOrCreate(String widgetId) {
			return map.computeIfAbsent(widgetId, _ -> new LocationSets(new ArrayList<>()));
		}

		public Layer() {
			this(new Object2ObjectOpenHashMap<>());
		}
	}

	/**
	 * A list of sets that each represent locations where the widget was copied to. The sets should not overlap.
	 * @param sets the sets
	 */
	public record LocationSets(List<Set<Location>> sets) {
		// Codec that accepts either a list of locations, or a string (contents doesn't matter) to represent "all locations"
		private static final Codec<Set<Location>> LOCATION_SET_CODEC =  Codec.either(Location.CODEC.listOf(), Codec.STRING)
				.xmap(
						e -> e.map(l -> (Set<Location>) EnumSet.copyOf(l), _ -> EnumSet.copyOf(WidgetManager.ALLOWED_LOCATIONS)),
						s -> s.equals(WidgetManager.ALLOWED_LOCATIONS) ? Either.right("everywhere") : Either.left(List.copyOf(s)));
		public static final Codec<LocationSets> CODEC = LOCATION_SET_CODEC.listOf()
				.xmap(l -> (List<Set<Location>>) new ArrayList<>(l), Function.identity())
				.xmap(LocationSets::new, LocationSets::sets);

		public Optional<Set<Location>> whereHas(Location location) {
			return sets.stream().filter(locations -> locations.contains(location)).findFirst();
		}

		public void track(Set<Location> locations) {
			for (Set<Location> set : sets) {
				set.removeAll(locations);
			}
			sets.add(EnumSet.copyOf(locations));
			sets.removeIf(s -> s.size() <= 1);
		}
	}
}
