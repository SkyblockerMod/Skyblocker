package de.hysky.skyblocker.skyblock.shortcut;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Shortcuts {
    private static final Logger LOGGER = LoggerFactory.getLogger(Shortcuts.class);
    private static final Path SHORTCUTS_FILE = SkyblockerMod.CONFIG_DIR.resolve("shortcuts.json");
    @Nullable
    private static CompletableFuture<Void> shortcutsLoaded;
    public static final Map<String, String> commands = new HashMap<>();
    public static final Map<String, String> commandArgs = new HashMap<>();

    public static boolean isShortcutsLoaded() {
        return shortcutsLoaded != null && shortcutsLoaded.isDone();
    }

    @Init
    public static void init() {
        loadShortcuts();
        ClientLifecycleEvents.CLIENT_STOPPING.register(Shortcuts::saveShortcuts);
        ClientCommandRegistrationCallback.EVENT.register(Shortcuts::registerCommands);
        ClientSendMessageEvents.MODIFY_COMMAND.register(Shortcuts::modifyCommand);
    }

    protected static void loadShortcuts() {
        if (shortcutsLoaded != null && !isShortcutsLoaded()) {
            return;
        }
        shortcutsLoaded = CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = Files.newBufferedReader(SHORTCUTS_FILE)) {
                Type shortcutsType = new TypeToken<Map<String, Map<String, String>>>() {
                }.getType();
                Map<String, Map<String, String>> shortcuts = SkyblockerMod.GSON.fromJson(reader, shortcutsType);
                commands.clear();
                commandArgs.clear();
                commands.putAll(shortcuts.get("commands"));
                commandArgs.putAll(shortcuts.get("commandArgs"));
                LOGGER.info("[Skyblocker] Loaded {} command shortcuts and {} command argument shortcuts", commands.size(), commandArgs.size());
            } catch (NoSuchFileException e) {
                registerDefaultShortcuts();
                LOGGER.warn("[Skyblocker] Shortcuts file not found, using default shortcuts. This is normal when using for the first time.");
            } catch (IOException e) {
                LOGGER.error("[Skyblocker] Failed to load shortcuts file", e);
            }
        });
    }

    private static void registerDefaultShortcuts() {
        commands.clear();
        commandArgs.clear();

        // Skyblock
        commands.put("/s", "/skyblock");
        commands.put("/i", "/is");
        commands.put("/h", "/hub");

        // Dungeon
        commands.put("/d", "/warp dungeon_hub");

        // Chat channels
        commands.put("/ca", "/chat all");
        commands.put("/cp", "/chat party");
        commands.put("/cg", "/chat guild");
        commands.put("/co", "/chat officer");
        commands.put("/cc", "/chat coop");

        // Message
        commandArgs.put("/m", "/msg");

        // Party
        commandArgs.put("/pa", "/p accept");
        commands.put("/pd", "/p disband");
        commands.put("/rp", "/reparty");

        // Visit
        commandArgs.put("/v", "/visit");
        commands.put("/vp", "/visit portalhub");
    }

    @SuppressWarnings("unused")
    private static void registerMoreDefaultShortcuts() {
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
    }

    protected static void saveShortcuts(MinecraftClient client) {
        JsonObject shortcutsJson = new JsonObject();
        shortcutsJson.add("commands", SkyblockerMod.GSON.toJsonTree(commands));
        shortcutsJson.add("commandArgs", SkyblockerMod.GSON.toJsonTree(commandArgs));
        try (BufferedWriter writer = Files.newBufferedWriter(SHORTCUTS_FILE)) {
            SkyblockerMod.GSON.toJson(shortcutsJson, writer);
            LOGGER.info("[Skyblocker] Saved {} command shortcuts and {} command argument shortcuts", commands.size(), commandArgs.size());
        } catch (IOException e) {
            LOGGER.error("[Skyblocker] Failed to save shortcuts file", e);
        }
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        for (String key : commands.keySet()) {
            if (key.startsWith("/")) {
                dispatcher.register(literal(key.substring(1)));
            }
        }
        for (Map.Entry<String, String> set : commandArgs.entrySet()) {
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
                }
                else {
                    dispatcher.register(literal(set.getKey().substring(1)).redirect(redirectLocation));
                }
            }
        }
        dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("help").executes(context -> {
            FabricClientCommandSource source = context.getSource();
            String status = SkyblockerConfigManager.get().general.shortcuts.enableShortcuts && SkyblockerConfigManager.get().general.shortcuts.enableCommandShortcuts ? "§a§l (Enabled)" : "§c§l (Disabled)";
            source.sendFeedback(Text.of("§e§lSkyblocker §fCommand Shortcuts" + status));
            if (!isShortcutsLoaded()) {
                source.sendFeedback(Text.translatable("skyblocker.shortcuts.notLoaded"));
            } else for (Map.Entry<String, String> command : commands.entrySet()) {
                source.sendFeedback(Text.of("§7" + command.getKey() + " §f→ §7" + command.getValue()));
            }
            status = SkyblockerConfigManager.get().general.shortcuts.enableShortcuts && SkyblockerConfigManager.get().general.shortcuts.enableCommandArgShortcuts ? "§a§l (Enabled)" : "§c§l (Disabled)";
            source.sendFeedback(Text.of("§e§lSkyblocker §fCommand Argument Shortcuts" + status));
            if (!isShortcutsLoaded()) {
                source.sendFeedback(Text.translatable("skyblocker.shortcuts.notLoaded"));
            } else for (Map.Entry<String, String> commandArg : commandArgs.entrySet()) {
                source.sendFeedback(Text.of("§7" + commandArg.getKey() + " §f→ §7" + commandArg.getValue()));
            }
            source.sendFeedback(Text.of("§e§lSkyblocker §fCommands"));
            for (String command : dispatcher.getSmartUsage(dispatcher.getRoot().getChild(SkyblockerMod.NAMESPACE), source).values()) {
                source.sendFeedback(Text.of("§7/" + SkyblockerMod.NAMESPACE + " " + command));
            }
            return Command.SINGLE_SUCCESS;
            // Queue the screen or else the screen will be immediately closed after executing this command
        })).then(literal("shortcuts").executes(Scheduler.queueOpenScreenCommand(ShortcutsConfigScreen::new))));
    }

    private static String modifyCommand(String command) {
        if (SkyblockerConfigManager.get().general.shortcuts.enableShortcuts) {
            if (!isShortcutsLoaded()) {
                LOGGER.warn("[Skyblocker] Shortcuts not loaded yet, skipping shortcut for command: {}", command);
                return command;
            }
            command = '/' + command;
            if (SkyblockerConfigManager.get().general.shortcuts.enableCommandShortcuts) {
                command = commands.getOrDefault(command, command);
            }
            if (SkyblockerConfigManager.get().general.shortcuts.enableCommandArgShortcuts) {
                String[] messageArgs = command.split(" ");
                for (int i = 0; i < messageArgs.length; i++) {
                    messageArgs[i] = commandArgs.getOrDefault(messageArgs[i], messageArgs[i]);
                }
                command = String.join(" ", messageArgs);
            }
            return command.substring(1);
        }
        return command;
    }
}
