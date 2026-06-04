package de.hysky.skyblocker.config.configs;

import java.awt.Color;
import net.minecraft.world.item.DyeColor;

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
		public Color hideonleafGlowColor = new Color(DyeColor.YELLOW.getTextColor(), false);
		public boolean highlightShellwise = true;
		public Color shellwiseGlowColor = new Color(DyeColor.ORANGE.getTextColor(), false);
		public boolean highlightCoralot = true;
		public Color coralotGlowColor = new Color(DyeColor.BLUE.getTextColor(), false);
	}

	public static class LassoHud {
		public boolean enabled = true;
	}
}
