package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.Nullable;

public class MiningCore {
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

	@SerializedName("powder_mithril")
	public long mithrilPowder;
	@SerializedName("powder_spent_mithril")
	public long mithrilPowderSpent1;
	@SerializedName("powder_spent_non_refundable_mithril")
	public long mithrilPowderSpentNonRefundable1;
	@SerializedName("powder_spent_mithril_2")
	public long mithrilPowderSpent2;
	@SerializedName("powder_spent_non_refundable_mithril_2")
	public long mithrilPowderSpentNonRefundable2;
	@SerializedName("powder_spent_mithril_3")
	public long mithrilPowderSpent3;
	@SerializedName("powder_spent_non_refundable_mithril_3")
	public long mithrilPowderSpentNonRefundable3;
	@SerializedName("powder_spent_mithril_4")
	public long mithrilPowderSpent4;
	@SerializedName("powder_spent_non_refundable_mithril_4")
	public long mithrilPowderSpentNonRefundable4;
	@SerializedName("powder_spent_mithril_5")
	public long mithrilPowderSpent5;
	@SerializedName("powder_spent_non_refundable_mithril_5")
	public long mithrilPowderSpentNonRefundable5;

	@SerializedName("powder_gemstone")
	public long gemstonePowder;
	@SerializedName("powder_spent_gemstone")
	public long gemstonePowderSpent1;
	@SerializedName("powder_spent_non_refundable_gemstone")
	public long gemstonePowderSpentNonRefundable1;
	@SerializedName("powder_spent_gemstone_2")
	public long gemstonePowderSpent2;
	@SerializedName("powder_spent_non_refundable_gemstone_2")
	public long gemstonePowderSpentNonRefundable2;
	@SerializedName("powder_spent_gemstone_3")
	public long gemstonePowderSpent3;
	@SerializedName("powder_spent_non_refundable_gemstone_3")
	public long gemstonePowderSpentNonRefundable3;
	@SerializedName("powder_spent_gemstone_4")
	public long gemstonePowderSpent4;
	@SerializedName("powder_spent_non_refundable_gemstone_4")
	public long gemstonePowderSpentNonRefundable4;
	@SerializedName("powder_spent_gemstone_5")
	public long gemstonePowderSpent5;
	@SerializedName("powder_spent_non_refundable_gemstone_5")
	public long gemstonePowderSpentNonRefundable5;

	@SerializedName("powder_glacite")
	public long glacitePowder;
	@SerializedName("powder_spent_glacite")
	public long glacitePowderSpent1;
	@SerializedName("powder_spent_non_refundable_glacite")
	public long glacitePowderSpentNonRefundable1;
	@SerializedName("powder_spent_glacite_2")
	public long glacitePowderSpent2;
	@SerializedName("powder_spent_non_refundable_glacite_2")
	public long glacitePowderSpentNonRefundable2;
	@SerializedName("powder_spent_glacite_3")
	public long glacitePowderSpent3;
	@SerializedName("powder_spent_non_refundable_glacite_3")
	public long glacitePowderSpentNonRefundable3;
	@SerializedName("powder_spent_glacite_4")
	public long glacitePowderSpent4;
	@SerializedName("powder_spent_non_refundable_glacite_4")
	public long glacitePowderSpentNonRefundable4;
	@SerializedName("powder_spent_glacite_5")
	public long glacitePowderSpent5;
	@SerializedName("powder_spent_non_refundable_glacite_5")
	public long glacitePowderSpentNonRefundable5;
}
