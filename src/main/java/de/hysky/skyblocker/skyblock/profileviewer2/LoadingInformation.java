package de.hysky.skyblocker.skyblock.profileviewer2;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.authlib.GameProfile;

import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.EliteLeaderboards;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.ProfileItemStorage;

public record LoadingInformation(ApiProfile profile, GameProfile mainMember, ProfileMember member, Map<String, Integer> leaderboards, CompletableFuture<ProfileItemStorage> itemStorage) {
	/// {@return the leaderboard position or {@link EliteLeaderboards#NO_POSITION}}
	public int getLeaderboardPosition(String leaderboard) {
		return this.leaderboards().getOrDefault(leaderboard, EliteLeaderboards.NO_POSITION);
	}
}
