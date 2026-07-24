package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

import de.hysky.skyblocker.annotations.GenToString;

public class PetsData {
	@SerializedName("pet_care")
	public PetCare petCare = new PetCare();
	public List<Pet> pets = List.of();

	public static class PetCare {
		@SerializedName("pet_types_sacrificed")
		public List<String> petTypesSacrificed = List.of();
	}

	public static class Pet {
		public @Nullable UUID uuid;
		public String type = "";
		public double exp;
		public String tier = "";
		public @Nullable String heldItem;
		public int candyUsed;
		public @Nullable String skin;

		@GenToString
		@Override
		public native String toString();
	}
}
