package de.hysky.skyblocker.skyblock.speedpreset;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.JsonData;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeedPresets {
	private static final Pattern COMMAND_PATTERN = Pattern.compile("^setmaxspeed\\s([a-zA-Z]\\w*)$");
	private static final Codec<Object2IntMap<String>> MAP_CODEC = CodecUtils.object2IntMapCodec(Codec.STRING);
	private static final Path PRESETS_FILE = SkyblockerMod.CONFIG_DIR.resolve("speed_presets.json");

	private static SpeedPresets instance;

	private final JsonData<Object2IntMap<String>> presets = new JsonData<>(PRESETS_FILE, MAP_CODEC, createDefaultMap());
	private CompletableFuture<Void> loaded;

	private SpeedPresets() {} // Enforce singleton

	public static SpeedPresets getInstance() {
		return instance == null ? instance = new SpeedPresets() : instance;
	}

	public static CommandNode<FabricClientCommandSource> getCommandNode() {
		return ClientCommandManager.literal("setmaxspeed")
				.requires(source -> Utils.isOnSkyblock())
				.then(ClientCommandManager.argument("preset", StringArgumentType.string())
						.suggests((ctx, builder) -> {
							if (SkyblockerConfigManager.get().general.speedPresets.enableSpeedPresets && getInstance().loaded.isDone()) {
								return CommandSource.suggestMatching(instance.getPresets().keySet(), builder);
							}
							return builder.buildFuture();
						})).build();
	}

	@Init
	public static void init() {
		SpeedPresets instance = getInstance();
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> instance.loaded = instance.presets.init());
		ClientSendMessageEvents.MODIFY_COMMAND.register(command -> {
			Matcher matcher = COMMAND_PATTERN.matcher(command);
			if (matcher.matches() && SkyblockerConfigManager.get().general.speedPresets.enableSpeedPresets) {
				String preset = matcher.group(1);
				if (instance.hasPreset(preset)) {
					return String.format("setmaxspeed %d", instance.getPreset(preset));
				}
			}
			return command;
		});
	}

	public boolean hasPreset(String name) {
		return getPresets().containsKey(name);
	}

	public int getPreset(String name) {
		return getPresets().getOrDefault(name, 0);
	}

	public void setPreset(String name, int value) {
		getPresets().put(name, value);
		presets.save();
	}

	public boolean arePresetsEqual(Map<String, Integer> presets) {
		if (presets.size() != getPresets().size()) return false;
		for (Map.Entry<String, Integer> entry : presets.entrySet()) {
			if (!getPresets().containsKey(entry.getKey()) || getPresets().getInt(entry.getKey()) != entry.getValue()) {
				return false;
			}
		}
		return true;
	}

	// This method exists mainly to avoid nullability warnings
	@NotNull
	public Object2IntMap<String> getPresets() {
		//noinspection DataFlowIssue // There's a non-null default value, so this is safe
		return presets.getData();
	}

	public CompletableFuture<Void> savePresets() {
		return presets.save();
	}

	// According to: https://www.reddit.com/r/HypixelSkyblock/comments/14kkz07/speed_vs_farming_fortune/
	public static Object2IntOpenHashMap<String> createDefaultMap() {
		Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<>();
		map.put("default", 100);
		map.put("crops", 93);
		map.put("cocoa", 155);
		map.put("mushroom", 233);
		map.put("cane", 327);
		map.put("squash", 327);
		map.put("cactus", 464);
		return map;
	}
}
