package me.xmrvizzy.skyblocker.skyblock.shortcut;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Shortcuts {
    private static final Logger LOGGER = LoggerFactory.getLogger(Shortcuts.class);
    private static final File SHORTCUTS_FILE = SkyblockerMod.CONFIG_DIR.resolve("shortcuts.json").toFile();
    private static CompletableFuture<Void> shortcutsLoaded;
    public static final Map<String, String> commands = new HashMap<>();
    public static final Map<String, String> commandArgs = new HashMap<>();

    public static void init() {
        shortcutsLoaded = CompletableFuture.runAsync(Shortcuts::loadShortcuts);
        ClientLifecycleEvents.CLIENT_STOPPING.register(Shortcuts::saveShortcuts);
        ClientCommandRegistrationCallback.EVENT.register(Shortcuts::registerCommands);
        ClientSendMessageEvents.MODIFY_COMMAND.register(Shortcuts::modifyCommand);
    }

    private static void loadShortcuts() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SHORTCUTS_FILE))) {
            Type shortcutsType = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
            Map<String, Map<String, String>> shortcuts = SkyblockerMod.GSON.fromJson(reader, shortcutsType);
            commands.putAll(shortcuts.get("commands"));
            commandArgs.putAll(shortcuts.get("commandArgs"));
            LOGGER.info("[Skyblocker] Loaded {} command shortcuts and {} command argument shortcuts", commands.size(), commandArgs.size());
        } catch (FileNotFoundException e) {
            registerDefaultShortcuts();
            LOGGER.warn("[Skyblocker] Shortcuts file not found, using default shortcuts. This is normal when using for the first time.", e);
        } catch (IOException e) {
            LOGGER.error("[Skyblocker] Failed to load shortcuts file", e);
        }
    }

    private static void registerDefaultShortcuts() {
        commands.put("/s", "/skyblock");
        commands.put("/sk", "/skyblock");
        commands.put("/sky", "/skyblock");
        commands.put("/i", "/is");
        commands.put("/h", "/hub");
        commands.put("/hu", "/hub");

        commands.put("/d", "/warp dungeon_hub");
        commands.put("/dn", "/warp dungeon_hub");
        commands.put("/dun", "/warp dungeon_hub");
        commands.put("/dungeon", "/warp dungeon_hub");

        commands.put("/bl", "/warp nether");
        commands.put("/blazing", "/warp nether");
        commands.put("/fortress", "/warp nether");
        commands.put("/crimson", "/warp nether");
        commands.put("/isles", "/warp nether");
        commands.put("/ci", "/warp nether");
        commands.put("/crimson isles", "/warp nether");
        commands.put("/n", "/warp nether");
        commands.put("/nether", "/warp nether");

        commands.put("/deep", "/warp deep");
        commands.put("/cavern", "/warp deep");
        commands.put("/caverns", "/warp deep");

        commands.put("/dw", "/warp mines");
        commands.put("/dwarven", "/warp mines");
        commands.put("/mi", "/warp mines");
        commands.put("/mines", "/warp mines");

        commands.put("/fo", "/warp forge");
        commands.put("/for", "/warp forge");
        commands.put("/forge", "/warp forge");

        commands.put("/cry", "/warp crystals");
        commands.put("/crystal", "/warp crystals");
        commands.put("/ho", "/warp crystals");
        commands.put("/hollows", "/warp crystals");
        commands.put("/ch", "/warp crystals");
        commands.put("/crystal hollows", "/warp crystals");

        commands.put("/ga", "/warp garden");
        commands.put("/garden", "/warp garden");
        commands.put("/go", "/warp gold");
        commands.put("/gold", "/warp gold");

        commands.put("/des", "/warp desert");
        commands.put("/desert", "/warp desert");
        commands.put("/mu", "/warp desert");
        commands.put("/mushroom", "/warp desert");

        commands.put("/sp", "/warp spider");
        commands.put("/spider", "/warp spider");
        commands.put("/spiders", "/warp spider");

        commands.put("/ba", "/warp barn");
        commands.put("/barn", "/warp barn");

        commands.put("/e", "/warp end");
        commands.put("/end", "/warp end");

        commands.put("/park", "/warp park");

        commands.put("/castle", "/warp castle");
        commands.put("/museum", "/warp museum");
        commands.put("/da", "/warp da");
        commands.put("/dark", "/warp da");
        commands.put("/crypt", "/warp crypt");
        commands.put("/crypts", "/warp crypt");
        commands.put("/nest", "/warp nest");
        commands.put("/magma", "/warp magma");
        commands.put("/void", "/warp void");
        commands.put("/drag", "/warp drag");
        commands.put("/dragon", "/warp drag");
        commands.put("/jungle", "/warp jungle");
        commands.put("/howl", "/warp howl");

        commands.put("/ca", "/chat all");
        commands.put("/cp", "/chat party");
        commands.put("/cg", "/chat guild");
        commands.put("/co", "/chat officer");
        commands.put("/cc", "/chat coop");

        commandArgs.put("/m", "/msg");

        commandArgs.put("/pa", "/p accept");
        commands.put("/pv", "/p leave");
        commands.put("/pd", "/p disband");
        commands.put("/rp", "/reparty");
        commands.put("/pr", "/reparty");

        commandArgs.put("/v", "/visit");
        commands.put("/vp", "/visit portalhub");
        commands.put("/visit p", "/visit portalhub");
    }

    private static void saveShortcuts(MinecraftClient client) {
        JsonObject shortcutsJson = new JsonObject();
        shortcutsJson.add("commands", SkyblockerMod.GSON.toJsonTree(commands));
        shortcutsJson.add("commandArgs", SkyblockerMod.GSON.toJsonTree(commandArgs));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SHORTCUTS_FILE))) {
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
        for (String key : commandArgs.keySet()) {
            if (key.startsWith("/")) {
                dispatcher.register(literal(key.substring(1)));
            }
        }
        dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("help").executes(context -> {
            FabricClientCommandSource source = context.getSource();
            String status = SkyblockerConfig.get().general.shortcuts.enableShortcuts && SkyblockerConfig.get().general.shortcuts.enableCommandShortcuts ? "§a§l (Enabled)" : "§c§l (Disabled)";
            source.sendFeedback(Text.of("§e§lSkyblocker §fCommand Shortcuts" + status));
            if (!shortcutsLoaded.isDone()) {
                source.sendFeedback(Text.of("§c§lShortcuts not loaded yet"));
            } else for (Map.Entry<String, String> command : commands.entrySet()) {
                source.sendFeedback(Text.of("§7" + command.getKey() + " §f→ §7" + command.getValue()));
            }
            status = SkyblockerConfig.get().general.shortcuts.enableShortcuts && SkyblockerConfig.get().general.shortcuts.enableCommandArgShortcuts ? "§a§l (Enabled)" : "§c§l (Disabled)";
            source.sendFeedback(Text.of("§e§lSkyblocker §fCommand Argument Shortcuts" + status));
            if (!shortcutsLoaded.isDone()) {
                source.sendFeedback(Text.of("§c§lShortcuts not loaded yet"));
            } else for (Map.Entry<String, String> commandArg : commandArgs.entrySet()) {
                source.sendFeedback(Text.of("§7" + commandArg.getKey() + " §f→ §7" + commandArg.getValue()));
            }
            source.sendFeedback(Text.of("§e§lSkyblocker §fCommands"));
            for (String command : dispatcher.getSmartUsage(dispatcher.getRoot().getChild(SkyblockerMod.NAMESPACE), source).values()) {
                source.sendFeedback(Text.of("§7/" + SkyblockerMod.NAMESPACE + " " + command));
            }
            return Command.SINGLE_SUCCESS;
        })));
    }

    private static String modifyCommand(String command) {
        if (Utils.isOnHypixel() && SkyblockerConfig.get().general.shortcuts.enableShortcuts) {
            if (!shortcutsLoaded.isDone()) {
                LOGGER.warn("[Skyblocker] Shortcuts not loaded yet, skipping shortcut for command: {}", command);
                return command;
            }
            command = '/' + command;
            if (SkyblockerConfig.get().general.shortcuts.enableCommandShortcuts) {
                command = commands.getOrDefault(command, command);
            }
            if (SkyblockerConfig.get().general.shortcuts.enableCommandArgShortcuts) {
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
