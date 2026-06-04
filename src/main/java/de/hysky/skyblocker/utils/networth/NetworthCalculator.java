package de.hysky.skyblocker.utils.networth;

import com.mojang.serialization.Dynamic;
import net.azureaaron.networth.ItemCalculator;
import net.azureaaron.networth.NetworthResult;
import net.azureaaron.networth.item.SkyblockItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class NetworthCalculator {
	public static NetworthResult getItemNetworth(ItemStack stack) {
		return getItemNetworth(stack, stack.getCount());
	}

	public static NetworthResult getItemNetworth(ItemStack stack, int count) {
		String itemId = stack.getSkyblockId();
		CompoundTag customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		Dynamic<Tag> customDataDynamic = new Dynamic<>(NbtOps.INSTANCE, customData);
		SkyblockItemStack skyblockItemStack = SkyblockItemStack.of(itemId, count, customDataDynamic, SkyblockItemMetadataRetriever.of(customData, itemId));

		return ItemCalculator.calculate(skyblockItemStack, NetworthDataSuppliers::getPrice, NetworthDataSuppliers.getSkyblockItemData());
	}
}
