package de.hysky.skyblocker.utils.command.argumenttypes.blockpos;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * This interface, its 2 implementations and ClientBlockPosArgumentType are all copied from minecraft
 * and converted to use FabricClientCommandSource instead of ServerCommandSource.
 * This removes the need for hacky workarounds such as creating new ServerCommandSources with null or 0 on every argument.
 */
public interface ClientPosArgument {
	Vec3 toAbsolutePos(FabricClientCommandSource source);

	Vec2 toAbsoluteRotation(FabricClientCommandSource source);

	default BlockPos toAbsoluteBlockPos(FabricClientCommandSource source) {
		return BlockPos.containing(this.toAbsolutePos(source));
	}

	boolean isXRelative();

	boolean isYRelative();

	boolean isZRelative();
}
