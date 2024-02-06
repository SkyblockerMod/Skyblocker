package de.hysky.skyblocker.debug;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Debug {
	private static final boolean DEBUG_ENABLED = Boolean.parseBoolean(System.getProperty("skyblocker.debug", "false"));

	public static boolean debugEnabled() {
		return DEBUG_ENABLED || FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	public static void init() {
		if (debugEnabled()) {
			SnapshotDebug.init();
			ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("debug")
					.then(dumpPlayersCommand())
					.then(ItemUtils.dumpHeldItemNbtCommand())
			)));
		}
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpPlayersCommand() {
		return literal("dumpPlayers")
				.executes(context -> {
					context.getSource().getWorld().getPlayers().forEach(player -> context.getSource().sendFeedback(Text.of("'" + player.getName().getString() + "'")));
					return Command.SINGLE_SUCCESS;
				});
	}
}
