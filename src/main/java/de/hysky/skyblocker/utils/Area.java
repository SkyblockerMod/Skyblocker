package de.hysky.skyblocker.utils;

import java.util.Arrays;

/**
 * An area of a Skyblock Island.
 */
public sealed interface Area {
	/**
	 * Constant representing all unknown locations.
	 */
	Area UNKNOWN = new Unknown();

	/**
	 * {@return the user-facing name of the area}
	 */
	String displayName();

	@SuppressWarnings("unchecked")
	static Area from(String name) {
		// Uses reflection on the permitted subclasses to find all constants dynamically
		return Arrays.stream((Class<Area>[]) Area.class.getPermittedSubclasses())
				.<Area>mapMulti((clazz, consumer) -> {
					// Implicitly excluding the Unknown record is fine since thats the fallback value anyways
					if (clazz.isEnum()) {
						for (Area area : clazz.getEnumConstants()) {
							consumer.accept(area);
						}
					}
				})
				.filter(area -> area.displayName().equals(name))
				.findFirst()
				.orElse(UNKNOWN);
	}

	/**
	 * @deprecated Use {@link Area#UNKNOWN} instead.
	 */
	@Deprecated
	record Unknown() implements Area {

		@Override
		public String displayName() {
			return "Unknown";
		}
	}

	enum PrivateIsland implements Area {
		YOUR_ISLAND("Your Island");

		private final String displayName;

		PrivateIsland(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String displayName() {
			return this.displayName;
		}
	}

	enum Hub implements Area {
		BANK("Bank"),
		BAZAAR("Bazaar Alley"),
		CARNIVAL("Carnival"),
		FOREST("Forest");

		private final String displayName;

		Hub(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String displayName() {
			return this.displayName;
		}
	}

	enum TheFarmingIslands implements Area {
		GLOWING_MUSHROOM_CAVE("Glowing Mushroom Cave");

		private final String displayName;

		TheFarmingIslands(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String displayName() {
			return this.displayName;
		}
	}

	enum Galatea implements Area {
		FOREST_TEMPLE("Forest Temple");

		private final String displayName;

		Galatea(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String displayName() {
			return this.displayName;
		}
	}

	enum TheEnd implements Area {
		DRAGONS_NEST("Dragon's Nest"),
		THE_END("The End");

		private final String displayName;

		TheEnd(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String displayName() {
			return this.displayName;
		}
	}

	enum CrimsonIsle implements Area {
		DOJO("Dojo");

		private final String displayName;

		CrimsonIsle(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String displayName() {
			return this.displayName;
		}
	}

	enum DwarvenMines implements Area {
		DWARVEN_BASE_CAMP("Dwarven Base Camp"),
		GLACITE_MINESHAFTS("Glacite Mineshafts"),
		GLACITE_TUNNELS("Glacite Tunnels"),
		GREAT_GLACITE_LAKE("Great Glacite Lake");

		private final String displayName;

		DwarvenMines(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String displayName() {
			return this.displayName;
		}
	}

	enum CrystalHollows implements Area {
		MINES_OF_DIVAN("Mines of Divan"),
		JUNGLE_TEMPLE("Jungle Temple");

		private final String displayName;

		CrystalHollows(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String displayName() {
			return this.displayName;
		}
	}

	enum TheRift implements Area {
		CHATEAU("Stillgore Château"),
		MIRRORVERSE("Mirrorverse");

		private final String displayName;

		TheRift(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String displayName() {
			return this.displayName;
		}
	}
}
