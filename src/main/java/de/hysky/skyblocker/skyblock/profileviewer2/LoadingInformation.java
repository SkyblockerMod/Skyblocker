package de.hysky.skyblocker.skyblock.profileviewer2;

import java.util.concurrent.CompletableFuture;

import com.mojang.authlib.GameProfile;

import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.ProfileItemStorage;

public record LoadingInformation(ApiProfile profile, GameProfile mainMember, ProfileMember member, CompletableFuture<ProfileItemStorage> itemStorage) {
	// TODO shared state system for loading items (since skills page nw will need them as well as the inv page)
}
