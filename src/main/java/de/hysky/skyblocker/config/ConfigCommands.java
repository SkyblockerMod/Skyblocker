package de.hysky.skyblocker.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class ConfigCommands {
	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) ->
				dispatcher.register(literal(SkyblockerMod.NAMESPACE)
						.then(ConfigCommands.registerConfigEntries(literal("configExecute"))))
		);
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> registerConfigEntries(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		try {
			return registerConfigEntries(builder, SkyblockerConfigManager.getUnpatched());
		} catch (Exception e) {
			SkyblockerConfigManager.LOGGER.error("[Skyblocker Config Manager] Failed to register config entries command!", e);
		}
		return builder;
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> registerConfigEntries(LiteralArgumentBuilder<FabricClientCommandSource> builder, Object object) throws IllegalAccessException {
		for (Field field : object.getClass().getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) continue;
			field.setAccessible(true);

			Class<?> type = field.getType();
			String name = field.getName();
			Object value = field.get(object);

			if (type == boolean.class) {
				builder.then(registerBooleanConfigEntry(field, object, name));
			} else if (value != null && SkyblockerConfigManager.isConfigClass(type)) {
				builder.then(registerConfigEntries(literal(name), value));
			}
		}

		return builder;
	}

	private static Component formatValue(boolean bl) {
		return Component.literal(Boolean.toString(bl)).withStyle(bl ? ChatFormatting.GREEN : ChatFormatting.RED);
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> registerBooleanConfigEntry(Field field, Object object, String name) {
		return literal(name).then(argument("value", BoolArgumentType.bool()).executes(context -> {
			SkyblockerConfigManager.update(_ -> {
				try {
					boolean value = BoolArgumentType.getBool(context, "value");
					field.setBoolean(object, value);
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.commands.set", name, formatValue(value))));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});
			return Command.SINGLE_SUCCESS;
		})).then(literal("toggle").executes(context -> {
			SkyblockerConfigManager.update(_ -> {
				try {
					boolean toggled = !field.getBoolean(object);
					field.setBoolean(object, toggled);
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.commands.set", name, formatValue(toggled))));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			try {
				context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.commands.query", name, formatValue(field.getBoolean(object)))));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			return Command.SINGLE_SUCCESS;
		});
	}
}
