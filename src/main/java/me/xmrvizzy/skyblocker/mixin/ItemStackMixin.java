package me.xmrvizzy.skyblocker.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import me.xmrvizzy.skyblocker.config.SkyblockerConfigManager;
import me.xmrvizzy.skyblocker.utils.ItemUtils;
import me.xmrvizzy.skyblocker.utils.ItemUtils.Durability;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow
	@Nullable
	private NbtCompound nbt;

	@ModifyReturnValue(method = "getName", at = @At("RETURN"))
	private Text skyblocker$customItemNames(Text original) {
		if (Utils.isOnSkyblock() && nbt != null && nbt.contains("ExtraAttributes"))  {
			NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
			String itemUuid = extraAttributes.contains("uuid") ? extraAttributes.getString("uuid") : null;

			return SkyblockerConfigManager.get().general.customItemNames.getOrDefault(itemUuid, original);
		}

		return original;
	}

	@ModifyReturnValue(method = "getDamage", at = @At("RETURN"))
	private int skyblocker$handlePickoDrillDamage(int original) {
		Durability dur = ItemUtils.getDurability((ItemStack) (Object) this);
		if (dur != null) {
			return dur.max() - dur.current();
		}
		return original;
	}

	@ModifyReturnValue(method = "getMaxDamage", at = @At("RETURN"))
	private int skyblocker$handlePickoDrillMaxDamage(int original) {
		Durability dur = ItemUtils.getDurability((ItemStack) (Object) this);
		if (dur != null) {
			return dur.max();
		}
		return original;
	}

	@ModifyReturnValue(method = "isDamageable", at = @At("RETURN"))
	private boolean skyblocker$handlePickoDrillDamageable(boolean original) {
		Durability dur = ItemUtils.getDurability((ItemStack) (Object) this);
		if (dur != null) {
			return true;
		}
		return original;
	}
}
