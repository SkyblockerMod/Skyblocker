package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Options;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Options.class)
public class GameOptionsMixin {

	@ModifyReturnValue(method = "dataFix", at = @At("TAIL"))
	private CompoundTag updateSkyblockerKeybinds(CompoundTag nbt) {
		skyblocker$update("wikiLookup.official", nbt);
		skyblocker$update("wikiLookup.fandom", nbt);
		skyblocker$update("hotbarSlotLock", nbt);
		skyblocker$update("itemPriceLookup", nbt);
		skyblocker$update("itemProtection", nbt);
		return nbt;
	}

	@Unique
	private void skyblocker$update(String key, CompoundTag nbt) {
		Tag element = nbt.get("key_key." + key);
		if (element != null && !nbt.contains("key_key.skyblocker." + key)) nbt.put("key_key.skyblocker." + key, element);
	}
}
