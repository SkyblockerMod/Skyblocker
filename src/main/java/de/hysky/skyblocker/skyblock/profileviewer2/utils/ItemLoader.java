package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.skyblock.profileviewer2.model.Inventories;
import de.hysky.skyblocker.skyblock.profileviewer2.model.Loadouts;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.datafixer.LegacyItemStackFixer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

public class ItemLoader {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int INVENTORY_SIZE = 36;

	public static CompletableFuture<ProfileItemStorage> decodeItems(ProfileMember member) {
		return CompletableFuture.supplyAsync(() -> decodeItemsInternal(member), Executors.newVirtualThreadPerTaskExecutor());
	}

	private static ProfileItemStorage decodeItemsInternal(ProfileMember member) {
		List<ItemStack> inventory = member.inventories.inventoryContents != null ? fixHotbar(decode(member.inventories.inventoryContents.data)) : List.of();
		List<ItemStack> armour = member.inventories.armourContents != null ? decode(member.inventories.armourContents.data).reversed() : List.of();
		List<ItemStack> equipment = member.inventories.equipmentContents != null ? decode(member.inventories.equipmentContents.data) : List.of();
		List<ItemStack> enderChest = member.inventories.enderChestContents != null ? decode(member.inventories.enderChestContents.data) : List.of();
		List<ItemStack> accessories = member.inventories.bagContents.talismanBag != null ? decode(member.inventories.bagContents.talismanBag.data) : List.of();

		// The tree map will automatically sort the stringified number keys so the iteration order will be correct for free!
		TreeMap<Integer, ProfileItemStorage.Backpack> backpacks = new TreeMap<>();

		for (Map.Entry<String, Inventories.AbstractInventoryContents> entry : member.inventories.backpackContents.entrySet()) {
			ItemStack icon = ItemStack.EMPTY;
			List<ItemStack> contents = decode(entry.getValue().data);

			if (member.inventories.backpackIcons.containsKey(entry.getKey())) {
				List<ItemStack> iconContents = decode(member.inventories.backpackIcons.get(entry.getKey()).data);

				// Choose the first item as the icon
				if (!iconContents.isEmpty()) {
					icon = iconContents.getFirst();
				}
			}

			backpacks.put(Integer.valueOf(entry.getKey()), new ProfileItemStorage.Backpack(icon, contents));
		}

		List<ItemStack> armourSets = new ArrayList<>();

		for (Loadouts.ArmourLoadout loadout : member.loadouts.armour.getLoadouts()) {
			List<ItemStack> helmet = loadout.helmet != null ? decode(loadout.helmet.data) : List.of(ItemStack.EMPTY);
			List<ItemStack> chestplate = loadout.chestplate != null ? decode(loadout.chestplate.data) : List.of(ItemStack.EMPTY);
			List<ItemStack> leggings = loadout.leggings != null ? decode(loadout.leggings.data) : List.of(ItemStack.EMPTY);
			List<ItemStack> boots = loadout.boots != null ? decode(loadout.boots.data) : List.of(ItemStack.EMPTY);

			armourSets.add(helmet.getFirst());
			armourSets.add(chestplate.getFirst());
			armourSets.add(leggings.getFirst());
			armourSets.add(boots.getFirst());
		}

		// When a wardrobe slot is selected the loadout has null for the pieces so we need to put them in the slots
		if (member.loadouts.armour.equippedSet != null) {
			// Note: The equipped slot is not zero-indexed
			int startingIndex = Math.min((member.loadouts.armour.equippedSet - 1) * 4, armourSets.size());

			armourSets.set(startingIndex + 0, armour.getFirst());
			armourSets.set(startingIndex + 1, armour.get(1));
			armourSets.set(startingIndex + 2, armour.get(2));
			armourSets.set(startingIndex + 3, armour.getLast());
		}

		List<ItemStack> equipmentSets = new ArrayList<>();

		for (Loadouts.EquipmentLoadout loadout : member.loadouts.equipment.getLoadouts()) {
			List<ItemStack> necklace = loadout.necklace != null ? decode(loadout.necklace.data) : List.of(ItemStack.EMPTY);
			List<ItemStack> cloak = loadout.cloak != null ? decode(loadout.cloak.data) : List.of(ItemStack.EMPTY);
			List<ItemStack> belt = loadout.belt != null ? decode(loadout.belt.data) : List.of(ItemStack.EMPTY);
			List<ItemStack> braceletOrGloves = loadout.braceletOrGloves != null ? decode(loadout.braceletOrGloves.data) : List.of(ItemStack.EMPTY);

			equipmentSets.add(necklace.getFirst());
			equipmentSets.add(cloak.getFirst());
			equipmentSets.add(belt.getFirst());
			equipmentSets.add(braceletOrGloves.getFirst());
		}

		// Insert the equipped equipment back into its original place (same as is done for armour)
		if (member.loadouts.equipment.equippedSet != null) {
			// Note: The equipped slot is not zero-indexed
			int startingIndex = Math.min((member.loadouts.equipment.equippedSet - 1) * 4, equipmentSets.size());

			equipmentSets.set(startingIndex + 0, equipment.getFirst());
			equipmentSets.set(startingIndex + 1, equipment.get(1));
			equipmentSets.set(startingIndex + 2, equipment.get(2));
			equipmentSets.set(startingIndex + 3, equipment.getLast());
		}

		return new ProfileItemStorage(inventory, armour, equipment, enderChest, backpacks, List.copyOf(armourSets), List.copyOf(equipmentSets), PetLoader.parsePets(member.petsData.pets), new ProfileItemStorage.Bags(accessories));
	}

