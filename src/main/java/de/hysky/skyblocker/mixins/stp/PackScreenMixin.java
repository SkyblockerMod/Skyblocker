package de.hysky.skyblocker.mixins.stp;

import java.util.Comparator;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;

@Mixin(PackScreen.class)
public class PackScreenMixin {
	@Shadow
	private PackListWidget availablePackList;

	@ModifyVariable(method = "updatePackList", at = @At("LOAD"))
	private Stream<ResourcePackOrganizer.Pack> skyblocker$prioritizeSTPPacks(Stream<ResourcePackOrganizer.Pack> original, @Local(argsOnly = true) PackListWidget widget) {
		if (widget == this.availablePackList) {
			return original.sorted(Comparator.comparing(pack -> pack instanceof AbstractPackAccessor abstractPack && abstractPack.getProfile().getSkyblockerMetadata() != null).reversed());
		}

		return original;
	}
}
