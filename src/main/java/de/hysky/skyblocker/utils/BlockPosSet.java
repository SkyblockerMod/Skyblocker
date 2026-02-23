package de.hysky.skyblocker.utils;

import net.minecraft.core.BlockPos;

import java.util.*;

public class BlockPosSet implements Iterable<BlockPos>, Set<BlockPos> {
	/// Sentinels for tombstones (deleted slots) and empty slots
	/// This does mean that the set cannot store the positions
	/// new BlockPos(unhashLong(TOMBSTONE)) and
	/// new BlockPos(unhashLong(EMPTY)),
	/// aka the coordinates
	/// [15_425_900, 107, 31_185_031] and
	/// [31_556_398, 107, 9_164_935].
	/// However, this is unlikely to be an issue, as both positions
	/// are outside the world border (+-30_000_000, +-30_000_000)
	public final static long TOMBSTONE = Long.MAX_VALUE;
	public final static long EMPTY = TOMBSTONE - 1;
	public final static float LOAD_FACTOR = 0.75f;
	public final static int LANES = 4;

	public int size = 0;
	public int tombstones = 0;
	///  Must be a power of two
	public int capacity = 8;
	/// Saves a single subtraction. WOW
	public int mask = capacity - 1;
	public int nextGrowThreshold = Math.min((int) (this.capacity * LOAD_FACTOR), this.capacity - 1);
	public long[] entries = new long[this.capacity + LANES - 1];

	// debug fields
	public final static boolean DEBUG = false;
	public static int TOMBSTONES_CLEARED = 0;
	public static int QUERIES = 0;
	public static int PROBES = 0;

	public BlockPosSet() {
		Arrays.fill(this.entries, EMPTY);
		if (DEBUG) {
			TOMBSTONES_CLEARED = 0;
			QUERIES = 0;
			PROBES = 0;
		}
	}

	/// Roughly 1.5x faster than LongOpenHashSet at high load factors
	/// Roughly on par at low load factors
	public boolean containsXyz(int x, int y, int z) {
		if (DEBUG) QUERIES++;
		final long hash = hashXyz(x, y, z);
		final int mask = this.mask;
		int i = (int) hash & mask;

		do {
			// TODO: Switch to this when Vector API stabilizes
			/*
			LongVector n = LongVector.fromArray(LongVector.SPECIES_512, this.entries, i);
			if (n.compare(VectorOperators.EQ, hash).anyTrue()) return true;
			if (n.compare(VectorOperators.EQ, EMPTY).anyTrue()) return false;
			 */

			// The JIT doesn't actually manage to vectorize this code.
			// Why is it profitable over a naive loop, then?
			// I was originally going something about making the branch more predictable
			// (due to the bitwise or), but then the bitwise or randomly
			// started being worse than logical or. So frankly your guess is as good as mine.
			// The loop unrolling probably helps.
			@SuppressWarnings("PointlessArithmeticExpression")
			long n0 = entries[i + 0];
			long n1 = entries[i + 1];
			long n2 = entries[i + 2];
			long n3 = entries[i + 3];
			if (n0 == hash || n1 == hash || n2 == hash || n3 == hash) return true;
			if (n0 == EMPTY || n1 == EMPTY || n2 == EMPTY || n3 == EMPTY) return false;
			i = (i + LANES) & mask;

			// Non-unrolled version
			/*
			if (entries[i] == hash) return true;
			if (entries[i] == EMPTY) return false;
			i = (i + 1) & mask;
			*/
			if (DEBUG) PROBES++;
		} while (true);
	}

	/// We store the hash instead of the long representing the blockpos.
	/// This is a tradeoff: It allows us to resize the map faster, because
	/// we don't have to recompute the hash. However, iteration is slower because
	/// we have to unhash the values before unpacking them.
	public boolean addXYZ(int x, int y, int z) {
		if (this.size >= this.nextGrowThreshold) this.resize(this.capacity * 2);
		else if (this.tombstones >= this.capacity - this.nextGrowThreshold) this.resize(this.capacity);

		if (DEBUG) QUERIES++;
		final long hash = hashXyz(x, y, z);

		final int mask = this.mask;
		int slotToInsert = (int) hash & mask;
		int candidateTombstoneSlot = Integer.MAX_VALUE;

		// scan slots for empty spot
		do {
			// already exists
			if (this.entries[slotToInsert] == hash) {
				return false;
			}
			// we can't insert immediately if we find a tombstone slot, because the entry could show up later
			else if (this.entries[slotToInsert] == TOMBSTONE && candidateTombstoneSlot == Integer.MAX_VALUE) {
				candidateTombstoneSlot = slotToInsert;
			}
			// found an absent slot
			else if (this.entries[slotToInsert] == EMPTY) {
				// check if we found a tombstone to replace earlier
				if (candidateTombstoneSlot != Integer.MAX_VALUE) {
					slotToInsert = candidateTombstoneSlot;
					// if we replaced a tombstone, reduce tombstone count
					this.tombstones--;
				}
				this.entries[slotToInsert] = hash;
				this.size++;

				if (slotToInsert < LANES - 1) this.entries[slotToInsert + this.capacity] = hash;
				return true;
			}
			// wrap to beginning
			// we will never enter an infinite loop because there is at least 1 empty slot

			if (DEBUG) PROBES++;
			slotToInsert = (slotToInsert + 1) & mask;
		} while (true);
	}

