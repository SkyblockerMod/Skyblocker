package de.hysky.skyblocker.skyblock.dwarven.fossilSolver;

import java.util.List;

public enum FossilTypes {
	CLAW(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY},
			{Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.FLIP_ROTATED_90, Structures.transformationOptions.FLIP_ROTATED_180), "7.7", 13, "Claw"),
	TUSK(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_90, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.FLIP_ROTATED_270), "12.5", 8, "Tusk"),
	UGLY(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_90, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.ROTATED_270), "6.2", 16, "Ugly"),
	HELIX(new Structures.tileState[][]{
			{Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL},
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_90, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.ROTATED_270), "7.1", 14, "Helix"),
	WEBBED(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY},
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.FLIP_ROTATED_0), "10", 10, "Webbed"),
	FOOTPRINT(new Structures.tileState[][]{
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_90, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.ROTATED_270), "7.7", 13, "Footprint"),
	CLUBBED(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL},
			{Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.EMPTY}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.FLIP_ROTATED_0, Structures.transformationOptions.FLIP_ROTATED_180), "9.1", 11, "Clubbed"),
	SPINE(new Structures.tileState[][]{
			{Structures.tileState.EMPTY, Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY, Structures.tileState.EMPTY},
			{Structures.tileState.EMPTY, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.EMPTY},
			{Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL, Structures.tileState.FOSSIL}
	}, List.of(Structures.transformationOptions.ROTATED_0, Structures.transformationOptions.ROTATED_90, Structures.transformationOptions.ROTATED_180, Structures.transformationOptions.ROTATED_270), "8.3", 12, "Spine");

	final List<Structures.transformationOptions> rotations;
	final Structures.tileState[][] grid;
	final String percentage;
	final int tileCount;
	final String name;


	FossilTypes(Structures.tileState[][] grid, List<Structures.transformationOptions> rotations, String percentage, int tileCount, String name) {
		this.grid = grid;
		this.rotations = rotations;
		this.percentage = percentage;
		this.tileCount = tileCount;
		this.name = name;
	}
}
