package de.hysky.skyblocker.mixins;


import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    @Inject(method = "handleBlockUpdate", at = @At("RETURN"))
    private void skyblocker$handleBlockUpdate(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        if (Utils.getLocation() == Location.CRIMSON_ISLE) {
            DojoManager.onBlockUpdate(pos.toImmutable(), state);
        }
    }
}
