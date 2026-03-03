package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Forge {
	@SerializedName("forge_processes")
	public ForgeProcesses forgeProcesses = new ForgeProcesses();

	public static class ForgeProcesses {
		public Map<String, Node> forge_1 = Map.of();

		public static class Node {
			public String type = "";
			public String id = "";
			public long startTime;
			public int slot;
			public boolean notified = false;
		}
	}
}
