package me.xmrvizzy.skyblocker.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(Item.class)
public abstract class ItemMixin {
	@WrapOperation(
		method = {"getItemBarColor", "getItemBarStep"},
		at = @At(value = "FIELD", target = "Lnet/minecraft/item/Item;maxDamage:I", opcode = Opcodes.GETFIELD)
	)
	private int skyblocker$handlePickoDrillBar(Item item, Operation<Integer> original, ItemStack stack) {
		return stack.getMaxDamage();
	}
}
