package de.hysky.skyblocker.skyblock.dwarven.fossilSolver;

import java.util.List;

public enum fossilTypes {
	CLAW(new structures.tileState[][]{
			{structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY},
			{structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY},
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY},
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL},
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY}
	}, List.of(structures.transformationOptions.values()), "7.7", 14),
	TUSK(new structures.tileState[][]{
			{structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL},
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL},
			{structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL},
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY},
			{structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY}
	}, List.of(structures.transformationOptions.values()), "12.5", 8),
	UGLY(new structures.tileState[][]{
			{structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY},
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY},
			{structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL},
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY}
	}, List.of(structures.transformationOptions.ROTATED_0, structures.transformationOptions.ROTATED_90, structures.transformationOptions.ROTATED_180, structures.transformationOptions.ROTATED_270), "6.2", 16),
	HELIX(new structures.tileState[][]{
			{structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL}, // helix
			{structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL},
			{structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL},
			{structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL}
	}, List.of(structures.transformationOptions.values()), "7.1", 14),
	WEBBED(new structures.tileState[][]{
			{structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY}, // webbed fossil
			{structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL},
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY},
			{structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY}
	}, List.of(structures.transformationOptions.ROTATED_0, structures.transformationOptions.FLIP_ROTATED_0), "10", 10),
	FOOTPRINT(new structures.tileState[][]{
			{structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL}, // footprint fossil
			{structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.FOSSIL},
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY},
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY},
			{structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY}
	}, List.of(structures.transformationOptions.ROTATED_0, structures.transformationOptions.ROTATED_90, structures.transformationOptions.ROTATED_180, structures.transformationOptions.ROTATED_270), "7.7", 13),
	CLUBBED(new structures.tileState[][]{
			{structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL}, // clubbed fossil
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL},
			{structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY},
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.EMPTY}
	}, List.of(structures.transformationOptions.ROTATED_0, structures.transformationOptions.ROTATED_180, structures.transformationOptions.FLIP_ROTATED_0, structures.transformationOptions.FLIP_ROTATED_180), "9.1", 11),
	SPINE(new structures.tileState[][]{
			{structures.tileState.EMPTY, structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY, structures.tileState.EMPTY}, // spine fossil
			{structures.tileState.EMPTY, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.EMPTY},
			{structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL, structures.tileState.FOSSIL}
	}, List.of(structures.transformationOptions.ROTATED_0, structures.transformationOptions.ROTATED_90, structures.transformationOptions.ROTATED_180, structures.transformationOptions.ROTATED_270), "8.3", 12);

	final List<structures.transformationOptions> rotations;
	final structures.tileState[][] grid;
	final String percentage;
	final int tileCount;

	fossilTypes(structures.tileState[][] grid, List<structures.transformationOptions> rotations, String percentage, int tileCount) {
		this.grid = grid;
		this.rotations = rotations;
		this.percentage = percentage;
		this.tileCount = tileCount; //todo just have tile count
	}
}
