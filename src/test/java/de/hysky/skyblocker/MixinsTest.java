package de.hysky.skyblocker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;

public class MixinsTest {

	@BeforeAll
	public static void setupEnvironment() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	public void auditMixins() {
		//Ensure that the transformer is active so that the Mixins can be audited
		Assertions.assertInstanceOf(IMixinTransformer.class, MixinEnvironment.getCurrentEnvironment().getActiveTransformer());

		//If this fails check the report to get the full stack trace
		MixinEnvironment.getCurrentEnvironment().audit();
	}
}
