package de.hysky.skyblocker.config.configs;

import java.awt.Color;

public class HuntingConfig {
	public HuntingBox huntingBox = new HuntingBox();

	public HuntingMobs huntingMobs = new HuntingMobs();

	public LassoHud lassoHud = new LassoHud();

	public static class HuntingBox {
		public boolean enabled = true;
	}

	public static class HuntingMobs {
		public boolean silencePhantoms = true;

		public boolean highlightHideonleaf = true;
		public Color hideonleafGlowColor = Color.decode("#2ECC40");
		public boolean highlightShellwise = true;
		public Color shellwiseGlowColor = Color.decode("#3D9970");
		public boolean highlightCoralot = true;
		public Color coralotGlowColor = Color.decode("#F012BE");
	}

	public static class LassoHud {
		public boolean enabled = true;
	}
}
