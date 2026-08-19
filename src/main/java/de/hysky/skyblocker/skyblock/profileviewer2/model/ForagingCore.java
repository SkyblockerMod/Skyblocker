package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

public class ForagingCore {
	public Whispers whispers = new Whispers();

	public static class Whispers {
		public AbstractWhispersData forest = new AbstractWhispersData();
		public AbstractWhispersData desert = new AbstractWhispersData();

		public static class AbstractWhispersData {
			public long total;
			@SerializedName("1")
			public WhispersSlot slot1 = new WhispersSlot();
			@SerializedName("2")
			public WhispersSlot slot2 = new WhispersSlot();
			@SerializedName("3")
			public WhispersSlot slot3 = new WhispersSlot();
			@SerializedName("4")
			public WhispersSlot slot4 = new WhispersSlot();
			@SerializedName("5")
			public WhispersSlot slot5 = new WhispersSlot();

			public static class WhispersSlot {
				public long spent;
				@SerializedName("spent_non_refundable")
				public long spentNonRefundable;
			}
		}
	}
}
