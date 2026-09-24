package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

public class Rift {
	public Access access = new Access();

	public static class Access {
		@SerializedName("consumed_prism")
		public boolean consumedPrism;
	}
}
