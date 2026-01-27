package de.hysky.skyblocker.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ConfigCommands {
	static void registerConfigEntries(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
		try {
			registerConfigEntries(builder, SkyblockerConfigManager.get());
		} catch (Exception e) {
			SkyblockerConfigManager.LOGGER.error("[Skyblocker Config Manager] Failed to register config entries command!", e);
		}
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

	private static LiteralArgumentBuilder<FabricClientCommandSource> registerBooleanConfigEntry(Field field, Object object, String name) {
		return literal(name).then(literal("true").executes(context -> {
			SkyblockerConfigManager.update(config -> {
				try {
					field.setBoolean(object, true);
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.commands.set", name, true).withStyle(ChatFormatting.GREEN)));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});
			return Command.SINGLE_SUCCESS;
		})).then(literal("false").executes(context -> {
			SkyblockerConfigManager.update(config -> {
				try {
					field.setBoolean(object, false);
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.commands.set", name, false).withStyle(ChatFormatting.GREEN)));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});
			return Command.SINGLE_SUCCESS;
		})).then(literal("toggle").executes(context -> {
			SkyblockerConfigManager.update(config -> {
				try {
					boolean toggled = !field.getBoolean(object);
					field.setBoolean(object, toggled);
					context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.commands.set", name, toggled).withStyle(ChatFormatting.GREEN)));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});
			return Command.SINGLE_SUCCESS;
		})).executes(context -> {
			try {
				context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.commands.query", name, field.getBoolean(object)).withStyle(ChatFormatting.GREEN)));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			return Command.SINGLE_SUCCESS;
		});
	}
}
