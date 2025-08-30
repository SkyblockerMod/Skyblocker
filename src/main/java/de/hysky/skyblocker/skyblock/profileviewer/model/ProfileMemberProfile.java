package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public class ProfileMemberProfile {
	@SerializedName("first_join")
	public long firstJoin;
	public int personal_bank_upgrade;
	public boolean cookie_buff_active;
	// TODO: deletion_notice, coop_invitation
	/**
	 * If this is not present, the person is not in a coop, and only has the shared coop bank, instead of a personal one.
	 * @see ApiProfile#banking
	 */
	@SerializedName("bank_account")
	public @Nullable Double personalBankAccount;
}
