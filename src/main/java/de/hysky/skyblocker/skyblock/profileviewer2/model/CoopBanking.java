package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CoopBanking {
	/**
	 * @see ProfileMemberProfile#personalBankAccount
	 */
	public double balance;
	public List<Transaction> transactions = List.of();

	public static class Transaction {
		public double amount;
		public long timestamp;
		public BankAction action;
		@SerializedName("initiator_name")
		public String initiatorName;
	}

	public enum BankAction {
		DEPOSIT,
		WITHDRAW;
	}
}
