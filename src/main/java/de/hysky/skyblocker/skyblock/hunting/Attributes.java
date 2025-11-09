package de.hysky.skyblocker.skyblock.hunting;

import java.io.InputStream;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import io.github.moulberry.repo.NEURepoFile;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.NEURepoManager;

public class Attributes {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static List<Attribute> attributes = List.of();

	@Init
	public static void init() {
		NEURepoManager.runAsyncAfterLoad(Attributes::loadShards);
	}

	private static void loadShards() {
		NEURepoFile file = NEURepoManager.file("constants/attribute_shards.json");
		if (file == null) return;
		try (InputStream stream = file.stream()) {
			String data = new String(stream.readAllBytes());
			JsonObject fileJson = JsonParser.parseString(data).getAsJsonObject();
			JsonArray attributesJson = fileJson.get("attributes").getAsJsonArray();
			attributes = Attribute.LIST_CODEC.parse(JsonOps.INSTANCE, attributesJson).getOrThrow();
		} catch (Exception ex) {
			LOGGER.error("[Skyblocker Attributes] Failed to load attributes!", ex);
		}
	}

	@Nullable
	public static Attribute getAttributeFromItemName(ComponentHolder stack) {
		if (!stack.contains(DataComponentTypes.CUSTOM_NAME)) return null;
		String name = stack.get(DataComponentTypes.CUSTOM_NAME).getString();

		// Shards outside the hunting box now have "Shard" in their item name.
		int index = name.indexOf("Shard");
		if (index > -1) name = name.substring(0, index - 1);

		name = name.replace("BUY ", "").replace("SELL ", ""); // Bazaar Buy/Sell orders

		for (Attribute attribute : attributes) {
			if (attribute.shardName().equals(name)) return attribute;
		}

		return null;
	}
}
