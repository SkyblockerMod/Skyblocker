package de.hysky.skyblocker.skyblock.calculators;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Calculator;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Formatters;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CalculatorCommand {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(CalculatorCommand::calculate);
	}

	private static void calculate(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("calculate")
						.then(argument("equation", StringArgumentType.greedyString())
								.executes(context -> doCalculation(getString(context, "equation")))
						)
				)
		);
	}

	private static int doCalculation(String calculation) {
		MutableComponent text = Constants.PREFIX.get();
		try {
			text.append(Component.literal(Formatters.DOUBLE_NUMBERS.format(Calculator.calculate(calculation))).withStyle(ChatFormatting.GREEN));
		} catch (Calculator.CalculatorException e) {
			text.append(Component.translatable("skyblocker.config.uiAndVisuals.inputCalculator.invalidEquation").withStyle(ChatFormatting.RED));
			text.append(Component.literal(": ").append(Component.translatable(e.getMessage(), e.args)).withStyle(ChatFormatting.RED));
		}

		if (CLIENT == null || CLIENT.player == null) {
			return 0;
		}

		CLIENT.player.displayClientMessage(text, false);
		return Command.SINGLE_SUCCESS;
	}
}
