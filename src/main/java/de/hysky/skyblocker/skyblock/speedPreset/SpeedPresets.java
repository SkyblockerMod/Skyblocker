package de.hysky.skyblocker.skyblock.speedPreset;

import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.command.CommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class SpeedPresets {

	private static final Pattern COMMAND_PATTERN = Pattern.compile("^setmaxspeed\\s([a-zA-Z][a-zA-Z0-9_]*)$");

	private static final Logger LOGGER = LoggerFactory.getLogger(SpeedPresets.class);
	private static final Codec<Map<String, Short>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, Codec.SHORT);
	private static final File PRESETS_FILE = new File(SkyblockerMod.CONFIG_DIR.toFile(), "speed_presets.json");

	private static SpeedPresets instance;

	private final Map<String, Short> presets;

	private SpeedPresets() {
		this.presets = new LinkedHashMap<>();
		this.loadPresets();
	}

	public static SpeedPresets getInstance() {
		return instance == null ? instance = new SpeedPresets() : instance;
	}

	public static CommandNode<FabricClientCommandSource> getCommandNode() {
		return ClientCommandManager.literal("setmaxspeed")
				.requires(source -> Utils.isOnSkyblock())
				.then(ClientCommandManager.argument("preset", StringArgumentType.string())
						.suggests((ctx, builder) -> {
							if (SkyblockerConfigManager.get().general.speedPresets.enableSpeedPresets) {
								return CommandSource.suggestMatching(getInstance().presets.keySet(), builder);
							}
							return builder.buildFuture();
						})).build();
	}

	@Init
	public static void init() {
		ClientSendMessageEvents.MODIFY_COMMAND.register((command) -> {
			var matcher = COMMAND_PATTERN.matcher(command);
			if (matcher.matches() && SkyblockerConfigManager.get().general.speedPresets.enableSpeedPresets) {
				var presets = getInstance();
				var preset = matcher.group(1);
				if (presets.presets.containsKey(preset)) {
					return String.format("setmaxspeed %d", presets.getPreset(preset));
				}
			}
			return command;
		});
	}

	public void clear() {
		this.presets.clear();
	}

	public Set<Map.Entry<String, Short>> entries() {
		return this.presets.entrySet();
	}

	public boolean hasPreset(String name) {
		return this.presets.containsKey(name);
	}

	public short getPreset(String name) {
		return this.presets.getOrDefault(name, (short) 0);
	}

	public void setPreset(String name, short value) {
		this.presets.put(name, value);
		savePresets();
	}

	public void forEach(BiConsumer<String, Short> consumer) {
		this.presets.forEach(consumer);
	}

	public int getPresetCount() {
		return this.presets.size();
	}

	public void loadPresets() {
		try (var reader = Files.newReader(PRESETS_FILE, StandardCharsets.UTF_8)) {
			var element = JsonParser.parseReader(reader);
			MAP_CODEC.parse(JsonOps.INSTANCE, element).resultOrPartial(LOGGER::error).ifPresent(this.presets::putAll);
		} catch (FileNotFoundException e) {
			LOGGER.warn("[Skyblocker Speed Presets] Couldn't find speed presets file, creating one automatically...");
			this.loadDefaults();
			this.savePresets();
		} catch (IOException e) {
			LOGGER.warn("Couldn't load speed presets: ", e);
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void savePresets() {
		try {
			if (!PRESETS_FILE.exists()) PRESETS_FILE.createNewFile();
			try (var writer = Files.newWriter(PRESETS_FILE, StandardCharsets.UTF_8)) {
				var element = MAP_CODEC.encodeStart(JsonOps.INSTANCE, this.presets).resultOrPartial(LOGGER::error)
						.orElse(new JsonObject());
				writer.write(SkyblockerMod.GSON.toJson(element) + "\n");
			}
		} catch (IOException e) {
			LOGGER.error("[Skyblocker Speed Presets] Couldn't create speed presets file", e);
		}
	}

	// According to: https://www.reddit.com/r/HypixelSkyblock/comments/14kkz07/speed_vs_farming_fortune/
	public void loadDefaults() {
		this.presets.clear();
		this.presets.put("default", (short) 100);
		this.presets.put("crops", (short) 93);
		this.presets.put("cocoa", (short) 155);
		this.presets.put("mushroom", (short) 233);
		this.presets.put("cane", (short) 327);
		this.presets.put("squash", (short) 327);
		this.presets.put("cactus", (short) 464);
	}
}
