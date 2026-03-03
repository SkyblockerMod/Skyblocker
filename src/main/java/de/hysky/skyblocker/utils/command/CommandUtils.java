package de.hysky.skyblocker.utils.command;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class CommandUtils {
	public static Command<FabricClientCommandSource> noOp = _ctx -> -1;
}
