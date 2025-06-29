package de.hysky.skyblocker.skyblock.item.custom;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import io.github.moulberry.repo.data.NEUItem;
import net.minecraft.component.type.ProfileComponent;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Caches generated ProfileComponents for custom player head textures.
 */
public class CustomHelmetTextures {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final List<NamedTexture> TEXTURES = new ArrayList<>();
	public static final Object2ObjectOpenHashMap<String, ProfileComponent> PROFILE_CACHE = new Object2ObjectOpenHashMap<>();

	public record NamedTexture(String name, String texture, String internalName) {}

	private static final Pattern LEVEL_PATTERN = Pattern.compile("\\[Lvl[^\\]]*\\]");

	@Init
	public static void init() {
		NEURepoManager.runAsyncAfterLoad(CustomHelmetTextures::loadTextures);
	}

	private static void loadTextures() {
		try {
			Set<String> seen = new HashSet<>();
			for (NEUItem item : NEURepoManager.NEU_REPO.getItems().getItems().values()) {
				if (!"minecraft:skull".equals(item.getMinecraftItemId())) continue;
				String texture = extractTexture(item.getNbttag());
				if (texture == null || texture.isEmpty() || !seen.add(texture)) continue;
				String name = cleanName(item.getDisplayName());
				TEXTURES.add(new NamedTexture(name, texture, item.getSkyblockItemId()));
			}
			TEXTURES.sort(java.util.Comparator.comparing(NamedTexture::internalName));
			LOGGER.info("[Skyblocker] Loaded and sorted {} helmet textures from repo", TEXTURES.size());
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to load helmet textures from repo", e);
		}
	}

	private static String cleanName(String name) {
		return LEVEL_PATTERN.matcher(name).replaceAll("").trim();
	}

	private static String extractTexture(String nbt) {
		String lower = nbt.toLowerCase();
		int texIdx = lower.indexOf("textures");
		if (texIdx == -1) return null;
		String valKey = "Value:\"";
		int valIdx = nbt.indexOf(valKey, texIdx);
		if (valIdx == -1) return null;
		int start = valIdx + valKey.length();
		int end = nbt.indexOf("\"", start);
		if (end == -1) return null;
		return nbt.substring(start, end);
	}

	public static List<NamedTexture> getTextures() {
		return TEXTURES;
	}

	public static ProfileComponent getProfile(String texture) {
		return PROFILE_CACHE.computeIfAbsent(texture, (String t) ->
				new ProfileComponent(Optional.of("custom"),
						Optional.of(UUID.nameUUIDFromBytes(t.getBytes(StandardCharsets.UTF_8))),
						ItemUtils.propertyMapWithTexture(t)));
	}
}
