package de.hysky.skyblocker.mixins.jgit;

import org.eclipse.jgit.lib.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(targets = "org.eclipse.jgit.util.SystemReader$Default", remap = false)
public class SystemReaderMixin {

	@ModifyReturnValue(method = "getenv", at = @At("RETURN"))
	private String skyblocker$blockLoadingSystemGitConfig(String original, String variable) {
		return variable.equals(Constants.GIT_CONFIG_NOSYSTEM_KEY) ? "FORCE-ENABLE" : original;
	}
}
