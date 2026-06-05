package de.hysky.skyblocker.skyblock.profileviewer2;

import com.mojang.authlib.GameProfile;

import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;

public record LoadingInformation(ApiProfile profile, GameProfile mainMember, ProfileMember member) {
	// TODO shared state system for loading items (since skills page nw will need them as well as the inv page)
}
