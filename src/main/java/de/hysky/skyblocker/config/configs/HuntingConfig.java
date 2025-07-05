package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class HuntingConfig {
	@SerialEntry
	public HuntingBox huntingBox = new HuntingBox();

	@SerialEntry
	public HuntingMobs huntingMobs = new HuntingMobs();

	@SerialEntry
	public ShardFusion shardFusion = new ShardFusion();

	public static class HuntingBox {
		@SerialEntry
		public boolean enabled = true;
	}

	public static class HuntingMobs {
		@SerialEntry
		public boolean silencePhantoms = true;

		@SerialEntry
		public boolean highlightHideonleaf = true;
	}

	public static class ShardFusion {
		@SerialEntry
		public boolean enableQuickShardFusion = true;
	}
}
