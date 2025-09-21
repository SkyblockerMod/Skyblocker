package de.hysky.skyblocker.skyblock.profileviewer.rework;

import de.hysky.skyblocker.skyblock.profileviewer.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer.model.ProfileMember;

import java.util.UUID;

public sealed interface ProfileLoadState {
	record SuccessfulLoad(
			ApiProfile profile,
			UUID mainMemberId,
			ProfileMember member
	) implements ProfileLoadState {}

	record Error(
			String message
	) implements ProfileLoadState {}

	record Loading() implements ProfileLoadState {}
}
