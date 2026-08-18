package de.hysky.skyblocker.utils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockPosSetTest {
	@FuzzTest(maxDuration = "10s")
	public void fuzz(FuzzedDataProvider data) {
		BlockPosSet customSet = new BlockPosSet();
		Set<BlockPos> referenceSet = new HashSet<>();

		int operations = data.consumeInt(1, 200);

		for (int i = 0; i < operations; i++) {
			int action = data.consumeInt(0, 6);

			BlockPos targetPos = new BlockPos(pickXZ(data), pickY(data), pickXZ(data));

			switch (action) {
				case 0 -> {
					assertEquals(referenceSet.size(), customSet.size());
					assertEquals(referenceSet.isEmpty(), customSet.isEmpty());
					assertEquals(referenceSet.size(), customSet.toArray().length);
					assertEquals(referenceSet.size(), customSet.toArray(new BlockPos[0]).length);
				}
				case 1 -> assertEquals(referenceSet.contains(targetPos), customSet.contains(targetPos), "Contains mismatch");
				case 2 -> assertEquals(referenceSet.add(targetPos), customSet.add(targetPos), "Add mismatch at " + targetPos);
				case 3 -> assertEquals(referenceSet.remove(targetPos), customSet.remove(targetPos), "Remove mismatch at " + targetPos);
				case 4 -> {
					Set<BlockPos> retain = new HashSet<>();
					int retainSize = data.consumeInt(1, 100);
					for (int b = 0; b < retainSize; b++) {
						retain.add(new BlockPos(pickXZ(data), pickY(data), pickXZ(data)));
					}
					assertEquals(referenceSet.retainAll(retain), customSet.retainAll(retain));
				}
				case 5 -> {
					customSet.clear();
					referenceSet.clear();
				}
				case 6 -> {
					if (customSet.isEmpty()) break;

					boolean itMut = data.consumeBoolean();
					Iterator<? extends BlockPos> customIt = itMut ? customSet.iterateMut().iterator() : customSet.iterator();
					Iterator<BlockPos> refIt = referenceSet.iterator();

					int steps = data.consumeInt(1, customSet.size());
					BlockPos customLastSeen = null;
					for (int s = 0; s < steps && customIt.hasNext(); s++) {
						customLastSeen = customIt.next();
						refIt.next();
					}

					if (customLastSeen != null && data.consumeBoolean()) {
						customIt.remove();
						refIt.remove();
						assertThrows(IllegalStateException.class, customIt::remove);
					}
				}
			}

			assertEquals(referenceSet.size(), customSet.size(), "Size went out of sync after action " + action);
		}

		assertEquals(referenceSet.size(), customSet.toArray().length, "toArray() returned wrong length");
	}

	private static Integer pickXZ(FuzzedDataProvider data) {
		return data.pickValue(new Integer[]{data.consumeInt(-30_000_000, 30_000_000), 0, 1, -1, 30_000_000, -30_000_000});
	}

	private static Integer pickY(FuzzedDataProvider data) {
		return data.pickValue(new Integer[]{data.consumeInt(-64, 320), 0, 1, -1, -64, 320});
	}

	@Test
	void testAddRemoveContains() {
		long start = System.nanoTime();
		PrintStream devNull = new PrintStream(OutputStream.nullOutputStream());
		fuzzTest(new TestParams(0, 10_000, -64, -64, -64, 64, 64, 64), devNull, System.err);
		fuzzTest(new TestParams(187598127, 10_000, -3, -3, -3, 20, 51, 2), devNull, System.err);
		fuzzTest(new TestParams(791798532, 10_000, 0, 0, 0, 1, 1, 1), devNull, System.err);
		long elapsed = System.nanoTime() - start;
		System.err.printf("E: %.2f ms\n", (double) elapsed / 1_000_000);
	}

	@Test
	void testIterators() {
		long start = System.nanoTime();
		PrintStream devNull = new PrintStream(OutputStream.nullOutputStream());
		iterTest(new TestParams(0, 10_000, -64, -64, -64, 64, 64, 64), devNull, System.err);
		iterTest(new TestParams(187598127, 10_000, -3, -3, -3, 20, 51, 2), devNull, System.err);
		iterTest(new TestParams(791798532, 10_000, 0, 0, 0, 1, 1, 1), devNull, System.err);
		long elapsed = System.nanoTime() - start;
		System.err.printf("E: %.2f ms\n", (double) elapsed / 1_000_000);
	}

	private static void fuzzTest(TestParams params, PrintStream log, PrintStream err) {
		final BlockPosSet set = new BlockPosSet();
		final Set<BlockPos> javaSet = new HashSet<>();
		final Random random = new Random(params.seed());
		for (int iters = 0; iters < 10000; iters++) {
			BlockPos pos = new BlockPos(
					random.nextInt(params.minX(), params.maxX()),
					random.nextInt(params.minY(), params.maxY()),
					random.nextInt(params.minZ(), params.maxZ())
			);

			int action = random.nextInt(3);

			try {
				switch (action) {
					case 0 -> {
						Object expected = javaSet.add(pos);
						assertEquals(expected, set.add(pos));
						log.printf("add %s t %d @ %d\n", pos, BlockPosSet.hashPos(pos) & set.mask, set.indexOf(pos));
					}
					case 1 -> {
						boolean success = javaSet.remove(pos);
						int index = set.indexOf(pos);
						log.printf("remove %s %s @ %d\n", success ? "Y" : "N", pos, index);
						assertEquals(success, set.remove(pos));
					}
					case 2 -> {
						log.printf("contains %s\n", pos);
						assertEquals(javaSet.contains(pos), set.contains(pos));
					}
				}
			} catch (AssertionError e) {
				err.printf("%s\n", set);
				throw e;
			}

			// check sets are the same
			for (BlockPos present : javaSet) {
				try {
					assertTrue(set.contains(present));
				} catch (AssertionError e) {
					err.printf("missing %s from custom @ %s\n", present, (int) BlockPosSet.hashPos(pos) & set.mask);
					for (int i = (int) BlockPosSet.hashPos(pos) & set.mask, j = 0; j < 16; j++, i++) {
						long h = set.entries[i];
						if (h == BlockPosSet.EMPTY) err.print("E ");
						else if (h == BlockPosSet.TOMBSTONE) err.print("T ");
						else err.printf("%s ", BlockPos.of(BlockPosSet.unhashLong(set.entries[i])));
					}
					long[] longs = set.entries;
					for (int i = 0; i < longs.length; i++) {
						long h = longs[i];
						if (h == BlockPosSet.hashPos(pos)) {
							err.printf("%d %d\n", i, set.entries.length);
						}
					}
					throw e;
				}
			}
			for (BlockPos present : set) {
				try {
					assertTrue(javaSet.contains(present));
				} catch (AssertionError e) {
					err.printf("missing %s from javaSet\n".formatted(present));
					throw e;
				}
			}
		}
	}

	private static void iterTest(TestParams params, PrintStream log, PrintStream err) {
		final BlockPosSet set = new BlockPosSet();
		final Random random = new Random(params.seed());
		for (int iters = 0; iters < 10000; iters++) {
			BlockPos pos = new BlockPos(
					random.nextInt(params.minX(), params.maxX()),
					random.nextInt(params.minY(), params.maxY()),
					random.nextInt(params.minZ(), params.maxZ())
			);

			int action = random.nextInt(2);

			switch (action) {
				case 0 -> set.add(pos);
				case 1 -> set.remove(pos);
			}
		}

		{
			Iterator<BlockPos> iterator = set.iterator();
			final Set<BlockPos> javaSet = new HashSet<>();
			int i = 0;
			while (iterator.hasNext()) {
				BlockPos pos = iterator.next();
				i++;
				javaSet.add(pos);
			}
			assertEquals(javaSet.size(), (Object) i);
			assertTrue(javaSet.containsAll(set));
			assertTrue(set.containsAll(javaSet));

			Set<BlockPos> jSetClone = new HashSet<>(javaSet);
			jSetClone.removeAll(set);
			assertEquals(0, (Object) jSetClone.size());

			set.removeAll(javaSet);
			assertEquals(0, (Object) set.size());
		}

		{
			Iterator<BlockPos.MutableBlockPos> iterator = set.iterateMut().iterator();
			final Set<BlockPos> javaSet = new HashSet<>();
			int i = 0;
			while (iterator.hasNext()) {
				BlockPos pos = iterator.next().immutable();
				i++;
				javaSet.add(pos);
			}
			assertEquals(javaSet.size(), (Object) i);
			assertTrue(javaSet.containsAll(set));
			assertTrue(set.containsAll(javaSet));

			Set<BlockPos> jSetClone = new HashSet<>(javaSet);
			jSetClone.removeAll(set);
			assertEquals(0, (Object) jSetClone.size());

			set.removeAll(javaSet);
			assertEquals(0, (Object) set.size());
		}

		{
			Random rand = new Random(0x837E8598EB1DC288L);
			final Set<BlockPos> javaSet = new HashSet<>(set);
			Iterator<BlockPos.MutableBlockPos> iterator = set.iterateMut().iterator();
			int i = 0;
			while (iterator.hasNext()) {
				BlockPos pos = iterator.next();
				if (rand.nextBoolean()) {
					iterator.remove();
					assertTrue(javaSet.remove(pos));
				} else {
					i++;
				}
			}
			assertEquals(javaSet.size(), (Object) i);
			assertTrue(javaSet.containsAll(set));
			assertTrue(set.containsAll(javaSet));

			Set<BlockPos> jSetClone = new HashSet<>(javaSet);
			jSetClone.removeAll(set);
			assertEquals(0, (Object) jSetClone.size());

			set.removeAll(javaSet);
			assertEquals(0, (Object) set.size());
		}

		{
			Iterator<BlockPos> iterator = set.clone().destroyAndIterate().iterator();
			final Set<BlockPos> javaSet = new HashSet<>();
			int i = 0;
			while (iterator.hasNext()) {
				BlockPos pos = iterator.next();
				i++;
				javaSet.add(pos);
			}
			assertEquals(javaSet.size(), (Object) i);
			assertTrue(javaSet.containsAll(set));
			assertTrue(set.containsAll(javaSet));

			Set<BlockPos> jSetClone = new HashSet<>(javaSet);
			jSetClone.removeAll(set);
			assertEquals(0, (Object) jSetClone.size());

			set.removeAll(javaSet);
			assertEquals(0, (Object) set.size());
		}
	}


	private record TestParams(int seed, int iters, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		private static TestParams fromRandom(Random random) {
			final int stddev = 100;
			int x0 = (int) (random.nextGaussian(0, stddev));
			int x1 = (int) (random.nextGaussian(0, stddev));
			int y0 = random.nextInt(0, stddev * 5);
			int y1 = random.nextInt(0, stddev * 5);
			int z0 = (int) (random.nextGaussian(0, stddev));
			int z1 = (int) (random.nextGaussian(0, stddev));
			return new TestParams(
					random.nextInt(),
					10_000,
					Math.min(x0, x1),
					Math.min(y0, y1),
					Math.min(z0, z1),
					x0 == x1 ? x0 + 1 : Math.max(x0, x1),
					y0 == y1 ? y0 + 1 : Math.max(y0, y1),
					z0 == z1 ? z0 + 1 : Math.max(z0, z1)
			);
		}
	}
}
