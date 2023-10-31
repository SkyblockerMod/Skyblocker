package de.hysky.skyblocker.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public abstract class ItemMixin {
	@Redirect(
		method = {"getItemBarColor", "getItemBarStep"},
		at = @At(value = "FIELD", target = "Lnet/minecraft/item/Item;maxDamage:I", opcode = Opcodes.GETFIELD)
	)
	private int skyblocker$handlePickoDrillBar(Item item, ItemStack stack) {
		return stack.getMaxDamage();
	}
}
