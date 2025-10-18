package de.hysky.skyblocker.mixins;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.skyblock.item.custom.CustomHelmetTextures;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;

@Mixin(ComponentHolder.class)
public interface ComponentHolderMixin {

	@SuppressWarnings("unchecked")
	@ModifyReturnValue(method = "get", at = @At("RETURN"))
	private <T> T skyblocker$customComponents(T original, ComponentType<? extends T> dataComponentType) {
		if (Utils.isOnSkyblock() && ((Object) this) instanceof ItemStack stack) {
			String itemUuid = stack.getUuid();
			if (dataComponentType == DataComponentTypes.TRIM) {
				Object2ObjectOpenHashMap<String, CustomArmorTrims.ArmorTrimId> customTrims = SkyblockerConfigManager.get().general.customArmorTrims;
				CustomArmorTrims.ArmorTrimId trimKey = customTrims.get(itemUuid);
				if (trimKey != null) return (T) CustomArmorTrims.TRIMS_CACHE.getOrDefault(trimKey, (ArmorTrim) original);
			} else if (dataComponentType == DataComponentTypes.PROFILE && stack.isOf(Items.PLAYER_HEAD)) {
				Object2ObjectOpenHashMap<String, String> customTextures = SkyblockerConfigManager.get().general.customHelmetTextures;
				String tex = customTextures.get(itemUuid);
				if (tex != null) return (T) CustomHelmetTextures.getProfile(tex);
			} else if (dataComponentType == DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE) {
				Object2BooleanMap<String> customGlint = SkyblockerConfigManager.get().general.customGlint;
				Boolean glint = customGlint.get(itemUuid); // sorry fastutil :(
				if (glint != null) return (T) glint;
			} else if (dataComponentType == DataComponentTypes.ITEM_MODEL) {
				Object2ObjectOpenHashMap<String, Identifier> customItemModel = SkyblockerConfigManager.get().general.customItemModel;
				Identifier id = customItemModel.get(itemUuid);
				if (id != null) return (T) id;
			}
		}
		return original;
	}
}
