package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.CustomArmorTrims;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;

@Mixin(ComponentHolder.class)
public interface ComponentHolderMixin {

	@SuppressWarnings("unchecked")
	@ModifyReturnValue(method = "get", at = @At("RETURN"))
	private <T> T skyblocker$customArmorTrims(T original, DataComponentType<? extends T> dataComponentType) {
		if (Utils.isOnSkyblock() && ((Object) this) instanceof ItemStack stack) {
			if (dataComponentType == DataComponentTypes.TRIM) {
				Object2ObjectOpenHashMap<String, CustomArmorTrims.ArmorTrimId> customTrims = SkyblockerConfigManager.get().general.customArmorTrims;
				String itemUuid = ItemUtils.getItemUuid(stack);

				if (customTrims.containsKey(itemUuid)) {
					CustomArmorTrims.ArmorTrimId trimKey = customTrims.get(itemUuid);
					return (T) CustomArmorTrims.TRIMS_CACHE.getOrDefault(trimKey, (ArmorTrim) original);
				}
			}
		}

		return original;
	}
}
