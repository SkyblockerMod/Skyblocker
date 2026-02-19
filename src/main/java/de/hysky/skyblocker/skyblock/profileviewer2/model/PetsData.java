package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;

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
		public transient @Nullable PetInfo petInfo;

		public PetInfo toPetInfo() {
			if (this.petInfo == null) {
				this.petInfo = new PetInfo(Optional.empty(), this.type, this.exp, SkyblockItemRarity.valueOf(this.tier), Optional.ofNullable(this.uuid).map(UUID::toString), Optional.ofNullable(this.heldItem), Optional.ofNullable(this.skin));
			}

			return this.petInfo;
		}
	}
}
