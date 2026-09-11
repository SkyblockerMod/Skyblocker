package de.hysky.skyblocker.utils;

import java.util.EnumSet;
import java.util.Set;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExclusiveGroupingSetTest {
	@FuzzTest(maxDuration = "10s")
	public void fuzz(FuzzedDataProvider data) {
		ExclusiveGroupingSet<Location> set = new ExclusiveGroupingSet<>();

		int operations = data.consumeInt(1, 100);
		for (int i = 0; i < operations; i++) {
			int action = data.consumeInt(0, 3);
			switch (action) {
				case 0 -> {
					Set<Location> elements = consumeEnumSet(data, Location.class);
					set.group(elements);

					for (Location element : elements) {
						if (elements.size() > 1) {
							assertTrue(set.getGroup(element).isPresent());
							assertEquals(elements, set.getGroup(element).get());
						} else {
							assertTrue(set.getGroup(element).isEmpty());
						}
					}
				}
				case 1 -> {
					Location element = data.pickValue(Location.class.getEnumConstants());
					set.remove(element);

					assertTrue(set.getGroup(element).isEmpty());
				}
				case 2 -> {
					Set<Location> elements = consumeEnumSet(data, Location.class);
					set.removeAll(elements);

					for (Location element : elements) {
						assertTrue(set.getGroup(element).isEmpty());
					}
				}
				case 3 -> set.cleanUp();
			}

			assertSet(set, Location.class);
		}
	}

	private static <T extends Enum<T>> Set<T> consumeEnumSet(FuzzedDataProvider data, Class<T> clazz) {
		Set<T> enumSet = EnumSet.noneOf(clazz);
		for (T constant : clazz.getEnumConstants()) {
			if (data.consumeBoolean()) {
				enumSet.add(constant);
			}
		}
		return enumSet;
	}

	private static <T extends Enum<T>> void assertSet(ExclusiveGroupingSet<T> set, Class<T> clazz) {
		Set<T> seen = EnumSet.noneOf(clazz);
		int size = 0;

		for (Set<T> group : set.sets) {
			seen.addAll(group);
			size += group.size();

			assertTrue(group.size() > 1);
		}

		assertEquals(size, seen.size());
	}
}
