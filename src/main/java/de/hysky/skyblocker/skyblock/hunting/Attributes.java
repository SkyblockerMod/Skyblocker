package de.hysky.skyblocker.skyblock.hunting;

import java.io.InputStream;
import java.util.List;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import io.github.moulberry.repo.NEURepoFile;
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
			attributes = AttributesFile.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(new String(stream.readAllBytes()))).getOrThrow().attributes();
			LOGGER.info("[Skyblocker Attributes] Successfully loaded attributes!");
		} catch (Exception ex) {
			LOGGER.error("[Skyblocker Attributes] Failed to load attributes!", ex);
		}
	}

	public static @Nullable Attribute getAttributeFromItemName(DataComponentHolder stack) {
		if (!stack.has(DataComponents.CUSTOM_NAME)) return null;
		String name = stack.get(DataComponents.CUSTOM_NAME).getString();

		return getAttributeFromItemName(name);
	}

	public static @Nullable Attribute getAttributeFromItemName(String name) {
		// Shards outside the hunting box now have "Shard" in their item name.
		// Dungeon chests also have x<number> after the "Shard" and this strips that too
		int index = name.indexOf("Shard");
		if (index > -1) name = name.substring(0, index - 1);

		name = name.replace("BUY ", "").replace("SELL ", ""); // Bazaar Buy/Sell orders

		for (Attribute attribute : attributes) {
			if (attribute.shardName().equals(name)) return attribute;
		}

		return null;
	}
}
