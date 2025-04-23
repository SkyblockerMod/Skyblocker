package de.hysky.skyblocker.skyblock.dungeon;

import it.unimi.dsi.fastutil.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class DungeonMapPlayerMatchDataTest {
	private final DungeonMap.PlayerMatchData<Pair<UUID, String>> playerMatchData = new DungeonMap.PlayerMatchData<>();

	@Test
	void testAddMatch() {
		List<Pair<UUID, String>> keys = IntStream.range(0, 10).mapToObj(i -> Pair.of(UUID.randomUUID(), String.valueOf(i))).toList();
		for (int i = 0; i < 10; i++) {
			for (Pair<UUID, String> key : keys) {
				playerMatchData.addMatch(key);
			}
			Assertions.assertEquals(10 * (i + 1), playerMatchData.matches().size());
			Assertions.assertEquals(10, playerMatchData.counts().size());
		}
		Assertions.assertEquals(100, playerMatchData.matches().size());
		Assertions.assertEquals(10, playerMatchData.counts().size());

		playerMatchData.addMatch(keys.getFirst());
		Assertions.assertEquals(100, playerMatchData.matches().size());
		Assertions.assertEquals(10, playerMatchData.counts().size());

		for (Pair<UUID, String> key : keys) {
			playerMatchData.addMatch(key);
		}
		Assertions.assertEquals(100, playerMatchData.matches().size());
		Assertions.assertEquals(10, playerMatchData.counts().size());
	}

	@Test
	void testMostFrequent() {
		List<Pair<UUID, String>> keys = IntStream.range(0, 11).mapToObj(i -> Pair.of(UUID.randomUUID(), String.valueOf(i))).toList();
		playerMatchData.addMatch(keys.getFirst());
		for (int i = 0; i < keys.size(); i++) {
			Pair<UUID, String> key = keys.get(i);
			for (int j = 0; j < 9; j++) {
				playerMatchData.addMatch(key);
			}
			Assertions.assertEquals(9 * (i + 1) + 1, playerMatchData.matches().size());
			Assertions.assertEquals(i + 1, playerMatchData.counts().size());
			Assertions.assertTrue(playerMatchData.getMostFrequent().isPresent());
			Assertions.assertEquals(keys.getFirst(), playerMatchData.getMostFrequent().get());
		}

		playerMatchData.addMatch(keys.get(1));
		Assertions.assertEquals(100, playerMatchData.matches().size());
		Assertions.assertEquals(11, playerMatchData.counts().size());
		Assertions.assertTrue(playerMatchData.getMostFrequent().isPresent());
		Assertions.assertEquals(keys.get(1), playerMatchData.getMostFrequent().get());
	}

	@Test
	void testRemove() {
		List<Pair<UUID, String>> keys = IntStream.range(0, 2).mapToObj(i -> Pair.of(UUID.randomUUID(), String.valueOf(i))).toList();
		Assertions.assertEquals(0, playerMatchData.matches().size());
		Assertions.assertEquals(0, playerMatchData.counts().size());
		Assertions.assertTrue(playerMatchData.getMostFrequent().isEmpty());

		for (int i = 0; i < 100; i++) {
			playerMatchData.addMatch(keys.getFirst());
		}
		Assertions.assertEquals(100, playerMatchData.matches().size());
		Assertions.assertEquals(1, playerMatchData.counts().size());
		Assertions.assertTrue(playerMatchData.getMostFrequent().isPresent());
		Assertions.assertEquals(keys.getFirst(), playerMatchData.getMostFrequent().get());

		for (int i = 0; i < 100; i++) {
			playerMatchData.addMatch(keys.getLast());
		}
		Assertions.assertEquals(100, playerMatchData.matches().size());
		Assertions.assertEquals(1, playerMatchData.counts().size());
		Assertions.assertTrue(playerMatchData.getMostFrequent().isPresent());
		Assertions.assertEquals(keys.getLast(), playerMatchData.getMostFrequent().get());
	}
}
