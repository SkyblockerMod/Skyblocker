package de.hysky.skyblocker.mixins;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.level.block.Blocks;

@Mixin(Blocks.class)
public class BlocksMixin {

	@Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/core/registries/BuiltInRegistries;BLOCK:Lnet/minecraft/core/DefaultedRegistry;", opcode = Opcodes.GETSTATIC))
	private static void skyblocker$loadUtils(CallbackInfo ci) {
		// I believe the ItemStack mixin calling Utils.isOnSkyblock() so early (when items are created for durability caching) creates a
		// cyclic class loading dependency where Utils tries to initialize the Items class by initializing the vanilla registry constant while Items is actively being initialized
		// thus causing some Items to constants to be null while trying to register stuff for the default registry context.
		// This "fix" isn't great but it works for now and I don't have to spend more time on getting things to work.
		@SuppressWarnings("unused")
		boolean ensureLoaded = Utils.isOnSkyblock();
	}
}
