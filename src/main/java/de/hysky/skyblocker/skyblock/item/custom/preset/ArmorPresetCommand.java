package de.hysky.skyblocker.skyblock.item.custom.preset;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.Optional;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ArmorPresetCommand {
	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(ArmorPresetCommand::register);
	}

	private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("preset")
						.then(literal("export")
								.then(argument("name", StringArgumentType.greedyString())
										.suggests((ctx, builder) -> CommandSource.suggestMatching(
												ArmorPresets.getInstance().getPresets().stream().map(ArmorPreset::name).toList(), builder))
										.executes(ctx -> exportPreset(ctx, StringArgumentType.getString(ctx, "name")))))
						.then(literal("import").executes(ArmorPresetCommand::importPreset))
				));
	}

	private static int exportPreset(CommandContext<FabricClientCommandSource> ctx, String name) {
		Optional<ArmorPreset> preset = ArmorPresets.getInstance().getPresets().stream()
				.filter(p -> p.name().equalsIgnoreCase(name)).findFirst();
		if (preset.isEmpty()) {
			ctx.getSource().sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.armorPresets.exportError")));
			return Command.SINGLE_SUCCESS;
		}
		String json = SkyblockerMod.GSON_COMPACT.toJson(preset.get());
		MinecraftClient.getInstance().keyboard.setClipboard(json);
		ctx.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.armorPresets.exportSuccess")));
		return Command.SINGLE_SUCCESS;
	}

	private static int importPreset(CommandContext<FabricClientCommandSource> ctx) {
		String json = MinecraftClient.getInstance().keyboard.getClipboard();
		try {
			ArmorPreset preset = SkyblockerMod.GSON.fromJson(json, ArmorPreset.class);
			if (preset != null) {
				ArmorPresets.getInstance().addPreset(preset);
				ctx.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.armorPresets.importSuccess")));
			} else {
				ctx.getSource().sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.armorPresets.importError")));
			}
		} catch (Exception e) {
			ctx.getSource().sendError(Constants.PREFIX.get().append(Text.translatable("skyblocker.armorPresets.importError")));
		}
		return Command.SINGLE_SUCCESS;
	}
}