	public boolean removeXyz(int x, int y, int z) {
		if (this.size == 0) return false;

		final long hash = hashXyz(x, y, z);
		final int mask = this.capacity - 1;
		int i = (int) hash & mask;

		do {
			if (this.entries[i] == hash) {
				// replace with tombstone
				this.entries[i] = TOMBSTONE;
				if (i < LANES - 1) this.entries[i + this.capacity] = TOMBSTONE;
				this.size--;
				this.tombstones++;

				// resize if falling below quarter load
				/*
				if (this.size < this.capacity * 0.25f && this.size > 8) {
					this.resize(this.capacity / 2);
					return true;
				}
				// */

				// travel backwards and attempt to remove tombstones if the next slot is empty
//                /*
				do {
					if (this.entries[(i + 1) & mask] == TOMBSTONE) {
						this.entries[i] = EMPTY;
						this.tombstones--;
						if (DEBUG) TOMBSTONES_CLEARED++;
					}
					i = (i - 1) & mask;
				} while (this.entries[i] == TOMBSTONE);
				// */

				return true;
			} else if (this.entries[i] == EMPTY) return false;
			i = (i + 1) & mask;
		} while (true);
	}

	public void resize(int newCapacity) {
		if (newCapacity < this.size)
			throw new AssertionError("capacity must be greater than size! (got: %d, size: %d)".formatted(newCapacity, this.size));
		if ((newCapacity & newCapacity - 1) != 0)
			throw new AssertionError("capacity must be a power of two! (got: %d)".formatted(newCapacity));
		final int mask = newCapacity - 1;
		long[] newEntries = new long[newCapacity + LANES - 1];
		Arrays.fill(newEntries, EMPTY);


		// iterate through the current entries array
		for (int currentIndex = 0; currentIndex < this.capacity; currentIndex++) {
			// skip tombstones and absents
			if (this.entries[currentIndex] >= EMPTY) continue;

			// add to new entries array
			final long hash = this.entries[currentIndex];
			int newIndex = (int) hash & mask;
			inner:
			while (true) {
				// found new slot, add here
				// no need to check for tombstones here
				if (newEntries[newIndex] == EMPTY) {
					newEntries[newIndex] = hash;
					if (newIndex < LANES - 1) newEntries[newIndex + newCapacity] = hash;
					break inner;
				}
				// wrap to beginning
				// no need to worry about an infinite loop, because we always have space
				newIndex = (newIndex + 1) & mask;
			}
		}

		// no more tombstones
		this.tombstones = 0;
		this.capacity = newCapacity;
		this.mask = newCapacity - 1;
		this.nextGrowThreshold = Math.min((int) (this.capacity * LOAD_FACTOR), this.capacity - 1);
		this.entries = newEntries;
	}

	public void clearRetainingCapacity() {
		this.size = 0;
		Arrays.fill(this.entries, EMPTY);
	}

	public static long hashPos(BlockPos pos) {
		return hashXyz(pos.getX(), pos.getY(), pos.getZ());
	}

	public static long hashXyz(int x, int y, int z) {
		return hashLong(
				BlockPos.asLong(x, y, z)
		);
	}

	/// This bijective hash function is faster than the one fastutil uses
	/// (2 instructions worth of latency vs 5) while still providing
	/// a good distribution from empirical testing.
	public static long hashLong(final long l) {
		// https://github.com/torvalds/linux/blob/master/include/linux/hash.h#L42 <- this one sucks
		// https://www.pcg-random.org/posts/does-it-beat-the-minimal-standard.html <- used the 64 bit mcg constant
		return Long.rotateRight(l * 0xcb45348a28cb43bdL, 32); // 64 bit mcg
	}

