package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.skyblock.item.custom.preset.ArmorPreviewStorage;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.item.Items;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import de.hysky.skyblocker.skyblock.item.custom.CustomHelmetTextures;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;

@Mixin(ComponentHolder.class)
public interface ComponentHolderMixin {

	@SuppressWarnings("unchecked")
	@ModifyReturnValue(method = "get", at = @At("RETURN"))
	private <T> T skyblocker$customComponents(T original, ComponentType<? extends T> dataComponentType) {
		if (Utils.isOnSkyblock() && ((Object) this) instanceof ItemStack stack) {
			String itemUuid = ItemUtils.getItemUuid(stack);
			if (dataComponentType == DataComponentTypes.TRIM) {
				if (ArmorPreviewStorage.TEMP_TRIMS.containsKey(itemUuid)) {
					CustomArmorTrims.ArmorTrimId trimKey = ArmorPreviewStorage.TEMP_TRIMS.get(itemUuid);
					return (T) CustomArmorTrims.TRIMS_CACHE.getOrDefault(trimKey, (ArmorTrim) original);
				}

				Object2ObjectOpenHashMap<String, CustomArmorTrims.ArmorTrimId> customTrims = SkyblockerConfigManager.get().general.customArmorTrims;
				if (customTrims.containsKey(itemUuid)) {
					CustomArmorTrims.ArmorTrimId trimKey = customTrims.get(itemUuid);
					return (T) CustomArmorTrims.TRIMS_CACHE.getOrDefault(trimKey, (ArmorTrim) original);
				}
			} else if (dataComponentType == DataComponentTypes.PROFILE && stack.isOf(Items.PLAYER_HEAD)) {
				if (ArmorPreviewStorage.TEMP_HELMET_TEXTURES.containsKey(itemUuid)) {
					String tex = ArmorPreviewStorage.TEMP_HELMET_TEXTURES.get(itemUuid);
					return (T) CustomHelmetTextures.getProfile(tex);
				}

				Object2ObjectOpenHashMap<String, String> customTextures = SkyblockerConfigManager.get().general.customHelmetTextures;
				if (customTextures.containsKey(itemUuid)) {
					String tex = customTextures.get(itemUuid);
					return (T) CustomHelmetTextures.getProfile(tex);
				}
			}
		}

		return original;
	}
}
