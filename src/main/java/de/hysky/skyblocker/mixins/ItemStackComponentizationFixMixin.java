package de.hysky.skyblocker.mixins;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.mixins.accessors.BannerPatternFormatFixInvoker;
import de.hysky.skyblocker.utils.datafixer.LegacyItemStackFixer;
import net.minecraft.datafixer.fix.BlockEntityBannerColorFix;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
	@Unique
	private static final Set<String> BANNER_IDS = Set.of(
			"minecraft:white_banner",
			"minecraft:light_gray_banner",
			"minecraft:gray_banner",
			"minecraft:black_banner",
			"minecraft:brown_banner",
			"minecraft:red_banner",
			"minecraft:orange_banner",
			"minecraft:yellow_banner",
			"minecraft:lime_banner",
			"minecraft:green_banner",
			"minecraft:cyan_banner",
			"minecraft:light_blue_banner",
			"minecraft:blue_banner",
			"minecraft:purple_banner",
			"minecraft:magenta_banner",
			"minecraft:pink_banner"
			);

	//Replicate the two data fixes that banner patterns have gotten since 1.9, first one is to change the ids of banner colors
	//and the second is the item stack componentization of course.
	@Inject(method = "fixStack", at = @At("HEAD"))
	private static void skyblocker$replicateBannerPatternFormatFix(CallbackInfo ci, @Local(argsOnly = true) ItemStackComponentizationFix.StackData data) {
		if (data.itemMatches(BANNER_IDS) && LegacyItemStackFixer.ENABLE_DFU_FIXES.get()) {
			data.applyFixer("BlockEntityTag", false, blockEntityTagDynamic -> {
				blockEntityTagDynamic = new BlockEntityBannerColorFix(null, false).fixBannerColor(blockEntityTagDynamic);
				blockEntityTagDynamic = BannerPatternFormatFixInvoker.invokeReplacePatterns(blockEntityTagDynamic);
				blockEntityTagDynamic = blockEntityTagDynamic.set("id", blockEntityTagDynamic.createString("minecraft:banner"));

				return blockEntityTagDynamic;
			});
		}
	}
}
