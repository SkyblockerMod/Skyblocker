package de.hysky.skyblocker.mixins;

import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomAnimatedHelmetTextures;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.skyblock.item.custom.CustomHelmetTextures;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
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
				CustomArmorTrims.ArmorTrimId trimKey = SkyblockerConfigManager.get().general.customArmorTrims.get(itemUuid);

				if (trimKey != null) {
					return (T) CustomArmorTrims.TRIMS_CACHE.getOrDefault(trimKey, (ArmorTrim) original);
				}
			} else if (dataComponentType == DataComponentTypes.PROFILE && stack.isOf(Items.PLAYER_HEAD)) {
				// Normal head textures
				String tex = SkyblockerConfigManager.get().general.customHelmetTextures.get(itemUuid);

				if (tex != null) {
					return (T) CustomHelmetTextures.getProfile(tex);
				}

				// Animated heads
				String animatedTexId = SkyblockerConfigManager.get().general.customAnimatedHelmetTextures.get(itemUuid);
				ProfileComponent frame = animatedTexId != null ? CustomAnimatedHelmetTextures.animateHeadTexture(animatedTexId) : null;

				if (frame != null) {
					return (T) frame;
				}
			} else if (dataComponentType == DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE) {
				@SuppressWarnings("deprecation")
				Boolean glint = SkyblockerConfigManager.get().general.customGlint.get(itemUuid); // sorry fastutil :(

				if (glint != null) {
					return (T) glint;
				}
			} else if (dataComponentType == DataComponentTypes.ITEM_MODEL) {
				Identifier id = SkyblockerConfigManager.get().general.customItemModel.get(itemUuid);

				if (id != null) {
					return (T) id;
				}
			}
		}
		return original;
	}
}
