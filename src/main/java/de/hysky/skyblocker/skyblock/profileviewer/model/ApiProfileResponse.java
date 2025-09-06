package de.hysky.skyblocker.skyblock.profileviewer.model;

import java.util.List;

/**
 * Object mapping for the success response of {@code /v2/skyblock/profiles}.
 */
public class ApiProfileResponse {
	public List<ApiProfile> profiles = List.of();
}
