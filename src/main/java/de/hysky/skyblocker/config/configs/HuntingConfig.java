package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class HuntingConfig {
	@SerialEntry
	public HuntingBox huntingBox = new HuntingBox();

	public static class HuntingBox {
		@SerialEntry
		public boolean enabled = true;
	}
}
