package de.hysky.skyblocker.skyblock.dwarven.fossil;

import java.util.List;

/**
 * All the possible fossils and there transformations
 */
public enum FossilTypes {
	CLAW(new Structures.TileState[][]{
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY},
			{Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL},
			{Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY}
	}, List.of(Structures.TransformationOptions.ROTATED_0, Structures.TransformationOptions.ROTATED_180, Structures.TransformationOptions.FLIP_ROTATED_90, Structures.TransformationOptions.FLIP_ROTATED_180), "7.7", 13, "Claw"),
	TUSK(new Structures.TileState[][]{
			{Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL},
			{Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY},
			{Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY}
	}, List.of(Structures.TransformationOptions.ROTATED_0, Structures.TransformationOptions.ROTATED_90, Structures.TransformationOptions.ROTATED_180, Structures.TransformationOptions.FLIP_ROTATED_270), "12.5", 8, "Tusk"),
	UGLY(new Structures.TileState[][]{
			{Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.EMPTY},
			{Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.EMPTY}
	}, List.of(Structures.TransformationOptions.ROTATED_0, Structures.TransformationOptions.ROTATED_90, Structures.TransformationOptions.ROTATED_180, Structures.TransformationOptions.ROTATED_270), "6.2", 16, "Ugly"),
	HELIX(new Structures.TileState[][]{
			{Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL},
			{Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL},
			{Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL},
			{Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL}
	}, List.of(Structures.TransformationOptions.ROTATED_0, Structures.TransformationOptions.ROTATED_90, Structures.TransformationOptions.ROTATED_180, Structures.TransformationOptions.ROTATED_270), "7.1", 14, "Helix"),
	WEBBED(new Structures.TileState[][]{
			{Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY},
			{Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY},
			{Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY}
	}, List.of(Structures.TransformationOptions.ROTATED_0, Structures.TransformationOptions.FLIP_ROTATED_0), "10", 10, "Webbed"),
	FOOTPRINT(new Structures.TileState[][]{
			{Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL},
			{Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.FOSSIL},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.EMPTY},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.EMPTY},
			{Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY}
	}, List.of(Structures.TransformationOptions.ROTATED_0, Structures.TransformationOptions.ROTATED_90, Structures.TransformationOptions.ROTATED_180, Structures.TransformationOptions.ROTATED_270), "7.7", 13, "Footprint"),
	CLUBBED(new Structures.TileState[][]{
			{Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL},
			{Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.EMPTY}
	}, List.of(Structures.TransformationOptions.ROTATED_0, Structures.TransformationOptions.ROTATED_180, Structures.TransformationOptions.FLIP_ROTATED_0, Structures.TransformationOptions.FLIP_ROTATED_180), "9.1", 11, "Clubbed"),
	SPINE(new Structures.TileState[][]{
			{Structures.TileState.EMPTY, Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.EMPTY, Structures.TileState.EMPTY},
			{Structures.TileState.EMPTY, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.EMPTY},
			{Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL, Structures.TileState.FOSSIL}
	}, List.of(Structures.TransformationOptions.ROTATED_0, Structures.TransformationOptions.ROTATED_90, Structures.TransformationOptions.ROTATED_180, Structures.TransformationOptions.ROTATED_270), "8.3", 12, "Spine");

	final List<Structures.TransformationOptions> rotations;
	final Structures.TileState[][] grid;
	final String percentage;
	final int tileCount;
	final String name;


	FossilTypes(Structures.TileState[][] grid, List<Structures.TransformationOptions> rotations, String percentage, int tileCount, String name) {
		this.grid = grid;
		this.rotations = rotations;
		this.percentage = percentage;
		this.tileCount = tileCount;
		this.name = name;
	}
}
