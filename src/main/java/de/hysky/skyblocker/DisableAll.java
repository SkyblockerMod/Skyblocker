package de.hysky.skyblocker;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Command helper for disabling every configurable feature.
 */
public class DisableAll {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String CONFIGS_PACKAGE = "de.hysky.skyblocker.config.configs";

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(DisableAll::registerCommand);
	}

	private static final long CONFIRM_TIMEOUT = 30_000L; // 30 seconds
	private static long confirmAllowedUntil;

	private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, net.minecraft.commands.CommandBuildContext registryAccess) {
		dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
				.then(ClientCommandManager.literal("disableAll")
						.executes(DisableAll::confirmMessage)
						.then(ClientCommandManager.literal("confirm")
								.executes(DisableAll::disableAll)))
		);
	}

	private static int confirmMessage(CommandContext<FabricClientCommandSource> context) {
		confirmAllowedUntil = System.currentTimeMillis() + CONFIRM_TIMEOUT;
		MutableComponent confirm = Component.translatable("skyblocker.disableAll.confirmYes")
				.withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("/" + SkyblockerMod.NAMESPACE + " disableAll confirm")));
		context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.disableAll.confirm", confirm)));
		return Command.SINGLE_SUCCESS;
	}

	private static int disableAll(CommandContext<FabricClientCommandSource> context) {
		if (System.currentTimeMillis() > confirmAllowedUntil) {
			context.getSource().sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.disableAll.notPending")));
			return Command.SINGLE_SUCCESS;
		}
		confirmAllowedUntil = 0;
		try {
			SkyblockerConfigManager.update(config -> {
				try {
					disableBooleans(config);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.disableAll.success")));
		} catch (Exception e) {
			LOGGER.error("[Skyblocker DisableAll] Failed to disable all features", e);
			context.getSource().sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.disableAll.failed")));
		}
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Recursively sets all boolean fields within our configuration classes to
	 * {@code false}. Previously this relied on the {@code SerialEntry} annotation
	 * from YACL, but the configuration system no longer uses it.
	 */
	private static void disableBooleans(Object target) throws IllegalAccessException {
		for (Field field : target.getClass().getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) continue;
			field.setAccessible(true);
			Object value = field.get(target);
			Class<?> type = field.getType();
			if (type == boolean.class) {
				field.setBoolean(target, false);
			} else if (type == Boolean.class) {
				field.set(target, false);
			} else if (value instanceof java.util.Map<?, ?>) {
				@SuppressWarnings("unchecked")
				java.util.Map<Object, Object> m = (java.util.Map<Object, Object>) value;
				for (java.util.Map.Entry<Object, Object> entry : m.entrySet()) {
					if (entry.getValue() instanceof Boolean) {
						m.put(entry.getKey(), Boolean.FALSE);
					}
				}
			} else if (value != null && isConfigClass(type)) {
				disableBooleans(value);
			}
		}
	}

	/**
	 * Returns {@code true} if the given class represents one of our config
	 * classes. This prevents {@link #disableBooleans(Object)} from touching
	 * unrelated objects from other mods.
	 */
	private static boolean isConfigClass(Class<?> clazz) {
		return !clazz.isPrimitive()
				&& !clazz.isEnum()
				&& !clazz.isRecord()
				&& !clazz.equals(String.class)
				&& !Number.class.isAssignableFrom(clazz)
				&& clazz.getPackageName().startsWith(CONFIGS_PACKAGE);
	}
}
