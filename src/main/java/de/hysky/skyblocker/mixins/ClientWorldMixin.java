package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.events.WorldEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin implements BlockView {

	@Inject(method = "handleBlockUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z"))
	private void skyblocker$beforeBlockUpdate(CallbackInfo ci, @Local(argsOnly = true) BlockPos pos, @Share("old") LocalRef<BlockState> oldState) {
		oldState.set(getBlockState(pos));
	}

	/**
	 * @implNote The {@code pos} can be mutable when this is called by chunk delta updates, so if you want to copy it into memory
	 * (e.g. store it in a field/list/map) make sure to duplicate it via {@link BlockPos#toImmutable()}.
	 */
	@Inject(method = "handleBlockUpdate", at = @At("RETURN"))
	private void skyblocker$afterBlockUpdate(CallbackInfo ci, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state, @Share("old") LocalRef<BlockState> oldState) {
		WorldEvents.BLOCK_STATE_UPDATE.invoker().onBlockStateUpdate(pos, oldState.get(), state);
	}

	@Inject(method = "playSoundFromEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V", at = @At("HEAD"), cancellable = true)
	private void skyblocker$allowSoundsFromEntity(CallbackInfo ci, @Local(argsOnly = true) RegistryEntry<SoundEvent> sound) {
		if (!PlaySoundEvents.ALLOW_SOUND.invoker().allowSound(sound.value())) {
			ci.cancel();
		}
	}

	@Inject(method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", at = @At("HEAD"), cancellable = true)
	private void skyblocker$allowSounds(CallbackInfo ci, @Local(argsOnly = true) SoundEvent sound) {
		if (!PlaySoundEvents.ALLOW_SOUND.invoker().allowSound(sound)) {
			ci.cancel();
		}
	}
}
