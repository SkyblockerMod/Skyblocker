package de.hysky.skyblocker.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import de.hysky.skyblocker.utils.datafixer.LegacyItemStackFixer;
import net.minecraft.datafixer.fix.EntityIdFix;

@Mixin(EntityIdFix.class)
public class EntityIdFixMixin {

	@ModifyArg(method = "method_15710", at = @At(value = "INVOKE", target = "Ljava/util/Map;getOrDefault(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false), index = 0)
	private static Object skyblocker$convertToPolarBear(Object id) { //This argument is a string and I don't want to bother with casting since its fine as is
		return id.equals("WitherBoss") && LegacyItemStackFixer.ENABLE_DFU_FIXES.get() ? "PolarBear" : id;
	}
}
