package de.hysky.skyblocker.utils;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class BlockPosSetTest {
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
						expectEqual(javaSet.add(pos), set.add(pos));
						log.printf("add %s t %d @ %d\n", pos, BlockPosSet.hashPos(pos) & (set.capacity - 1), set.indexOf(pos));
					}
					case 1 -> {
						boolean success = javaSet.remove(pos);
						int index = set.indexOf(pos);
						log.printf("remove %s %s @ %d\n", success ? "Y" : "N", pos, index);
						expectEqual(success, set.remove(pos));
					}
					case 2 -> {
						log.printf("contains %s\n", pos);
						expectEqual(javaSet.contains(pos), set.contains(pos));
					}
				}
			} catch (AssertionError e) {
				err.printf("%s\n", set);
				throw e;
			}

			// check sets are the same
			for (BlockPos present : javaSet) {
				try {
					expectEqual(true, set.contains(present));
				} catch (AssertionError e) {
					err.printf("missing %s from custom @ %s\n", present, (int) BlockPosSet.hashPos(pos) & (set.capacity - 1));
					for (int i = (int) BlockPosSet.hashPos(pos) & (set.capacity - 1), j = 0; j < 16; j++, i++) {
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
					expectEqual(true, javaSet.contains(present));
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
			expectEqual(javaSet.size(), i);
			expectEqual(true, javaSet.containsAll(set));
			expectEqual(true, set.containsAll(javaSet));

			Set<BlockPos> jSetClone = new HashSet<>(javaSet);
			jSetClone.removeAll(set);
			expectEqual(0, jSetClone.size());

			set.removeAll(javaSet);
			expectEqual(0, set.size());
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
			expectEqual(javaSet.size(), i);
			expectEqual(true, javaSet.containsAll(set));
			expectEqual(true, set.containsAll(javaSet));

			Set<BlockPos> jSetClone = new HashSet<>(javaSet);
			jSetClone.removeAll(set);
			expectEqual(0, jSetClone.size());

			set.removeAll(javaSet);
			expectEqual(0, set.size());
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
					expectEqual(true, javaSet.remove(pos));
				} else {
					i++;
				}
			}
			expectEqual(javaSet.size(), i);
			expectEqual(true, javaSet.containsAll(set));
			expectEqual(true, set.containsAll(javaSet));

			Set<BlockPos> jSetClone = new HashSet<>(javaSet);
			jSetClone.removeAll(set);
			expectEqual(0, jSetClone.size());

			set.removeAll(javaSet);
			expectEqual(0, set.size());
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
			expectEqual(javaSet.size(), i);
			expectEqual(true, javaSet.containsAll(set));
			expectEqual(true, set.containsAll(javaSet));

			Set<BlockPos> jSetClone = new HashSet<>(javaSet);
			jSetClone.removeAll(set);
			expectEqual(0, jSetClone.size());

			set.removeAll(javaSet);
			expectEqual(0, set.size());
		}
	}


	public static void expectEqual(Object expected, Object actual) {
		if (!Objects.equals(expected, actual)) {
			throw new AssertionError("Expected %s, got %s".formatted(expected, actual));
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
