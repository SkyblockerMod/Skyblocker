package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GlacitePlayerData {
	@SerializedName("fossil_dust")
	public double fossilDust;
	@SerializedName("mineshafts_entered")
	public int mineshaftsEntered;
	@SerializedName("corpses_looted")
	public CorpsesLooted corpsesLooted = new CorpsesLooted();
	@SerializedName("fossils_donated")
	public List<String> fossilsDonated = new ArrayList<>();

	public static class CorpsesLooted {
		public int lapis;
		public int umber;
		public int tungsten;
		public int vanguard;

		public int getTotalCorpsesLooted() {
			return this.lapis + this.umber + this.tungsten + this.vanguard;
		}
	}

	// TODO finish
	public enum Fossil {
		CLAW("Claw"),
		SPINE("Spine"),
		CLUBBED("Clubbed"),
		UGLY("Ugly"),
		HELIX("Helix"),
		FOOTPRINT("Footprint"),
		WEBBED("Webbed"),
		TUSK("Tusk");

		public final String name;

		Fossil(String name) {
			this.name = name;
		}

		public static List<Fossil> fromDonated(List<String> donated) {
			return Arrays.stream(Fossil.values())
					.filter(fossil -> donated.contains(fossil.name()))
					.sorted(Comparator.comparingInt(Fossil::ordinal))
					.toList();
		}
	}
}
