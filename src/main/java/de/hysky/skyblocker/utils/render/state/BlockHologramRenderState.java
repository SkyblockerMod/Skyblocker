package de.hysky.skyblocker.utils.render.state;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public record BlockHologramRenderState(BlockPos pos, BlockState state, float alpha) {
}
