package de.hysky.skyblocker.config;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.backup.ConfigBackupManager;
import de.hysky.skyblocker.config.categories.ChatCategory;
import de.hysky.skyblocker.config.categories.CrimsonIsleCategory;
import de.hysky.skyblocker.config.categories.DebugCategory;
import de.hysky.skyblocker.config.categories.DungeonsCategory;
import de.hysky.skyblocker.config.categories.EventNotificationsCategory;
import de.hysky.skyblocker.config.categories.FarmingCategory;
import de.hysky.skyblocker.config.categories.ForagingCategory;
import de.hysky.skyblocker.config.categories.GeneralCategory;
import de.hysky.skyblocker.config.categories.HelperCategory;
import de.hysky.skyblocker.config.categories.HuntingCategory;
import de.hysky.skyblocker.config.categories.MiningCategory;
import de.hysky.skyblocker.config.categories.MiscCategory;
import de.hysky.skyblocker.config.categories.OtherLocationsCategory;
import de.hysky.skyblocker.config.categories.QuickNavigationCategory;
import de.hysky.skyblocker.config.categories.SlayersCategory;
import de.hysky.skyblocker.config.categories.UIAndVisualsCategory;
import de.hysky.skyblocker.config.datafixer.ConfigDataFixer;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
import de.hysky.skyblocker.utils.datafixer.JsonHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.azureaaron.dandelion.api.ConfigManager;
import net.azureaaron.dandelion.api.DandelionConfigScreen;
import net.azureaaron.dandelion.api.patching.ConfigPatch;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.StackWalker.Option;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import org.slf4j.Logger;

public class SkyblockerConfigManager {
	public static final int CONFIG_VERSION = 6;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	private static final Path CONFIG_FILE = CONFIG_DIR.resolve("skyblocker.json");
	private static final ConfigManager<SkyblockerConfig> CONFIG_MANAGER = ConfigManager.create(SkyblockerConfig.class, CONFIG_FILE, UnaryOperator.identity());

	public static SkyblockerConfig get() {
		return CONFIG_MANAGER.instance();
	}

	protected static SkyblockerConfig getUnpatched() {
		return CONFIG_MANAGER.unpatchedInstance();
	}

	/**
	 * This method is caller sensitive and can only be called by the mod initializer,
	 * this is enforced.
	 */
	public static void init() {
		if (StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass() != SkyblockerMod.class) {
			throw new RuntimeException("Skyblocker: Called config init from an illegal place!");
		}
		dataFix(CONFIG_FILE, CONFIG_DIR.resolve("skyblocker.json.old"));

		CONFIG_MANAGER.load();
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(configLiteral("config")).then(configLiteral("options"))));
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (get().uiAndVisuals.showConfigButton && screen instanceof ContainerScreen genericContainerScreen && screen.getTitle().getString().equals("SkyBlock Menu")) {
				Screens.getButtons(screen).add(Button
						.builder(Component.literal("\uD83D\uDD27"), buttonWidget -> client.setScreen(createGUI(screen)))
						.bounds(((AbstractContainerScreenAccessor) genericContainerScreen).getX() + ((AbstractContainerScreenAccessor) genericContainerScreen).getImageWidth() - 16, ((AbstractContainerScreenAccessor) genericContainerScreen).getY() + 4, 12, 12)
						.tooltip(Tooltip.create(Component.translatable("skyblocker.config.title", Component.translatable("skyblocker.config.title.settings"))))
						.build());
			}
		});
	}

	protected static void setPatches(List<ConfigPatch> patches) {
		CONFIG_MANAGER.setPatches(patches);
	}

	/**
	 * Executes the given {@code action} to update fields in the config, then saves the changes.
	 */
	public static void update(Consumer<SkyblockerConfig> action) {
		action.accept(getUnpatched());
		ConfigBackupManager.backupConfig();
		CONFIG_MANAGER.save();
	}

	/**
	 * Executes the given {@code action} to update fields in the config, without saving the changes.
	 */
	public static void updateOnly(Consumer<SkyblockerConfig> action) {
		action.accept(getUnpatched());
		CONFIG_MANAGER.updatePatchedInstance();
	}

	public static Screen createGUI(@Nullable Screen parent) {
		return createGUI(parent, "");
	}

	public static Screen createGUI(@Nullable Screen parent, String search) {
		return DandelionConfigScreen.create(CONFIG_MANAGER, (defaults, config, builder) -> builder
				.title(Component.translatable("skyblocker.config.title", SkyblockerMod.VERSION))
				.category(GeneralCategory.create(defaults, config))
				.category(UIAndVisualsCategory.create(defaults, config))
				.category(HelperCategory.create(defaults, config))
				.category(DungeonsCategory.create(defaults, config))
				.category(ForagingCategory.create(defaults, config))
				.category(CrimsonIsleCategory.create(defaults, config))
				.category(MiningCategory.create(defaults, config))
				.category(FarmingCategory.create(defaults, config))
				.category(HuntingCategory.create(defaults, config))
				.category(OtherLocationsCategory.create(defaults, config))
				.category(SlayersCategory.create(defaults, config))
				.category(ChatCategory.create(defaults, config))
				.category(QuickNavigationCategory.create(defaults, config))
				.category(EventNotificationsCategory.create(defaults, config))
				.category(MiscCategory.create(defaults, config))
				.categoryIf(Debug.debugEnabled(), DebugCategory.create(defaults, config))
				.search(search)
		).generateScreen(parent, get().misc.configBackend);
	}

	/**
	 * Returns the path to the config file.
	 */
	public static Path getConfigPath() {
		return CONFIG_FILE;
	}

	/**
	 * Reloads the config from disk.
	 */
	public static void reload() {
		CONFIG_MANAGER.load();
	}

	/**
	 * Registers a command argument to open the config.
	 *
	 * @return the command builder
	 */
	private static LiteralArgumentBuilder<FabricClientCommandSource> configLiteral(String name) {
		return literal(name).executes(Scheduler.queueOpenScreenCommand(() -> createGUI(null)))
				.then(argument("option", StringArgumentType.greedyString()).executes((ctx) -> Scheduler.queueOpenScreen(createGUI(null, ctx.getArgument("option", String.class)))));
	}

	public static void dataFix(Path configDir, Path backupDir) {
		//User is new - has no config file (or maybe config folder)
		if (!Files.exists(CONFIG_DIR) || !Files.exists(configDir)) return;

		//Should never be null if the file exists unless its malformed JSON or something in which case well it gets reset
		JsonObject oldConfig = loadConfig(configDir);
		if (oldConfig == null || JsonHelper.getInt(oldConfig, "version").orElse(1) == CONFIG_VERSION) return;

		JsonObject newConfig = ConfigDataFixer.apply(ConfigDataFixer.CONFIG_TYPE, oldConfig);

		//Write the updated file
		if (!writeConfig(configDir, newConfig)) {
			LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Data Fixer] Failed to fix up config file!");
			writeConfig(backupDir, oldConfig);
		}
	}

	private static @Nullable JsonObject loadConfig(Path path) {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		} catch (Throwable t) {
			LOGGER.error("[Skyblocker Config Data Fixer] Failed to load config file!", t);
		}

		return null;
	}

	private static boolean writeConfig(Path path, JsonObject config) {
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			SkyblockerMod.GSON.toJson(config, writer);

			return true;
		} catch (Throwable t) {
			LOGGER.error("[Skyblocker Config Data Fixer] Failed to save config file at {}!", path, t);
		}

		return false;
	}
}
