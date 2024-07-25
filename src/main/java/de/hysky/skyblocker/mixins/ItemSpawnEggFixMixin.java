package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import de.hysky.skyblocker.utils.datafixer.LegacyItemStackFixer;
import net.minecraft.datafixer.fix.ItemSpawnEggFix;

@Mixin(ItemSpawnEggFix.class)
public class ItemSpawnEggFixMixin {

	//Hypixel uses a Damage of zero for certain spawn egg items like decoys which sent to modern clients as polar bear spawn eggs.
	//Since polar bears didn't exist in 1.9 we pretend that these are wither spawn eggs and then correct it back 
	//to polar bear ones later. I've used a wither spawn egg because these did not exist in their own individual form until 1.19.
	@ModifyVariable(method = "method_5027", at = @At("STORE"))
	private static String skyblocker$correctPolarBearEggs(String entityId, @Local short damage) {
		return (damage & 255) == 0 && LegacyItemStackFixer.ENABLE_DFU_FIXES.get() ? "WitherBoss" : entityId; //We correct to polar bear later
	}
}
