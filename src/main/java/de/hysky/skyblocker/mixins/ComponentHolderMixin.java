package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomAnimatedHelmetTextures;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.skyblock.item.custom.CustomHelmetTextures;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

@Mixin(DataComponentHolder.class)
public interface ComponentHolderMixin {

	@SuppressWarnings("unchecked")
	@ModifyReturnValue(method = "get", at = @At("RETURN"))
	private <T> T skyblocker$customComponents(T original, DataComponentType<? extends T> dataComponentType) {
		if (Utils.isOnSkyblock() && ((Object) this) instanceof ItemStack stack) {
			String itemUuid = stack.getUuid();
			if (dataComponentType == DataComponents.TRIM) {
				CustomArmorTrims.ArmorTrimId trimKey = SkyblockerConfigManager.get().general.customArmorTrims.get(itemUuid);

				if (trimKey != null) {
					return (T) CustomArmorTrims.TRIMS_CACHE.getOrDefault(trimKey, (ArmorTrim) original);
				}
			} else if (dataComponentType == DataComponents.PROFILE && stack.is(Items.PLAYER_HEAD)) {
				// Normal head textures
				String tex = SkyblockerConfigManager.get().general.customHelmetTextures.get(itemUuid);

				if (tex != null) {
					return (T) CustomHelmetTextures.getProfile(tex);
				}

				// Animated heads
				String animatedTexId = SkyblockerConfigManager.get().general.customAnimatedHelmetTextures.get(itemUuid);
				ResolvableProfile frame = animatedTexId != null ? CustomAnimatedHelmetTextures.animateHeadTexture(animatedTexId) : null;

				if (frame != null) {
					return (T) frame;
				}
			} else if (dataComponentType == DataComponents.ENCHANTMENT_GLINT_OVERRIDE) {
				@SuppressWarnings("deprecation")
				Boolean glint = SkyblockerConfigManager.get().general.customGlint.get(itemUuid); // sorry fastutil :(

				if (glint != null) {
					return (T) glint;
				}
			} else if (dataComponentType == DataComponents.ITEM_MODEL) {
				Identifier id = SkyblockerConfigManager.get().general.customItemModel.get(itemUuid);

				if (id != null) {
					return (T) id;
				}
			}
		}
		return original;
	}
}
