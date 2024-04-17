package de.hysky.skyblocker.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProfileUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProfileUtils.class);
    private static final long HYPIXEL_API_COOLDOWN = 300000; // 5min = 300000

    public static Map<String, ObjectLongPair<JsonObject>> players = new HashMap<>();

    public static void init() {
        updateProfile();
    }

    public static CompletableFuture<JsonObject> updateProfile() {
        return updateProfile(MinecraftClient.getInstance().getSession().getUsername());
    }

    public static CompletableFuture<JsonObject> updateProfile(String name) {
        ObjectLongPair<JsonObject> playerCache = players.get(name);
        if (playerCache != null && playerCache.rightLong() + HYPIXEL_API_COOLDOWN > System.currentTimeMillis()) {
            return CompletableFuture.completedFuture(playerCache.left());
        }

        return CompletableFuture.supplyAsync(() -> {
            String uuid = ApiUtils.name2Uuid(name);
            try (Http.ApiResponse response = Http.sendHypixelRequest("skyblock/profiles", "?uuid=" + uuid)) {
                if (!response.ok()) {
                    throw new IllegalStateException("Failed to get profile uuid for players " + name + "! Response: " + response.content());
                }
                JsonObject responseJson = SkyblockerMod.GSON.fromJson(response.content(), JsonObject.class);

                JsonObject player = responseJson.getAsJsonArray("profiles").asList().stream()
                        .map(JsonElement::getAsJsonObject)
                        .filter(profile -> profile.getAsJsonPrimitive("selected").getAsBoolean())
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No selected profile found!?"))
                        .getAsJsonObject("members").get(uuid).getAsJsonObject();

                players.put(name, ObjectLongPair.of(player, System.currentTimeMillis()));
                return player;
            } catch (Exception e) {
                LOGGER.error("[Skyblocker Profile Utils] Failed to get Player Profile Data for players {}, is the API Down/Limited?", name, e);
            }
            return null;
        });
    }
}
