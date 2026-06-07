package de.hysky.skyblocker.utils;

import com.mojang.brigadier.Command;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.FileManager;
import org.apache.logging.log4j.spi.LoggerContext;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LogsFolderFinder {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static @Nullable Path folder;

	public static Path getLogsFolder() {
		if (folder == null) {
			folder = computeFolder();
		}
		return folder;
	}

	public static void openLogsFolder() {
		Util.getPlatform().openPath(getLogsFolder());
	}

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) ->
				dispatcher.register(ClientCommands.literal(SkyblockerMod.NAMESPACE).then(
						ClientCommands.literal("logsFolder")
								.executes(_ -> {
									openLogsFolder();
									return Command.SINGLE_SUCCESS;
								})
				)));
	}

	private static Path computeFolder() {
		LoggerContext context;
		if (LogManager.getFactory().isClassLoaderDependent()) {
			context = LogManager.getContext(Minecraft.class.getClassLoader(), false);
		} else {
			context = LogManager.getContext(false);
		}
		if (!(context instanceof org.apache.logging.log4j.core.LoggerContext instance)) {
			LOGGER.warn("Could not compute logs folder, expected instance of LoggerContext got {}", context.getClass());
			return getFallbackFolder();
		}
		Map<String, Appender> appenders = instance.getConfiguration().getAppenders();
		List<String> fileNames = new ArrayList<>(appenders.size());
		for (Appender appender : appenders.values()) {
			if (appender instanceof AbstractOutputStreamAppender<?> streamAppender && streamAppender.getManager() instanceof FileManager manager) {
				fileNames.add(manager.getFileName());
			}
		}
		if (fileNames.isEmpty()) {
			LOGGER.warn("Could not compute logs folder: didn't find any StreamAppender with a FileManager");
			return getFallbackFolder();
		}
		Optional<String> latestLogOpt = fileNames.stream().filter(s -> s.contains("latest.log")).findFirst();
		if (latestLogOpt.isEmpty()) {
			LOGGER.warn("Could not compute logs folder: didn't find a path that contains 'latest.log'. Only got: {}", fileNames);
			return getFallbackFolder();
		}
		return Path.of(latestLogOpt.get()).toAbsolutePath().getParent();

		/*return Optional.of(context)
				.filter(org.apache.logging.log4j.core.LoggerContext.class::isInstance)
				.map(org.apache.logging.log4j.core.LoggerContext.class::cast)
				.stream()
				.flatMap(c -> c.getConfiguration().getAppenders().values().stream())
				.filter(AbstractOutputStreamAppender.class::isInstance)
				.map(AbstractOutputStreamAppender.class::cast)
				.map(AbstractOutputStreamAppender::getManager)
				.filter(FileManager.class::isInstance)
				.map(FileManager.class::cast)
				.map(FileManager::getFileName)
				.filter(s -> s.contains("latest.log"))
				.map(File::new)
				.map(File::toPath)
				.map(Path::toAbsolutePath)
				.findFirst().orElseGet(() -> FabricLoader.getInstance().getGameDir().resolve("logs"));*/
	}

	private static Path getFallbackFolder() {
		return FabricLoader.getInstance().getGameDir().resolve("logs").toAbsolutePath();
	}
}
