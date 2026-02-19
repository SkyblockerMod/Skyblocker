package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Object mapping for the success response of {@code /v2/skyblock/profiles}.
 */
public class ApiProfileResponse {
	public List<ApiProfile> profiles = List.of();

	public @Nullable ApiProfile getSelectedProfile() {
		return this.profiles.stream()
				.filter(profile -> profile.selected)
				.findFirst()
				.orElse(null);
	}
}
