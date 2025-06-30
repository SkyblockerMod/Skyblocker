package de.hysky.skyblocker.skyblock.hunting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import de.hysky.skyblocker.utils.container.ContainerSolver;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;

public class AttributesDebug {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final List<Attribute> DUMPED_ATTRIBUTES = new ArrayList<>();
	private static final Pattern SOURCE_PATTERN = Pattern.compile("Source: (?<shardName>[A-za-z ]+) Shard \\((?<id>[CUREL]\\d+)\\)");
	private static final Path ATTRIBUTE_EXPORT_DEST = SkyblockerMod.CONFIG_DIR.resolve("attribute_export.json");

	//@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
				if (key == GLFW.GLFW_KEY_G) {
					dumpAttributes();
				} else if (key == GLFW.GLFW_KEY_J) {
					exportAttributes();
				}
			});
		});
	}

	private static void dumpAttributes() {
		if (CLIENT.currentScreen instanceof HandledScreen<?> screen && screen.getTitle().getString().equals("Attribute Menu")) {
			@SuppressWarnings("unchecked")
			Int2ObjectMap<ItemStack> slots = ContainerSolverManager.slotMap(screen.getScreenHandler().slots.subList(0, ((HandledScreen<GenericContainerScreenHandler>) screen).getScreenHandler().getRows() * 9));
			ContainerSolver.trimEdges(slots, 6);

			for (ItemStack stack : slots.values()) {
				if (stack.isEmpty()) continue;

				String name = stack.getName().getString();
				Matcher sourceMatcher = ItemUtils.getLoreLineIfMatch(stack, SOURCE_PATTERN);

				//Remove roman numeral from name
				List<String> words = new ArrayList<>(Arrays.asList(name.split(" ")));
				if (RomanNumerals.isValidRomanNumeral(words.getLast().strip())) {
					words.removeLast();
					name = String.join(" ", words);
				}

				if (sourceMatcher != null) {
					String shardName = sourceMatcher.group("shardName");
					String id = sourceMatcher.group("id");
					String apiIdGuess = "SHARD_" + shardName.replace(' ', '_').toUpperCase(Locale.ENGLISH);
					boolean hasDataForId = TooltipInfoType.BAZAAR.getData().containsKey(apiIdGuess);

					//Most attributes follow the format above but some have different ids so this is to catch those ones
					if (!hasDataForId) LOGGER.warn("[Skyblocker Attributes Debug] No data found for shard. Shard Name: {}", shardName);

					Attribute attribute = new Attribute(name, shardName, id, apiIdGuess);
					DUMPED_ATTRIBUTES.add(attribute);
				} else {
					LOGGER.warn("[Skyblocker Attributes Debug] Failed to match shard! Name: {}", name);
				}
			}
		}
	}

	private static void exportAttributes() {
		if (CLIENT.currentScreen instanceof HandledScreen screen && screen.getTitle().getString().equals("Attribute Menu")) {
			List<Attribute> copy = DUMPED_ATTRIBUTES.stream().distinct().toList();

			CompletableFuture.runAsync(() -> {
				try {
					Files.writeString(ATTRIBUTE_EXPORT_DEST, Attribute.LIST_CODEC.encodeStart(JsonOps.INSTANCE, copy).getOrThrow().toString());
				} catch (Exception e) {
					LOGGER.error("[Skyblocker Attributes Debug] Failed to export attributes!", e);
				}
			});
		}
	}
}
