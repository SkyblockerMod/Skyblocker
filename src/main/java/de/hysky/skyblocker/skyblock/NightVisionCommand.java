package de.hysky.skyblocker.skyblock;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class NightVisionCommand {

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(NightVisionCommand::register);
	}

	private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess access) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("nightVision")
						.then(argument("strength", IntegerArgumentType.integer(0, 100))
								.executes(context -> NightVisionCommand.writeStrength(context, IntegerArgumentType.getInteger(context, "strength"))))
						.then(literal("off").executes(context -> NightVisionCommand.writeStrength(context, 0)))
						.then(literal("full").executes(context -> NightVisionCommand.writeStrength(context, 100)))));
	}

	private static int writeStrength(CommandContext<FabricClientCommandSource> context, int strength) {
		SkyblockerConfigManager.get().uiAndVisuals.nightVisionStrength = strength;
		context.getSource().sendFeedback(Text.translatable("skyblocker.nightVision.success", strength));
		SkyblockerConfigManager.save();
		return 1;
	}
}
