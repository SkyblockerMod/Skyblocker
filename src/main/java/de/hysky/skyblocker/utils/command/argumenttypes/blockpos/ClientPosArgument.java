package de.hysky.skyblocker.utils.command.argumenttypes.blockpos;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

/**
 * This interface, its 2 implementations and ClientBlockPosArgumentType are all copied from minecraft
 * and converted to use FabricClientCommandSource instead of ServerCommandSource.
 * This removes the need for hacky workarounds such as creating new ServerCommandSources with null or 0 on every argument.
 */
public interface ClientPosArgument {
	Vec3d toAbsolutePos(FabricClientCommandSource source);

	Vec2f toAbsoluteRotation(FabricClientCommandSource source);

	default BlockPos toAbsoluteBlockPos(FabricClientCommandSource source) {
		return BlockPos.ofFloored(this.toAbsolutePos(source));
	}

	boolean isXRelative();

	boolean isYRelative();

	boolean isZRelative();
}
