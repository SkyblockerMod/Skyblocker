package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.option.GameOptions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameOptions.class)
public class GameOptionsMixin {

	@ModifyReturnValue(method = "update", at = @At("TAIL"))
	private NbtCompound updateSkyblockerKeybinds(NbtCompound nbt) {
		skyblocker$update("wikiLookup.official", nbt);
		skyblocker$update("wikiLookup.fandom", nbt);
		skyblocker$update("hotbarSlotLock", nbt);
		skyblocker$update("itemPriceLookup", nbt);
		skyblocker$update("itemProtection", nbt);
		return nbt;
	}

	@Unique
	private void skyblocker$update(String key, NbtCompound nbt) {
		NbtElement element = nbt.get("key_key." + key);
		if (element != null && !nbt.contains("key_key.skyblocker." + key)) nbt.put("key_key.skyblocker." + key, element);
	}
}
