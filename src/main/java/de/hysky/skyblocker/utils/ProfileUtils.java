package de.hysky.skyblocker.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.events.SkyblockEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProfileUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProfileUtils.class);
    private static final long HYPIXEL_API_COOLDOWN = 300000; // 5min = 300000

    public static Map<String, List<Profile>> players = new HashMap<>();
    private static boolean waitingForNextMessage = false;

    public static void init() {
        ClientReceiveMessageEvents.GAME.register(ProfileUtils::onMessage);
    }

    private static void onMessage(Text message, boolean overlay) {
        if (overlay) return;
        String str = message.getString();
        if (str.startsWith("§aYour profile was changed to:")) {
            Profile profile = getSelectedProfile(players.get(getUsername()));
            if (profile != null) {
                profile.selected = false;
            }
            waitingForNextMessage = true;
        } else if (str.startsWith("§aYou are playing on profile:")) {
            //This is already set by the api request, but it might've been cached, so we should still update it when possible
            waitingForNextMessage = true;
        } else if (waitingForNextMessage && str.startsWith("Profile ID: ")) {
            String profileId = str.substring(12);
            List<Profile> profiles = players.get(getUsername());
            if (profiles != null) {
                for (Profile profile : profiles) {
                    if (profile.uuid.equals(profileId)) {
                        profile.selected = true;
                        break;
                    }
                }
            }
            SkyblockEvents.PROFILE_CHANGE.invoker().onSkyblockProfileChange();
            waitingForNextMessage = false;
        }
    }

    /**
     * Convenience method to get the current player's username.
     */
    private static String getUsername() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }

    /**
     * Updates the profile data for the current player, or returns the cached data if it's still valid.
     * @see #updateProfile(String)
     */
    public static CompletableFuture<JsonObject> updateProfile() {
        return updateProfile(getUsername());
    }

    /**
     * Updates the profile data for the given player name, or returns the cached data if it's still valid.
     * The data is cached for 5 minutes.
     * @param name The player name to get the profile data for
     * @return A CompletableFuture that will be completed with either the profile's json data or null if the data couldn't be fetched
     */
    public static @NotNull CompletableFuture<@Nullable JsonObject> updateProfile(String name) {
        List<Profile> cachedProfiles = players.get(name);
        if (cachedProfiles != null) {
            Profile profile = getSelectedProfile(cachedProfiles);
            if (profile != null && profile.lastUpdate + HYPIXEL_API_COOLDOWN > System.currentTimeMillis()) return CompletableFuture.completedFuture(profile.jsonData);
        }

        return CompletableFuture.supplyAsync(() -> {
            String uuid = ApiUtils.name2Uuid(name);
            try (Http.ApiResponse response = Http.sendHypixelRequest("skyblock/profiles", "?uuid=" + uuid)) {
                if (!response.ok()) {
                    throw new IllegalStateException("Failed to get profile uuid for players " + name + "! Response: " + response.content());
                }
                JsonObject responseJson = SkyblockerMod.GSON.fromJson(response.content(), JsonObject.class);

                List<Profile> profiles = new ArrayList<>();
                for (JsonObject profile : responseJson.getAsJsonArray("profiles").asList().stream().map(JsonElement::getAsJsonObject).toList()) {
                    JsonPrimitive profileUuid = profile.getAsJsonPrimitive("profile_id");
                    boolean selected = profile.getAsJsonPrimitive("selected").getAsBoolean();
                    JsonObject profileJson = profile.getAsJsonObject("members").getAsJsonObject(uuid);
                    long lastUpdate = System.currentTimeMillis();
                    LOGGER.info("[Skyblocker Profile Utils] Updated Player Profile Data for player {} with profile {}, selected: {}", name, profileUuid.getAsString(), selected);
                    profiles.add(new Profile(profileUuid.getAsString(), profileJson, selected, lastUpdate));
                }
                players.put(name, profiles);

                return getSelectedProfile(profiles).jsonData;
            } catch (Exception e) {
                LOGGER.error("[Skyblocker Profile Utils] Failed to get Player Profile Data for players {}, is the API Down/Limited?", name, e);
            }
            return null;
        });
    }

    /**
     * Convenience method to get the selected profile from a list of profiles.
     * @param profiles The list of profiles to search in
     * @return The selected profile or null if none is selected (technically, this should never happen.)
     */
    private static @Nullable Profile getSelectedProfile(List<Profile> profiles) {
        for (Profile profile : profiles) {
            if (profile.selected) return profile;
        }
        return null; //This should never happen
    }

    public static class Profile {
        String uuid;
        JsonObject jsonData;
        boolean selected;
        long lastUpdate;

        public Profile(String uuid, JsonObject jsonData, boolean selected, long lastUpdate) {
            this.uuid = uuid;
            this.jsonData = jsonData;
            this.selected = selected;
            this.lastUpdate = lastUpdate;
        }
    }
}
