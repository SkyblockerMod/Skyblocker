package de.hysky.skyblocker.utils.networth;

import com.mojang.serialization.Dynamic;

import de.hysky.skyblocker.utils.ItemUtils;
import net.azureaaron.networth.ItemCalculator;
import net.azureaaron.networth.NetworthResult;
import net.azureaaron.networth.item.SkyblockItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;

public class NetworthCalculator {
	public static NetworthResult getItemNetworth(ItemStack stack) {
		return getItemNetworth(stack, stack.getCount());
	}

	public static NetworthResult getItemNetworth(ItemStack stack, int count) {
		String itemId = ItemUtils.getItemId(stack);
		NbtCompound customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		Dynamic<NbtElement> customDataDynamic = new Dynamic<>(NbtOps.INSTANCE, customData);
		SkyblockItemStack skyblockItemStack = SkyblockItemStack.of(itemId, count, customDataDynamic, SkyblockItemMetadataRetriever.of(customData, itemId));

		return ItemCalculator.calculate(skyblockItemStack, NetworthDataSuppliers::getPrice, NetworthDataSuppliers.getSkyblockItemData());
	}
}
