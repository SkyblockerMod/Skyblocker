package de.hysky.skyblocker.utils.datafixer;

import com.mojang.serialization.Dynamic;

import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;

/**
 * Contains a data fixer to convert legacy item NBT to the new components system.
 * 
 * @see {@link net.minecraft.datafixer.fix.ItemStackComponentizationFix}
 */
public class ItemStackComponentizationFixer {
	private static final int ITEM_NBT_DATA_VERSION = 3817;
	private static final int ITEM_COMPONENTS_DATA_VERSION = 3818;

	public static ItemStack fixUpItem(NbtCompound nbt) {
		Dynamic<NbtElement> dynamic = Schemas.getFixer().update(TypeReferences.ITEM_STACK, new Dynamic<NbtElement>(NbtOps.INSTANCE, nbt), ITEM_NBT_DATA_VERSION, ITEM_COMPONENTS_DATA_VERSION);

		return ItemStack.CODEC.parse(dynamic).result().orElseThrow();
	}
}
