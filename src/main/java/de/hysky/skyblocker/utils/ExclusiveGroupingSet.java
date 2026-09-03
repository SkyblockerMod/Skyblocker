package de.hysky.skyblocker.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.VisibleForTesting;

/// A collection of disjoint sets (with no common elements) used to group elements.
///
/// For example, this is useful for tracking which locations a widget config was copied to,
/// where the locations in each set share the same config.
public final class ExclusiveGroupingSet<T extends Enum<T>> {
	@VisibleForTesting
	final List<Set<T>> sets;

	public ExclusiveGroupingSet() {
		this(new ArrayList<>());
	}

	private ExclusiveGroupingSet(List<Set<T>> sets) {
		this.sets = sets;
	}

	public static <T extends Enum<T>> Codec<ExclusiveGroupingSet<T>> getCodec(Codec<Set<T>> setCodec) {
		return setCodec.listOf()
				.<List<Set<T>>>xmap(ArrayList::new, Function.identity())
				.xmap(ExclusiveGroupingSet::new, s -> s.sets);
	}

	/// Gets the group that the given location belongs to, if any.
	///
	/// Modifying the returned set will not modify this [ExclusiveGroupingSet].
	public Optional<Set<T>> getGroup(T location) {
		return sets.stream().filter(locations -> locations.contains(location)).findAny().map(EnumSet::copyOf);
	}

	/// Group the given locations, removing them from any other groups.
	public void group(Set<T> locations) {
		removeAll(locations);
		if (locations.size() > 1) {
			sets.add(EnumSet.copyOf(locations));
		}
	}

	/// Removes the given location from its group.
	public void remove(T location) {
		for (Set<T> set : sets) {
			set.remove(location);
		}
		cleanUp();
	}

	/// Removes the given locations from their groups.
	public void removeAll(Collection<T> locations) {
		for (Set<T> set : sets) {
			set.removeAll(locations);
		}
		cleanUp();
	}

	/// We can remove any groups of size 1, since we only care about groupings.
	public void cleanUp() {
		sets.removeIf(s -> s.size() <= 1);
	}
}
