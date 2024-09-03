package de.hysky.skyblocker.skyblock.calculators;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Calculator;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;
import java.util.Locale;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CalculatorCommand {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final NumberFormat FORMATTER = NumberFormat.getInstance(Locale.US);

    @Init
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register(CalculatorCommand::calculate);
    }

    private static void calculate(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal(SkyblockerMod.NAMESPACE)
                .then(literal("calculate")
                        .then(argument("equation", StringArgumentType.greedyString())
                                .executes(context -> doCalculation(getString(context, "equation")))
                        )
                )
        );
    }

    private static int doCalculation(String calculation) {
        MutableText text = Constants.PREFIX.get();
        try {
            text.append(Text.literal(FORMATTER.format(Calculator.calculate(calculation))).formatted(Formatting.GREEN));
        } catch (UnsupportedOperationException e) {
            text.append(Text.translatable("skyblocker.config.uiAndVisuals.inputCalculator.invalidEquation").formatted(Formatting.RED));
        }

        if (CLIENT == null || CLIENT.player == null) {
            return 0;
        }

        CLIENT.player.sendMessage(text, false);
        return Command.SINGLE_SUCCESS;
    }
}
