package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;

public class ProfileMemberProfile {
	@SerializedName("first_join")
	public long firstJoin;
	public int personal_bank_upgrade;
	public boolean cookie_buff_active;
	// TODO: deletion_notice, coop_invitation
	/**
	 * @see ApiProfile#banking
	 */
	@SerializedName("bank_account")
	public double personalBankAccount;
}
