package de.hysky.skyblocker.skyblock.shortcut;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.data.JsonData;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Shortcuts {
	private static final Logger LOGGER = LoggerFactory.getLogger(Shortcuts.class);
	private static final Path SHORTCUTS_FILE = SkyblockerMod.CONFIG_DIR.resolve("shortcuts.json");
	public static final JsonData<ShortcutsRecord> shortcuts = new JsonData<>(SHORTCUTS_FILE, ShortcutsRecord.CODEC, getDefaultShortcuts());

	/**
	 * Keys that are currently held down.
	 *
	 * @see net.minecraft.client.option.KeyBinding#isPressed() KeyBinding#isPressed()
	 */
	private static final List<InputUtil.Key> pressedKeys = new ArrayList<>();
	private static final long KEY_BINDING_COOLDOWN = 200;
	private static long lastKeyBindingCommandTime;

	public static boolean isShortcutsLoaded() {
		return shortcuts.isLoaded();
	}

	@Init
	public static void init() {
		shortcuts.init();
		ClientCommandRegistrationCallback.EVENT.register(Shortcuts::registerCommands);
		ClientSendMessageEvents.MODIFY_COMMAND.register(Shortcuts::modifyCommand);
	}

	private static ShortcutsRecord getDefaultShortcuts() {
		Object2ObjectOpenHashMap<String, String> commands = new Object2ObjectOpenHashMap<>();
		Object2ObjectOpenHashMap<String, String> commandArgs = new Object2ObjectOpenHashMap<>();

		// Skyblock
		commands.put("/s", "/skyblock");
		commands.put("/i", "/is");
		commands.put("/h", "/hub");
		commands.put("/ga", "/warp garden");

		// Dungeon
		commands.put("/d", "/warp dungeon_hub");

		// Chat channels
		commands.put("/ca", "/chat all");
		commands.put("/cp", "/chat party");
		commands.put("/cg", "/chat guild");
		commands.put("/co", "/chat officer");

		// Message
		commandArgs.put("/m", "/msg");

		// Party
		commandArgs.put("/pa", "/p accept");
		commands.put("/pd", "/p disband");

		// Visit
		commandArgs.put("/v", "/visit");
		commands.put("/vp", "/visit portalhub");

		return new ShortcutsRecord(commands, commandArgs, new Object2ObjectOpenHashMap<>());
	}

	@SuppressWarnings("unused")
	private static ShortcutsRecord getMoreDefaultShortcuts() {
		Object2ObjectOpenHashMap<String, String> commands = new Object2ObjectOpenHashMap<>();
		Object2ObjectOpenHashMap<String, String> commandArgs = new Object2ObjectOpenHashMap<>();

		// Combat
		commands.put("/spider", "/warp spider");
		commands.put("/crimson", "/warp nether");
		commands.put("/end", "/warp end");

		// Mining
		commands.put("/gold", "/warp gold");
		commands.put("/cavern", "/warp deep");
		commands.put("/dwarven", "/warp mines");
		commands.put("/fo", "/warp forge");
		commands.put("/ch", "/warp crystals");

		// Foraging & Farming
		commands.put("/park", "/warp park");
		commands.put("/barn", "/warp barn");
		commands.put("/desert", "/warp desert");
		commands.put("/ga", "/warp garden");

		// Other warps
		commands.put("/castle", "/warp castle");
		commands.put("/museum", "/warp museum");
		commands.put("/da", "/warp da");
		commands.put("/crypt", "/warp crypt");
		commands.put("/nest", "/warp nest");
		commands.put("/magma", "/warp magma");
		commands.put("/void", "/warp void");
		commands.put("/drag", "/warp drag");
		commands.put("/jungle", "/warp jungle");
		commands.put("/howl", "/warp howl");

		return new ShortcutsRecord(commands, commandArgs, new Object2ObjectOpenHashMap<>());
	}

	private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("help").executes(context -> {
			FabricClientCommandSource source = context.getSource();
			String status = SkyblockerConfigManager.get().general.shortcuts.enableShortcuts && SkyblockerConfigManager.get().general.shortcuts.enableCommandShortcuts ? "§a§l (Enabled)" : "§c§l (Disabled)";
			source.sendFeedback(Text.of("§e§lSkyblocker §fCommand Shortcuts" + status));
			if (!isShortcutsLoaded()) {
				source.sendFeedback(Text.translatable("skyblocker.shortcuts.notLoaded"));
			} else for (Map.Entry<String, String> command : shortcuts.getData().commands.entrySet()) {
				source.sendFeedback(Text.of("§7" + command.getKey() + " §f→ §7" + command.getValue()));
			}

			status = SkyblockerConfigManager.get().general.shortcuts.enableShortcuts && SkyblockerConfigManager.get().general.shortcuts.enableCommandArgShortcuts ? "§a§l (Enabled)" : "§c§l (Disabled)";
			source.sendFeedback(Text.of("§e§lSkyblocker §fCommand Argument Shortcuts" + status));
			if (!isShortcutsLoaded()) {
				source.sendFeedback(Text.translatable("skyblocker.shortcuts.notLoaded"));
			} else for (Map.Entry<String, String> commandArg : shortcuts.getData().commandArgs.entrySet()) {
				source.sendFeedback(Text.of("§7" + commandArg.getKey() + " §f→ §7" + commandArg.getValue()));
			}

			status = SkyblockerConfigManager.get().general.shortcuts.enableShortcuts && SkyblockerConfigManager.get().general.shortcuts.enableKeyBindingShortcuts ? "§a§l (Enabled)" : "§c§l (Disabled)";
			source.sendFeedback(Text.of("§e§lSkyblocker §fKey Binding Shortcuts" + status));
			if (!isShortcutsLoaded()) {
				source.sendFeedback(Text.translatable("skyblocker.shortcuts.notLoaded"));
			} else for (Map.Entry<ShortcutKeyBinding, String> keyBinding : shortcuts.getData().keyBindings.entrySet()) {
				source.sendFeedback(Text.of("§7" + keyBinding.getKey().getBoundKeysText().getString() + " §f→ §7" + keyBinding.getValue()));
			}

			source.sendFeedback(Text.of("§e§lSkyblocker §fCommands"));
			for (String command : dispatcher.getSmartUsage(dispatcher.getRoot().getChild(SkyblockerMod.NAMESPACE), source).values()) {
				source.sendFeedback(Text.of("§7/" + SkyblockerMod.NAMESPACE + " " + command));
			}
			return Command.SINGLE_SUCCESS;
			// Queue the screen or else the screen will be immediately closed after executing this command
		})).then(literal("shortcuts").executes(Scheduler.queueOpenScreenCommand(ShortcutsConfigScreen::new))));

		if (!SkyblockerConfigManager.get().general.shortcuts.enableShortcuts) return;
		if (!isShortcutsLoaded()) {
			LOGGER.warn("[Skyblocker Shortcuts] Shortcuts not loaded yet, skipping command registration");
			return;
		}
		for (String key : shortcuts.getData().commands.keySet()) {
			if (key.startsWith("/")) {
				dispatcher.register(literal(key.substring(1)));
			}
		}
		for (Map.Entry<String, String> set : shortcuts.getData().commandArgs.entrySet()) {
			if (set.getKey().startsWith("/")) {
				CommandNode<FabricClientCommandSource> redirectLocation = dispatcher.getRoot();
				for (String word : set.getValue().substring(1).split(" ")) {
					redirectLocation = redirectLocation.getChild(word);
					if (redirectLocation == null) {
						break;
					}
				}
				if (redirectLocation == null) {
					dispatcher.register(literal(set.getKey().substring(1)).then(argument("args", StringArgumentType.greedyString())));
				} else {
					dispatcher.register(literal(set.getKey().substring(1)).redirect(redirectLocation));
				}
			}
		}
	}

	private static String modifyCommand(String command) {
		if (!SkyblockerConfigManager.get().general.shortcuts.enableShortcuts) return command;
		if (!isShortcutsLoaded()) {
			LOGGER.warn("[Skyblocker Shortcuts] Shortcuts not loaded yet, skipping shortcut for command: {}", command);
			return command;
		}

		command = '/' + command;
		if (SkyblockerConfigManager.get().general.shortcuts.enableCommandShortcuts) {
			command = shortcuts.getData().commands.getOrDefault(command, command);
		}
		if (SkyblockerConfigManager.get().general.shortcuts.enableCommandArgShortcuts) {
			String[] messageArgs = command.split(" ");
			for (int i = 0; i < messageArgs.length; i++) {
				messageArgs[i] = shortcuts.getData().commandArgs.getOrDefault(messageArgs[i], messageArgs[i]);
			}
			command = String.join(" ", messageArgs);
		}
		return command.substring(1);
	}

	public static void onKeyPressed(InputUtil.Key key) {
		if (!SkyblockerConfigManager.get().general.shortcuts.enableShortcuts || !SkyblockerConfigManager.get().general.shortcuts.enableKeyBindingShortcuts) return;
		if (!isShortcutsLoaded()) {
			LOGGER.warn("[Skyblocker Shortcuts] Shortcuts not loaded yet, skipping key binding check for key: {}", key);
			return;
		}

		String command = shortcuts.getData().keyBindings.get(new ShortcutKeyBinding(List.of(key)));
		// Check for combinations
		if (command == null) {
			// This should never happen, but the last pressed key was not added to the list!
			if (pressedKeys.isEmpty() || !pressedKeys.getLast().equals(key)) {
				setKeyPressed(key, true);
				LOGGER.warn("[Skyblocker Shortcuts] Key {} was not in the pressed keys list when it should be. Check if `setKeyPressed` is always called before `onKeyPressed`.", key);
			}
			command = shortcuts.getData().keyBindings.get(new ShortcutKeyBinding(pressedKeys));
		}
		if (command == null || lastKeyBindingCommandTime + KEY_BINDING_COOLDOWN > System.currentTimeMillis()) return;

		MessageScheduler.INSTANCE.sendMessageAfterCooldown(command, true);
		lastKeyBindingCommandTime = System.currentTimeMillis();
	}

	public static void setKeyPressed(InputUtil.Key key, boolean pressed) {
		if (!SkyblockerConfigManager.get().general.shortcuts.enableShortcuts || !SkyblockerConfigManager.get().general.shortcuts.enableKeyBindingShortcuts) return;
		if (pressed) {
			if (pressedKeys.isEmpty() || !pressedKeys.getLast().equals(key)) pressedKeys.add(key);
		} else {
			pressedKeys.remove(key);
		}
	}

	public record ShortcutsRecord(Object2ObjectMap<String, String> commands, Object2ObjectMap<String, String> commandArgs, Object2ObjectMap<ShortcutKeyBinding, String> keyBindings) {
		@VisibleForTesting
		static final Codec<ShortcutsRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				CodecUtils.object2ObjectMapCodec(Codec.STRING, Codec.STRING).fieldOf("commands").forGetter(ShortcutsRecord::commands),
				CodecUtils.object2ObjectMapCodec(Codec.STRING, Codec.STRING).fieldOf("commandArgs").forGetter(ShortcutsRecord::commandArgs),
				CodecUtils.mutableOptional(CodecUtils.object2ObjectMapCodec(ShortcutKeyBinding.CODEC, Codec.STRING).optionalFieldOf("keyBindings", Object2ObjectMaps.emptyMap()), Object2ObjectOpenHashMap::new).forGetter(ShortcutsRecord::keyBindings)
		).apply(instance, ShortcutsRecord::new));

		public int size() {
			return commands.size() + commandArgs.size() + keyBindings.size();
		}

		public void clear() {
			commands.clear();
			commandArgs.clear();
			keyBindings.clear();
		}
	}
}
