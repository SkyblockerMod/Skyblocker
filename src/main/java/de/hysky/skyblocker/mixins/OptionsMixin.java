package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Options;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Options.class)
public class OptionsMixin {

	@ModifyReturnValue(method = "dataFix", at = @At("TAIL"))
	private CompoundTag updateSkyblockerKeybinds(CompoundTag nbt) {
		skyblocker$update("wikiLookup.fandom", nbt);
		skyblocker$migrate("wikiLookup.fandom", "wikiLookup.independent", nbt);
		skyblocker$update("hotbarSlotLock", nbt);
		skyblocker$update("itemPriceLookup", nbt);
		skyblocker$update("itemProtection", nbt);
		migrateToLoadoutKeybinds(nbt);

		return nbt;
	}

	@Unique
	private void skyblocker$update(String key, CompoundTag nbt) {
		Tag element = nbt.get("key_key." + key);
		if (element != null && !nbt.contains("key_key.skyblocker." + key)) nbt.put("key_key.skyblocker." + key, element);
	}

	@Unique
	private void skyblocker$migrate(String oldKey, String newKey, CompoundTag nbt) {
		String newEntry = "key_key.skyblocker." + newKey;
		if (nbt.contains(newEntry)) return;

		Tag element = nbt.get("key_key.skyblocker." + oldKey);
		if (element != null) nbt.put(newEntry, element);
	}

	@Unique
	private void migrateToLoadoutKeybinds(CompoundTag nbt) {
		Set<String> usedLoadoutKeys = new HashSet<>();

		// Copy hotbar keys to loadout keys
		for (int i = 1; i <= 9; i++) {
			String numberString = i < 10 ? "0" + String.valueOf(i) : String.valueOf(i);
			String loadoutKey = "key_key.skyblocker.loadout." + numberString;
			String hotbarKey = "key_key.hotbar." + i;

			if (!nbt.contains(loadoutKey) && nbt.contains(hotbarKey)) {
				nbt.put(loadoutKey, nbt.get(hotbarKey));
				usedLoadoutKeys.add(nbt.get(hotbarKey).asString().get());
			}
		}

		// Reset keys for loadouts 10, 11, and 12 if they conflict with a hotbar key
		Map<String, String> newLoadoutKeys = Map.of(
				"key_key.skyblocker.loadout.10", "key.keyboard.0",
				"key_key.skyblocker.loadout.11", "key.keyboard.minus",
				"key_key.skyblocker.loadout.12", "key.keyboard.equal"
				);

		for (Map.Entry<String, String> entry : newLoadoutKeys.entrySet()) {
			// If the options does not have this loadout key and it is already taken then reset it
			if (!nbt.contains(entry.getKey()) && usedLoadoutKeys.contains(entry.getValue())) {
				nbt.putString(entry.getKey(), "key.keyboard.unknown");
			}
		}
	}
}
