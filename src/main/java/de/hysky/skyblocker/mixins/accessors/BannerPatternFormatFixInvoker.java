package de.hysky.skyblocker.mixins.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.serialization.Dynamic;

import net.minecraft.datafixer.fix.BannerPatternFormatFix;

@Mixin(BannerPatternFormatFix.class)
public interface BannerPatternFormatFixInvoker {

	@Invoker("replacePatterns")
	static Dynamic<?> invokeReplacePatterns(Dynamic<?> dynamic) {
		throw new UnsupportedOperationException();
	}
}
