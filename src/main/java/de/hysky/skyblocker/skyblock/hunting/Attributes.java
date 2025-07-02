package de.hysky.skyblocker.skyblock.hunting;

import java.io.BufferedReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.Identifier;

public class Attributes {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Identifier ATTRIBUTES_FILE = Identifier.of(SkyblockerMod.NAMESPACE, "hunting/attributes.json");
	private static List<Attribute> attributes = List.of();

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(Attributes::loadShards);
	}

	private static void loadShards(MinecraftClient client) {
		CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = client.getResourceManager().openAsReader(ATTRIBUTES_FILE)) {
				attributes = Attribute.LIST_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Attributes] Failed to load attributes.", e);
			}
		});
	}

	@Nullable
	public static Attribute getAttributeFromItemName(ComponentHolder stack) {
		if (!stack.contains(DataComponentTypes.CUSTOM_NAME)) return null;
		String name = stack.get(DataComponentTypes.CUSTOM_NAME).getString();

		for (Attribute attribute : attributes) {
			if (attribute.shardName().equals(name)) return attribute;
		}

		return null;
	}
}
