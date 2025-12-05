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
		update("wikiLookup.official", nbt);
		update("wikiLookup.fandom", nbt);
		update("hotbarSlotLock", nbt);
		update("itemPriceLookup", nbt);
		update("itemProtection", nbt);
		return nbt;
	}

	@Unique
	private void update(String key, NbtCompound nbt) {
		NbtElement element = nbt.get("key_key." + key);
		if (element != null && !nbt.contains("key_key.skyblocker." + key)) nbt.put("key_key.skyblocker." + key, element);
	}
}
