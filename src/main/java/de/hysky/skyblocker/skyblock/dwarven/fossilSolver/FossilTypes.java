package de.hysky.skyblocker.skyblock.dwarven.fossilSolver;

import java.util.List;

public enum FossilTypes {
	CLAW(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY},
			{Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.FLIP_ROTATED_90, Structures.transformationOptions.FLIP_ROTATED_180), "7.7", 13),
	TUSK(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_90, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.FLIP_ROTATED_270), "12.5", 8),
	UGLY(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_90, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.ROTATED_270), "6.2", 16),
	HELIX(new Structures.tileState[][]{
			{Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL}, // helix
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_90, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.ROTATED_270), "7.1", 14),
	WEBBED(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY}, // webbed fossil
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.FLIP_ROTATED_0), "10", 10),
	FOOTPRINT(new Structures.tileState[][]{
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL}, // footprint fossil
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_90, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.ROTATED_270), "7.7", 13),
	CLUBBED(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL}, // clubbed fossil
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL},
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.FLIP_ROTATED_0, Structures.transformationOptions.FLIP_ROTATED_180), "9.1", 11),
	SPINE(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY}, // spine fossil
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_90, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.ROTATED_270), "8.3", 12);

	final List<Structures.transformationOptions> rotations;
	final Structures.tileState[][] grid;
	final String percentage;
	final int tileCount;

	FossilTypes(Structures.tileState[][] grid, List<Structures.transformationOptions> rotations, String percentage, int tileCount) {
		this.grid = grid;
		this.rotations = rotations;
		this.percentage = percentage;
		this.tileCount = tileCount; //todo just have tile count
	}
}
