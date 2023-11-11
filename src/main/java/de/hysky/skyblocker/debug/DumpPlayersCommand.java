package de.hysky.skyblocker.debug;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import de.hysky.skyblocker.SkyblockerMod;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class DumpPlayersCommand {
	
	static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("debug")
						.then(literal("dumpPlayers")
								.executes(context -> {
									FabricClientCommandSource source = context.getSource();
									MinecraftClient client = source.getClient();
									
									client.world.getEntities().forEach(e -> {
										if (e instanceof PlayerEntity player) {
											source.sendFeedback(Text.of("\"" + player.getName().getString() + "\""));
										}
									});
									
									return Command.SINGLE_SUCCESS;
								}))));
	}
}
