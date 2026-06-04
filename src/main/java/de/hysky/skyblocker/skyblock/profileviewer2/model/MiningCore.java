package de.hysky.skyblocker.skyblock.profileviewer2.model;

import org.jspecify.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

public class MiningCore {
	@SerializedName("powder_mithril")
	public long mithrilPowder;
	@SerializedName("powder_gemstone")
	public long gemstonePowder;
	@SerializedName("powder_glacite")
	public long glacitePowder;
	public Crystals crystals = new Crystals();

	public static class Crystals {
		@SerializedName("jade_crystal")
		public CrystalData jadeCrystal = new CrystalData();
		@SerializedName("amber_crystal")
		public CrystalData amberCrystal = new CrystalData();
		@SerializedName("topaz_crystal")
		public CrystalData topazCrystal = new CrystalData();
		@SerializedName("sapphire_crystal")
		public CrystalData sapphireCrystal = new CrystalData();
		@SerializedName("amethyst_crystal")
		public CrystalData amethystCrystal = new CrystalData();
		@SerializedName("jasper_crystal")
		public CrystalData jasperCrystal = new CrystalData();
		@SerializedName("ruby_crystal")
		public CrystalData rubyCrystal = new CrystalData();
		@SerializedName("citrine_crystal")
		public CrystalData citrineCrystal = new CrystalData();
		@SerializedName("peridot_crystal")
		public CrystalData peridotCrystal = new CrystalData();
		@SerializedName("aquamarine_crystal")
		public CrystalData aquamarineCrystal = new CrystalData();
		@SerializedName("onyx_crystal")
		public CrystalData onyxCrystal = new CrystalData();
		@SerializedName("opal_crystal")
		public CrystalData opalCrystal = new CrystalData();

		public static class CrystalData {
			public String state = "";
			@SerializedName("total_placed")
			public @Nullable Integer totalPlaced;
			@SerializedName("total_found")
			public int totalFound;
		}
	}
}
