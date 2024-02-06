package de.hysky.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.CustomArmorTrims;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ArmorTrim.class)
public class ArmorTrimMixin {

	@ModifyReturnValue(method = "getTrim", at = @At("RETURN"))
	private static Optional<ArmorTrim> skyblocker$customArmorTrims(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<ArmorTrim> original, @Local(argsOnly = true) ItemStack stack) {
		if (Utils.isOnSkyblock()) {
			Object2ObjectOpenHashMap<String, CustomArmorTrims.ArmorTrimId> customTrims = SkyblockerConfigManager.get().general.customArmorTrims;
			String itemUuid = ItemUtils.getItemUuid(stack);

			if (customTrims.containsKey(itemUuid)) {
				CustomArmorTrims.ArmorTrimId trimKey = customTrims.get(itemUuid);
				return CustomArmorTrims.TRIMS_CACHE.getOrDefault(trimKey, original);
			}
		}

		return original;
	}
}