	public static List<ItemStack> decode(String itemData) {
		try {
			byte[] gzippedData = Base64.getDecoder().decode(itemData);
			CompoundTag decoded = NbtIo.readCompressed(new ByteArrayInputStream(gzippedData), NbtAccounter.unlimitedHeap());
			ListTag items = decoded.getListOrEmpty("i");

			List<ItemStack> stacks = new ArrayList<>();

			for (int i = 0; i < items.size(); i++) {
				CompoundTag tag = items.getCompoundOrEmpty(i);

				// Check if the item is air (id 0) and if so add an empty stack. Most empty slots will likely be empty compound tags
				// so this avoids the need to process them.
				if (tag.getIntOr("id", 0) == 0) {
					stacks.add(ItemStack.EMPTY);
					continue;
				}

				ItemStack stack = LegacyItemStackFixer.fixLegacyStack(tag, ItemStack.CODEC);

				// Add a placeholder if the stack failed to load
				if (stack.isEmpty()) {
					String name = "Error: " + tag.getCompoundOrEmpty("tag").getCompoundOrEmpty("ExtraAttributes").getStringOr("id", "");
					ItemStack placeholder = ItemUtils.getNamedPlaceholder(name).getStackOrThrow();

					stacks.add(placeholder);
					continue;
				}

				// FIXME old pv v1 had some pet logic here, might not be needed though since I imagine pets in the API have full lore etc

				// Attach an override for Aaron's Mod so that these item stacks will work with the mod's features even when not in SkyBlock
				if (stack.has(DataComponents.CUSTOM_DATA)) {
					CompoundTag customData = ItemUtils.getCustomData(stack);
					customData.put("aaron-mod", Util.make(new CompoundTag(), comp -> comp.putBoolean("alwaysDisplaySkyblockInfo", true)));
				}

				stacks.add(stack);
			}

			// Ensure the returned list is immutable
			return List.copyOf(stacks);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profile Viewer Item Loader] Failed to decode items.", e);
		}

		return List.of();
	}

	/// By default the hot bar items are at the front of the list and this pushes them to the back so that it is easier
	/// to work with inside of the {@code InventoryWidget}.
	private static List<ItemStack> fixHotbar(List<ItemStack> inventory) {
		List<ItemStack> fixedInventory = new ArrayList<>();
		fixedInventory.addAll(inventory.subList(9, INVENTORY_SIZE));
		fixedInventory.addAll(inventory.subList(0, 9));

		return List.copyOf(fixedInventory);
	}
}
