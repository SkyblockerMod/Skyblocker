package de.hysky.skyblocker.skyblock.item.custom;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Items;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
		ItemRepository.runAsyncAfterImport(CustomHelmetTextures::loadTextures);
	}

	private static void loadTextures() {
		try {
			if (!ItemRepository.filesImported()) return;

			TEXTURES.clear();
			ObjectSet<String> seen = new ObjectOpenHashSet<>();
			ItemRepository.getItemsStream()
					.filter(stack -> stack.isOf(Items.PLAYER_HEAD))
					.forEach(stack -> {
						String texture = ItemUtils.getHeadTexture(stack);
						if (texture.isEmpty() || !seen.add(texture)) return;
						String name = cleanName(stack.getName().getString());
						TEXTURES.add(new NamedTexture(name, texture, stack.getNeuName()));
					});

			TEXTURES.sort(java.util.Comparator.comparing(NamedTexture::internalName));
			LOGGER.info("[Skyblocker] Loaded and sorted {} helmet textures from repo", TEXTURES.size());
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to load helmet textures from repo", e);
		}
	}

	private static String cleanName(String name) {
		return LEVEL_PATTERN.matcher(name).replaceAll("").trim();
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
