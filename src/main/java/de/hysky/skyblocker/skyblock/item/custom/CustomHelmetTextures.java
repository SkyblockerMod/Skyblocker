package de.hysky.skyblocker.skyblock.item.custom;

import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caches generated ProfileComponents for custom player head textures.
 */
public class CustomHelmetTextures {
    private static final Identifier TEXTURE_LIST = Identifier.of(SkyblockerMod.NAMESPACE, "textures/playerhead_textures.json");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Type LIST_TYPE = new TypeToken<List<NamedTexture>>() {}.getType();

    public static final List<NamedTexture> TEXTURES = new ArrayList<>();
    public static final Object2ObjectOpenHashMap<String, ProfileComponent> PROFILE_CACHE = new Object2ObjectOpenHashMap<>();

    public record NamedTexture(String name, String texture) {}

    @Init
    public static void init() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            try (BufferedReader reader = client.getResourceManager().openAsReader(TEXTURE_LIST)) {
                TEXTURES.addAll(SkyblockerMod.GSON.fromJson(reader, LIST_TYPE));
                LOGGER.info("[Skyblocker] Loaded {} helmet textures", TEXTURES.size());
            } catch (Exception e) {
                LOGGER.error("[Skyblocker] Failed to load helmet textures", e);
            }
        });
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
