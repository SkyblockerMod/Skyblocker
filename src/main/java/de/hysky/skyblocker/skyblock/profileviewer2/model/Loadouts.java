package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

public class Loadouts {
	@SerializedName("armor")
	public Armour armour = new Armour();
	public Equipment equipment = new Equipment();

	public static class Armour {
		@SerializedName("1")
		public ArmourLoadout one = new ArmourLoadout();
		@SerializedName("2")
		public ArmourLoadout two = new ArmourLoadout();
		@SerializedName("3")
		public ArmourLoadout three = new ArmourLoadout();
		@SerializedName("4")
		public ArmourLoadout four = new ArmourLoadout();
		@SerializedName("5")
		public ArmourLoadout five = new ArmourLoadout();
		@SerializedName("6")
		public ArmourLoadout six = new ArmourLoadout();
		@SerializedName("7")
		public ArmourLoadout seven = new ArmourLoadout();
		@SerializedName("8")
		public ArmourLoadout eight = new ArmourLoadout();
		@SerializedName("9")
		public ArmourLoadout nine = new ArmourLoadout();
		@SerializedName("10")
		public ArmourLoadout ten = new ArmourLoadout();
		@SerializedName("11")
		public ArmourLoadout eleven = new ArmourLoadout();
		@SerializedName("12")
		public ArmourLoadout twelve = new ArmourLoadout();
		@SerializedName("13")
		public ArmourLoadout thirteen = new ArmourLoadout();
		@SerializedName("14")
		public ArmourLoadout fourteen = new ArmourLoadout();
		@SerializedName("15")
		public ArmourLoadout fifteen = new ArmourLoadout();
		@SerializedName("16")
		public ArmourLoadout sixteen = new ArmourLoadout();
		@SerializedName("17")
		public ArmourLoadout seventeen = new ArmourLoadout();
		@SerializedName("18")
		public ArmourLoadout eighteen = new ArmourLoadout();
		@SerializedName("19")
		public ArmourLoadout nineteen = new ArmourLoadout();
		@SerializedName("20")
		public ArmourLoadout twenty = new ArmourLoadout();
		@SerializedName("21")
		public ArmourLoadout twentyOne = new ArmourLoadout();
		@SerializedName("22")
		public ArmourLoadout twentyTwo = new ArmourLoadout();
		@SerializedName("23")
		public ArmourLoadout twentyThree = new ArmourLoadout();
		@SerializedName("24")
		public ArmourLoadout twentyFour = new ArmourLoadout();
		@SerializedName("25")
		public ArmourLoadout twentyFive = new ArmourLoadout();
		@SerializedName("26")
		public ArmourLoadout twentySix = new ArmourLoadout();
		@SerializedName("27")
		public ArmourLoadout twentySeven = new ArmourLoadout();
		/// The non-zero index of the set equipped
		@SerializedName("equipped_set")
		public @Nullable Integer equippedSet;

		public List<ArmourLoadout> getLoadouts() {
			return List.of(
					this.one,
					this.two,
					this.three,
					this.four,
					this.five,
					this.six,
					this.seven,
					this.eight,
					this.nine,
					this.ten,
					this.eleven,
					this.twelve,
					this.thirteen,
					this.fourteen,
					this.fifteen,
					this.sixteen,
					this.seventeen,
					this.eighteen,
					this.nineteen,
					this.twenty,
					this.twentyOne,
					this.twentyTwo,
					this.twentyThree,
					this.twentyFour,
					this.twentyFive,
					this.twentySix,
					this.twentySeven
					);
		}
	}

	public static class ArmourLoadout {
		public int id;
		@SerializedName("HELMET")
		public Inventories.@Nullable AbstractInventoryContents helmet;
		@SerializedName("CHESTPLATE")
		public Inventories.@Nullable AbstractInventoryContents chestplate;
		@SerializedName("LEGGINGS")
		public Inventories.@Nullable AbstractInventoryContents leggings;
		@SerializedName("BOOTS")
		public Inventories.@Nullable AbstractInventoryContents boots;
	}

	public static class Equipment {
		@SerializedName("1")
		public EquipmentLoadout one = new EquipmentLoadout();
		@SerializedName("2")
		public EquipmentLoadout two = new EquipmentLoadout();
		@SerializedName("3")
		public EquipmentLoadout three = new EquipmentLoadout();
		@SerializedName("4")
		public EquipmentLoadout four = new EquipmentLoadout();
		@SerializedName("5")
		public EquipmentLoadout five = new EquipmentLoadout();
		@SerializedName("6")
		public EquipmentLoadout six = new EquipmentLoadout();
		@SerializedName("7")
		public EquipmentLoadout seven = new EquipmentLoadout();
		@SerializedName("8")
		public EquipmentLoadout eight = new EquipmentLoadout();
		@SerializedName("9")
		public EquipmentLoadout nine = new EquipmentLoadout();
		@SerializedName("10")
		public EquipmentLoadout ten = new EquipmentLoadout();
		@SerializedName("11")
		public EquipmentLoadout eleven = new EquipmentLoadout();
		@SerializedName("12")
		public EquipmentLoadout twelve = new EquipmentLoadout();
		@SerializedName("13")
		public EquipmentLoadout thirteen = new EquipmentLoadout();
		@SerializedName("14")
		public EquipmentLoadout fourteen = new EquipmentLoadout();
		@SerializedName("15")
		public EquipmentLoadout fifteen = new EquipmentLoadout();
		@SerializedName("16")
		public EquipmentLoadout sixteen = new EquipmentLoadout();
		@SerializedName("17")
		public EquipmentLoadout seventeen = new EquipmentLoadout();
		@SerializedName("18")
		public EquipmentLoadout eighteen = new EquipmentLoadout();
		/// The non-zero index of the set equipped
		@SerializedName("equipped_set")
		public @Nullable Integer equippedSet;

		public List<EquipmentLoadout> getLoadouts() {
			return List.of(
					this.one,
					this.two,
					this.three,
					this.four,
					this.five,
					this.six,
					this.seven,
					this.eight,
					this.nine,
					this.ten,
					this.eleven,
					this.twelve,
					this.thirteen,
					this.fourteen,
					this.fifteen,
					this.sixteen,
					this.seventeen,
					this.eighteen
					);
		}
	}

	public static class EquipmentLoadout {
		public int id;
		@SerializedName("EQUIPMENT_SLOT_1")
		public Inventories.@Nullable AbstractInventoryContents necklace;
		@SerializedName("EQUIPMENT_SLOT_2")
		public Inventories.@Nullable AbstractInventoryContents cloak;
		@SerializedName("EQUIPMENT_SLOT_3")
		public Inventories.@Nullable AbstractInventoryContents belt;
		@SerializedName("EQUIPMENT_SLOT_4")
		public Inventories.@Nullable AbstractInventoryContents braceletOrGloves;
	}
}
