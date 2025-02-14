package de.hysky.skyblocker.mixins;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;

import de.hysky.skyblocker.skyblock.entity.MobGlow;

import net.minecraft.client.render.RenderPhase;

@Mixin(RenderPhase.DepthTest.class)
public class RenderPhaseDepthTestMixin {

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderPhase;<init>(Ljava/lang/String;Ljava/lang/Runnable;Ljava/lang/Runnable;)V"), index = 1)
	private static Runnable skyblocker$modifyOutlineAlwaysStartAction(Runnable original, @Local(argsOnly = true) String depthFunctionName) {
		if (depthFunctionName.equals("outline_always")) {
			return () -> {
				if (MobGlow.atLeastOneMobHasCustomGlow()) {
					RenderSystem.enableDepthTest();
					RenderSystem.depthFunc(GL11.GL_LEQUAL);
				}
			};
		}

		return original;
	}

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderPhase;<init>(Ljava/lang/String;Ljava/lang/Runnable;Ljava/lang/Runnable;)V"), index = 2)
	private static Runnable skyblocker$modifyOutlineAlwaysEndAction(Runnable original, @Local(argsOnly = true) String depthFunctionName) {
		if (depthFunctionName.equals("outline_always")) {
			return () -> {
				if (MobGlow.atLeastOneMobHasCustomGlow()) {
					RenderSystem.disableDepthTest();
					RenderSystem.depthFunc(GL11.GL_LEQUAL);
				}
			};
		}

		return original;
	}
}
