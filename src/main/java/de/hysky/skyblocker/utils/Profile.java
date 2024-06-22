package de.hysky.skyblocker.utils;

import com.google.gson.JsonObject;

/**
 * Represents a skyblock profile.
 */
public class Profile {
	public final String uuid;
	public final JsonObject jsonData;
	private boolean selected;
	//This is used for the API call cooldown, but the value is not updated as creating new Profile objects is easier than getting those from the cache and updating them
	public final long lastUpdate;

	public Profile(String uuid, JsonObject jsonData, boolean selected, long lastUpdate) {
		this.uuid = uuid;
		this.jsonData = jsonData;
		this.selected = selected;
		this.lastUpdate = lastUpdate;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}
}
