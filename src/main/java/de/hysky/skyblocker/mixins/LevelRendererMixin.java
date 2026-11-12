package de.hysky.skyblocker.mixins;

//import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow
	@Final
	private SubmitNodeStorage submitNodeStorage;

	/*@ModifyReturnValue(method = "frameHasAlwaysOnTopGizmos", at = @At("RETURN"))
	private boolean skyblocker$hasAlwaysOnTopSubmits(boolean original) {
		// If there's no always on top gizmos check if there's just any always on top submits
		if (!original) {
			for (SubmitNodeCollection collection : this.submitNodeStorage.getSubmitsPerOrder().values()) {
				if (!collection.alwaysOnTopGizmos.isEmpty()) {
					return true;
				}
			}
		}

		return original;
	}*/
}
