package me.xmrvizzy.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.item.CustomArmorTrims;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ArmorTrim.class)
public class ArmorTrimMixin {

    @ModifyReturnValue(method = "getTrim", at = @At("RETURN"))
    private static Optional<ArmorTrim> skyblocker$customArmorTrims(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<ArmorTrim> original, @Local ItemStack stack) {
        NbtCompound nbt = stack.getNbt();

        if (Utils.isOnSkyblock() && nbt != null && nbt.contains("ExtraAttributes")) {
            Object2ObjectOpenHashMap<String, CustomArmorTrims.ArmorTrimId> customTrims = SkyblockerConfig.get().general.customArmorTrims;
            NbtCompound extraAttributes = nbt.getCompound("ExtraAttributes");
            String itemUuid = extraAttributes.contains("uuid") ? extraAttributes.getString("uuid") : null;

            if (customTrims.containsKey(itemUuid)) {
                CustomArmorTrims.ArmorTrimId trimKey = customTrims.get(itemUuid);
                return CustomArmorTrims.TRIMS_CACHE.getOrDefault(trimKey, original);
            }
        }

        return original;
    }
}
