package de.hysky.skyblocker.debug;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.SkyblockerMod;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class DumpPlayersCommand {
	
	static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("debug")
						.then(literal("dumpPlayers")
								.executes(context -> {
									FabricClientCommandSource source = context.getSource();

									source.getWorld().getEntities().forEach(e -> {
										if (e instanceof PlayerEntity player) {
											source.sendFeedback(Text.of("'" + player.getName().getString() + "'"));
										}
									});

									return Command.SINGLE_SUCCESS;
								}))));
	}
}
