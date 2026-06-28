package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import java.util.List;
import java.util.TreeMap;

import net.minecraft.world.item.ItemStack;

public record ProfileItemStorage(List<ItemStack> inventory, List<ItemStack> armour, List<ItemStack> equipment, List<ItemStack> enderChestContents, TreeMap<Integer, Backpack> backpacks, List<ItemStack> wardrobe, Bags bags) {
	public record Backpack(ItemStack icon, List<ItemStack> contents) {}

	public record Bags(List<ItemStack> accessories) {}
}
