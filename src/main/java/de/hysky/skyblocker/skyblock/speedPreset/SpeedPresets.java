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
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class SpeedPresets {

	private static final Pattern COMMAND_PATTERN = Pattern.compile("^setmaxspeed\\s([a-zA-Z][a-zA-Z0-9_]*)$");

	private static final Logger LOGGER = LoggerFactory.getLogger(SpeedPresets.class);
	private static final Codec<Map<String, Integer>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT);
	private static final File PRESETS_FILE = new File(SkyblockerMod.CONFIG_DIR.toFile(), "speed_presets.json");
	private static boolean attackWasHeld = false;

	private static SpeedPresets instance;

	private final Object2IntMap<String> presets;

	private SpeedPresets() {
		this.presets = new Object2IntOpenHashMap<>();
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
		ClientTickEvents.END_CLIENT_TICK.register(SpeedPresets::onSignAttack);
	}

	private static void onSignAttack(MinecraftClient client) {
		if (client == null || client.player == null || client.world == null) return;
		if (!(Utils.isOnSkyblock() && Utils.isInGarden())) return;

		if (!(client.crosshairTarget instanceof BlockHitResult hit && client.world.getBlockEntity(hit.getBlockPos()) instanceof SignBlockEntity sign)) return;
		ItemStack boots = client.player.getEquippedStack(EquipmentSlot.FEET);

		if (!boots.getSkyblockId().equals("RANCHERS_BOOTS")) return;

		boolean attackPressed = client.options.attackKey.isPressed();

		if (attackPressed && !attackWasHeld) {
			attackWasHeld = true;

			String firstLine = sign.getFrontText().getMessage(0, true).getString();
			if (!firstLine.isEmpty()) {
				if (MinecraftClient.getInstance().player != null) {
					MinecraftClient.getInstance().player.networkHandler.sendChatCommand("setmaxspeed " + firstLine);
				}
			}
		}

		if (!attackPressed) {
			attackWasHeld = false;
		}
	}

	public void clear() {
		this.presets.clear();
	}

	public boolean hasPreset(String name) {
		return this.presets.containsKey(name);
	}

	public int getPreset(String name) {
		return this.presets.getOrDefault(name, 0);
	}

	public void setPreset(String name, int value) {
		this.presets.put(name, value);
		savePresets();
	}

	public void forEach(BiConsumer<String, Integer> consumer) {
		this.presets.forEach(consumer);
	}

	public boolean arePresetsEqual(Map<String, Integer> presets) {
		return this.presets.equals(presets);
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
			LOGGER.error("[Skyblocker Speed Presets] Couldn't load speed presets", e);
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
		this.presets.put("default", 100);
		this.presets.put("crops", 93);
		this.presets.put("cocoa", 155);
		this.presets.put("mushroom", 233);
		this.presets.put("cane", 327);
		this.presets.put("squash", 327);
		this.presets.put("cactus", 464);
	}
}
