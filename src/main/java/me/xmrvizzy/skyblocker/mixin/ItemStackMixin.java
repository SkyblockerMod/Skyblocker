package me.xmrvizzy.skyblocker.mixin;

import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Shadow
	@Nullable
	private NbtCompound nbt;

	@ModifyReturnValue(method = "getName", at = @At("RETURN"))
	public Text skyblocker$customItemNames(Text original) {
		if (Utils.isOnSkyblock() && nbt != null && nbt.contains("ExtraAttributes"))  {
			Map<String, Text> customItemNames = SkyblockerConfig.get().general.customItemNames;
			NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
			String itemUuid =  extraAttributes.contains("uuid") ? extraAttributes.getString("uuid") : null;
			
			if (itemUuid != null && customItemNames.containsKey(itemUuid)) return customItemNames.get(itemUuid);
		}
		
		return original;
	}
}
