package de.hysky.skyblocker.utils.networth;

import de.hysky.skyblocker.utils.ItemUtils;
import net.azureaaron.networth.ItemCalculator;
import net.azureaaron.networth.NetworthResult;
import net.azureaaron.networth.item.SkyblockItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;

public class NetworthCalculator {

	public static NetworthResult getItemNetworth(ItemStack stack) {
		String itemId = ItemUtils.getItemId(stack);
		SkyblockItemStack skyblockItemStack = SkyblockItemStack.of(itemId, stack.getCount(), SkyblockItemMetadata.of(stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt(), itemId));

		return ItemCalculator.calculate(skyblockItemStack, NetworthDataSuppliers::getPrice, NetworthDataSuppliers.getSkyblockItemData());
	}
}
