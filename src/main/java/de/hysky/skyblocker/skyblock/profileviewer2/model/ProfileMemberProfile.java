package de.hysky.skyblocker.skyblock.profileviewer2.model;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

public class ProfileMemberProfile {
	@SerializedName("first_join")
	public long firstJoin;
	public int personal_bank_upgrade;
	public boolean cookie_buff_active;
	@SerializedName("coop_invitation")
	public @Nullable CoopInvitation coopInvitation;
	@SerializedName("deletion_notice")
	public @Nullable DeletionNotice deletionNotice;
	/**
	 * If this is not present, the person is not in a coop, and only has the shared coop bank, instead of a personal one.
	 * @see ApiProfile#banking
	 */
	@SerializedName("bank_account")
	public @Nullable Double personalBankAccount;

	public static class CoopInvitation {
		public long timestamp;
		@SerializedName("invited_by")
		public UUID invitedBy = UUID.randomUUID();
		public boolean confirmed;
		@SerializedName("confirmed_timestamp")
		public long confirmedTimestamp;
	}

	public static class DeletionNotice {
		public long timestamp;
	}
}
