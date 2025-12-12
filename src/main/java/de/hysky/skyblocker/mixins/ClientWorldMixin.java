package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.events.WorldEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

@Mixin(ClientLevel.class)
public abstract class ClientWorldMixin implements BlockGetter {

	@Inject(method = "setServerVerifiedBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"))
	private void skyblocker$beforeBlockUpdate(CallbackInfo ci, @Local(argsOnly = true) BlockPos pos, @Share("old") LocalRef<BlockState> oldState) {
		oldState.set(getBlockState(pos));
	}

	/**
	 * @implNote The {@code pos} can be mutable when this is called by chunk delta updates, so if you want to copy it into memory
	 * (e.g. store it in a field/list/map) make sure to duplicate it via {@link BlockPos#immutable()}.
	 */
	@Inject(method = "setServerVerifiedBlockState", at = @At("RETURN"))
	private void skyblocker$afterBlockUpdate(CallbackInfo ci, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state, @Share("old") LocalRef<BlockState> oldState) {
		WorldEvents.BLOCK_STATE_UPDATE.invoker().onBlockStateUpdate(pos, oldState.get(), state);
	}

	@Inject(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), cancellable = true)
	private void skyblocker$allowSoundsFromEntity(CallbackInfo ci, @Local(argsOnly = true) Holder<SoundEvent> sound) {
		if (!PlaySoundEvents.ALLOW_SOUND.invoker().allowSound(sound.value())) {
			ci.cancel();
		}
	}

	@Inject(method = "playSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZJ)V", at = @At("HEAD"), cancellable = true)
	private void skyblocker$allowSounds(CallbackInfo ci, @Local(argsOnly = true) SoundEvent sound) {
		if (!PlaySoundEvents.ALLOW_SOUND.invoker().allowSound(sound)) {
			ci.cancel();
		}
	}
}
