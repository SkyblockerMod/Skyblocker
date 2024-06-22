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
	//Convenience variable to store the selected profile for the current player to not have to do `list.get()` twice every time
	private static Profile selectedProfile = null;
	private static boolean profileWasChanged = false;

	private ProfileUtils() {}

	public static void init() {
		ClientReceiveMessageEvents.GAME.register(ProfileUtils::onMessage);
	}

	private static void onMessage(Text message, boolean overlay) {
		if (overlay) return;
		String str = message.getString();

		if (str.startsWith("§aYour profile was changed to:")) {
			Profile profile = getSelectedProfile(players.get(getUsername()));
			if (profile != null) {
				profile.setSelected(false);
				selectedProfile = null;
			}
			profileWasChanged = true;
		} else if (str.startsWith("Profile ID: ")) {
			//This branch is also fired when world is changed, with those "you're playing on profile ..." messages
			//It's intentionally not ignored as the profile change might've been cached on the API and gave us a wrong profile as the selected one
			String profileId = str.substring(12);
			List<Profile> profiles = players.get(getUsername());
			if (profiles == null) return;

			selectedProfile = profiles.stream()
			                          .filter(p -> p.uuid.equals(profileId))
			                          .findFirst()
			                          .orElseGet(() -> {
				                          // If no profile was found with the given id, that means it's a new profile.
				                          // Since it won't have any data yet, we can just add it with an empty json object and save ourselves an API call.
				                          Profile newProfile = new Profile(profileId, new JsonObject(), true, System.currentTimeMillis());
				                          profiles.add(newProfile);
				                          return newProfile;
			                          });

			if (profileWasChanged) {
				SkyblockEvents.PROFILE_CHANGE.invoker().onSkyblockProfileChange(selectedProfile);
				profileWasChanged = false;
			}
		}
	}

	/**
	 * Convenience method to get the current player's username.
	 */
	public static String getUsername() {
		return MinecraftClient.getInstance().getSession().getUsername();
	}

	/**
	 * Updates the profile data for the current player, or returns the cached data if it's still valid.
	 *
	 * @see #updateProfile(String)
	 */
	public static CompletableFuture<JsonObject> updateProfile() {
		return updateProfile(getUsername());
	}

	/**
	 * Updates the profile data for the given player name, or returns the cached data if it's still valid.
	 * The data is cached for 5 minutes.
	 *
	 * @param name The player name to get the profile data for
	 * @return A CompletableFuture that will be completed with either the profile's json data or null if the data couldn't be fetched
	 */
	public static @NotNull CompletableFuture<@Nullable JsonObject> updateProfile(String name) {
		if (selectedProfile != null && selectedProfile.lastUpdate + HYPIXEL_API_COOLDOWN > System.currentTimeMillis()) {
			return CompletableFuture.completedFuture(selectedProfile.jsonData);
		}

		return CompletableFuture.supplyAsync(() -> {
			String uuid = ApiUtils.name2Uuid(name);
			try (Http.ApiResponse response = Http.sendHypixelRequest("skyblock/profiles", "?uuid=" + uuid)) {
				if (!response.ok()) {
					throw new IllegalStateException("Failed to get profile uuid for players " + name + "! Response: " + response.content());
				}
				JsonObject responseJson = SkyblockerMod.GSON.fromJson(response.content(), JsonObject.class);

				List<Profile> profiles = new ArrayList<>();
				Profile selectedProfile = null;
				for (JsonObject profile : responseJson.getAsJsonArray("profiles").asList().stream().map(JsonElement::getAsJsonObject).toList()) {
					JsonPrimitive profileUuid = profile.getAsJsonPrimitive("profile_id");
					boolean selected = profile.getAsJsonPrimitive("selected").getAsBoolean();
					JsonObject profileJson = profile.getAsJsonObject("members").getAsJsonObject(uuid);
					long lastUpdate = System.currentTimeMillis();
					Profile tempProfile = new Profile(profileUuid.getAsString(), profileJson, selected, lastUpdate);
					if (selected) selectedProfile = tempProfile;
					profiles.add(tempProfile);
				}
				players.put(name, profiles); //Overwrites the old value of the key, if it exists

				assert selectedProfile != null;
				return selectedProfile.jsonData;
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Profile Utils] Failed to get Player Profile Data for players {}, is the API Down/Limited?", name, e);
			}
			return null;
		});
	}

	/**
	 * Convenience method to get the selected profile from a list of profiles.
	 *
	 * @param profiles The list of profiles to search in
	 * @return The selected profile or null if none is selected (technically, this should never happen.)
	 */
	private static @Nullable Profile getSelectedProfile(List<Profile> profiles) {
		for (Profile profile : profiles) {
			if (profile.isSelected()) return profile;
		}
		return null; //This should never happen
	}

	/**
	 * @return The selected profile for the current player, or null if the API call for the profiles hasn't been made yet.
	 */
	public static @Nullable Profile getSelectedProfile() {
		return selectedProfile;
	}
}
