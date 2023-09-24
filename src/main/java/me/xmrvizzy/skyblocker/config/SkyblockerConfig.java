package me.xmrvizzy.skyblocker.config;

import java.lang.StackWalker.Option;
import java.nio.file.Path;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.config.GsonConfigInstance;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.categories.DiscordRPCCategory;
import me.xmrvizzy.skyblocker.config.categories.DungeonsCategory;
import me.xmrvizzy.skyblocker.config.categories.DwarvenMinesCategory;
import me.xmrvizzy.skyblocker.config.categories.GeneralCategory;
import me.xmrvizzy.skyblocker.config.categories.MessageFilterCategory;
import me.xmrvizzy.skyblocker.config.categories.LocationsCategory;
import me.xmrvizzy.skyblocker.config.categories.QuickNavigationCategory;
import me.xmrvizzy.skyblocker.config.categories.SlayersCategory;
import me.xmrvizzy.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@SuppressWarnings("deprecation")
public class SkyblockerConfig {
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("skyblocker.json");
	private static final GsonConfigInstance<ConfigModel> INSTANCE = GsonConfigInstance.createBuilder(ConfigModel.class)
			.setPath(PATH)
			.overrideGsonBuilder(ConfigSerializer.INSTANCE)
			.build();
	/*private static final ConfigClassHandler<ConfigModel> HANDLER = ConfigClassHandler.createBuilder(ConfigModel.class)
			.serializer(config -> GsonConfigSerializerBuilder.create(config)
					.setPath(PATH)
					.setJson5(false)
					.overrideGsonBuilder(ConfigSerializer.INSTANCE)
					.build())
			.build();*/
	
	public static ConfigModel get() {
		return INSTANCE.getConfig();
	}
	
	/**
	 * This method is caller sensitive and can only be called by the mod initializer,
	 * this is enforced.
	 */
	public static void init() {
		if (StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass() != SkyblockerMod.class) {
			throw new RuntimeException("Skyblocker: Called config init from an illegal place!");
		}
		
		INSTANCE.load();
		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(optionsLiteral("config")).then(optionsLiteral("options")))));
	}
	
	public static void save() {
		INSTANCE.save();
	}
	
	public static Screen createGUI(Screen parent) {
		return YetAnotherConfigLib.create(INSTANCE, (defaults, config, builder) -> {
			return builder
					.title(Text.literal("Skyblocker"))
					.category(GeneralCategory.create(defaults, config))
					.category(DungeonsCategory.create(defaults, config))
					.category(DwarvenMinesCategory.create(defaults, config))
					.category(LocationsCategory.create(defaults, config))
					.category(SlayersCategory.create(defaults, config))
					.category(QuickNavigationCategory.create(defaults, config))
					.category(MessageFilterCategory.create(defaults, config))
					.category(DiscordRPCCategory.create(defaults, config));
					
		}).generateScreen(parent);
	}
	
	/**
	 * Registers an options command with the given name. Used for registering both options and config as valid commands.
	 *
	 * @param name the name of the command node
	 * @return the command builder
	 */
	private static LiteralArgumentBuilder<FabricClientCommandSource> optionsLiteral(String name) {
		// Don't immediately open the next screen as it will be closed by ChatScreen right after this command is executed
		return ClientCommandManager.literal(name).executes(Scheduler.queueOpenScreenCommand(() -> createGUI(null)));
	}
}