	public static long unhashLong(final long h) {
		return Long.rotateLeft(h, 32) * 0xE3EBDD17C2778F95L;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public boolean isEmpty() {
		return this.size > 0;
	}

	@Override
	public boolean contains(Object o) {
		return o instanceof BlockPos pos && this.containsXyz(pos.getX(), pos.getY(), pos.getZ());
	}

	public boolean contains(BlockPos pos) {
		return this.containsXyz(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public Object[] toArray() {
		int size = this.size;
		int capacity = this.capacity;
		long[] entries = this.entries;

		BlockPos[] array = new BlockPos[size];
		for (int i = 0, j = 0; i < capacity && j < size; i++) {
			long h = entries[i];
			if (h >= EMPTY) continue;

			array[j] = BlockPos.of(unhashLong(h));
		}
		return array;
	}

	@SuppressWarnings({"unchecked", "DataFlowIssue"})
	@Override
	public <T> T[] toArray(T[] a) {
		int size = this.size;
		int capacity = this.capacity;
		long[] entries = this.entries;

		if (a.length < size) a = (T[]) new BlockPos[size];
		else if (a.length > size) a[size] = null;

		for (int i = 0, j = 0; i < capacity && j < size; i++) {
			long h = entries[i];
			if (h >= EMPTY) continue;

			a[j] = (T) BlockPos.of(unhashLong(h));
		}

		return a;
	}

	@Override
	public boolean add(BlockPos blockPos) {
		return this.addXYZ(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	@Override
	public boolean remove(Object o) {
		return o instanceof BlockPos pos && this.removeXyz(pos.getX(), pos.getY(), pos.getZ());
	}

	public boolean remove(BlockPos pos) {
		return this.removeXyz(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		Iterator<?> iter = c.iterator();
		while (iter.hasNext() && iter.next() instanceof BlockPos pos) {
			if (!this.contains(pos)) return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends BlockPos> c) {
		Iterator<?> iter = c.iterator();
		boolean anyAdded = false;
		while (iter.hasNext() && iter.next() instanceof BlockPos pos) {
			anyAdded |= this.add(pos);
		}
		return anyAdded;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		long[] longs = this.entries;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		boolean removedAny = false;

		for (int i = 0, capacity = this.capacity; i < capacity; i++) {
			long h = longs[i];
			if (h >= EMPTY) continue;
			pos.set(h);
			if (!c.contains(pos)) {
				longs[i] = TOMBSTONE;
				this.size--;
				this.tombstones++;
				removedAny = true;

				if (i < LANES - 1) longs[i + capacity] = TOMBSTONE;
			}
		}

		// TODO: resize at the end
		return removedAny;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		Iterator<?> iter = c.iterator();
		boolean removedAny = false;

		final long[] entries = this.entries;
		int capacity = this.capacity;
		final int mask = this.mask;

		while (iter.hasNext() && iter.next() instanceof BlockPos pos) {
			boolean result = false;
			if (this.size != 0) {
				final long hash = hashPos(pos);
				int i = (int) hash & mask;
				do {
					if (entries[i] == hash) {
						// replace with tombstone
						entries[i] = TOMBSTONE;
						if (i < LANES - 1) entries[i + capacity] = TOMBSTONE;
						this.size--;
						this.tombstones++;

						result = true;
						break;
					} else if (entries[i] == EMPTY) {
						break;
					}
					i = (i + 1) & mask;
				} while (true);
			}

			removedAny |= result;
		}
		return removedAny;
	}

	@Override
	public void clear() {
		this.clearRetainingCapacity();
	}


	@Override
	public Iterator<BlockPos> iterator() {
		return new Iter(this.capacity, this.entries);
	}

	private static class Iter implements Iterator<BlockPos> {
		private int index;
		private int nextIndex;
		private final int capacity;
		private final long[] entries;

		public Iter(int capacity, long[] entries) {
			this.index = Integer.MIN_VALUE;
			this.nextIndex = Integer.MIN_VALUE;
			for (int i = 0; i < capacity; i++) {
				if (entries[i] < EMPTY) {
					this.nextIndex = i;
					break;
				}
			}
			this.capacity = capacity;
			this.entries = entries;
		}

		@Override
		public boolean hasNext() {
			return this.nextIndex != Integer.MIN_VALUE;
		}

		@Override
		public BlockPos next() {
			if (!hasNext()) throw new NoSuchElementException();
			this.index = this.nextIndex;
			do {
				this.nextIndex++;
				if (this.nextIndex >= this.capacity) {
					nextIndex = Integer.MIN_VALUE;
					break;
				}
			} while (this.entries[this.nextIndex] >= EMPTY);
			return BlockPos.of(unhashLong(this.entries[this.index]));
		}

		@Override
		public void remove() {
			this.entries[this.index] = TOMBSTONE;
		}
	}

	/// Returns the same {@link BlockPos.MutableBlockPos} each time {@link Iterator#next} is called.
	public Iterable<BlockPos.MutableBlockPos> iterateMut() {
		return new IterMut(this.capacity, this.entries);
	}

	private static class IterMut implements Iterator<BlockPos.MutableBlockPos>, Iterable<BlockPos.MutableBlockPos> {
		private int index;
		private int nextIndex;
		private final int capacity;
		private final long[] entries;
		private final BlockPos.MutableBlockPos pos;

		public IterMut(int capacity, long[] entries) {
			this.index = Integer.MIN_VALUE;
			this.nextIndex = Integer.MIN_VALUE;
			for (int i = 0; i < capacity; i++) {
				if (entries[i] < EMPTY) {
					this.nextIndex = i;
					break;
				}
			}
			this.capacity = capacity;
			this.entries = entries;
			this.pos = new BlockPos.MutableBlockPos();
		}

		@Override
		public boolean hasNext() {
			return this.nextIndex != Integer.MIN_VALUE;
		}

		@Override
		public BlockPos.MutableBlockPos next() {
			if (!hasNext()) throw new NoSuchElementException();
			this.index = this.nextIndex;
			do {
				this.nextIndex++;
				if (this.nextIndex >= this.capacity) {
					this.nextIndex = Integer.MIN_VALUE;
					break;
				}
			} while (this.entries[this.nextIndex] >= EMPTY);
			this.pos.set(unhashLong(this.entries[this.index]));
			return this.pos;
		}

		@Override
		public void remove() {
			this.entries[this.index] = TOMBSTONE;
		}

		@Override
		public Iterator<BlockPos.MutableBlockPos> iterator() {
			return this;
		}
	}

	/// Compacts the entries array before iterating
	/// Consider using the default iterator if you're going to early out, as this might result in wasted work
	/// Using any other methods on the set after this returns is undefined behavior
	public Iterable<BlockPos> destroyAndIterate() {
		return new OwningIter(this.size, this.entries);
	}

	private static class OwningIter implements Iterable<BlockPos>, Iterator<BlockPos> {
		private int i = 0;
		private final int size;
		private final long[] entries;

		private OwningIter(int size, long[] entries) {
			for (int i = 0, j = 0; j < size; i++) {
				if (entries[i] >= EMPTY) continue;
				entries[j] = entries[i];
				j++;
			}
			this.entries = entries;
			this.size = size;
		}

		@Override
		public boolean hasNext() {
			return i < size;
		}

		@Override
		public BlockPos next() {
			if (this.i >= size) throw new NoSuchElementException();
			long l = unhashLong(entries[this.i]);
			this.i++;
			return BlockPos.of(l);
		}

		public Iterator<BlockPos> iterator() {
			return this;
		}
	}


	/// Compacts the entries array before iterating
	/// Consider using the default iterator if you're going to early out, as this might result in wasted work
	/// Using any other methods on the set after this returns is undefined behavior
	/// Returns the same {@link BlockPos.MutableBlockPos} each time {@link Iterator#next} is called.
	public Iterable<BlockPos.MutableBlockPos> destroyAndIterateMut() {
		return new OwningIterMut(this.size, this.entries);
	}

	private static class OwningIterMut implements Iterable<BlockPos.MutableBlockPos>, Iterator<BlockPos.MutableBlockPos> {
		private int i = 0;
		private final int size;
		private final long[] entries;
		private final BlockPos.MutableBlockPos pos;

		private OwningIterMut(int size, long[] entries) {
			for (int i = 0, j = 0; j < size; i++) {
				if (entries[i] >= EMPTY) continue;
				entries[j] = entries[i];
				j++;
			}
			this.entries = entries;
			this.size = size;
			this.pos = new BlockPos.MutableBlockPos();
		}

		@Override
		public boolean hasNext() {
			return this.i < this.size;
		}

		@Override
		public BlockPos.MutableBlockPos next() {
			if (this.i >= this.size) throw new NoSuchElementException();
			this.pos.set(unhashLong(this.entries[this.i]));
			this.i++;
			return this.pos;
		}

		public Iterator<BlockPos.MutableBlockPos> iterator() {
			return this;
		}
	}
}
