package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.skyblock.dungeon.device.SimonSays;
import de.hysky.skyblocker.skyblock.dwarven.CrystalsChestHighlighter;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

	/**
	 * @implNote The {@code pos} can be mutable when this is called by chunk delta updates, so if you want to copy it into memory
	 * (e.g. store it in a field/list/map) make sure to duplicate it via {@link BlockPos#toImmutable()}.
	 */
    @Inject(method = "handleBlockUpdate", at = @At("RETURN"))
    private void skyblocker$handleBlockUpdate(CallbackInfo ci, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state) {
        if (Utils.isInCrimson()) {
            DojoManager.onBlockUpdate(pos.toImmutable(), state);
        } else if (Utils.isInCrystalHollows()) {
            CrystalsChestHighlighter.onBlockUpdate(pos.toImmutable(), state);
        }

        SimonSays.onBlockUpdate(pos, state);
    }
}
