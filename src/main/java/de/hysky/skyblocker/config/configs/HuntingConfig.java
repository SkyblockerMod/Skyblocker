package de.hysky.skyblocker.config.configs;

public class HuntingConfig {
	public HuntingBox huntingBox = new HuntingBox();

	public HuntingMobs huntingMobs = new HuntingMobs();

	public static class HuntingBox {
		public boolean enabled = true;
	}

	public static class HuntingMobs {
		public boolean silencePhantoms = true;

		public boolean highlightHideonleaf = true;
	}
}
