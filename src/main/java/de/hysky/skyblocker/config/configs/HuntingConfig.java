package de.hysky.skyblocker.config.configs;

import java.awt.Color;
import net.minecraft.world.item.DyeColor;

public class HuntingConfig {
	public HuntingBox huntingBox = new HuntingBox();

	public HuntingMobs huntingMobs = new HuntingMobs();

	public TorrhusMobs torrhusMobs = new TorrhusMobs();

	public Safari safari = new Safari();

	public HauntedBiome hauntedBiome = new HauntedBiome();

	public ForestBiome forestBiome = new ForestBiome();

	public CanyonBiome canyonBiome = new CanyonBiome();

	public IcyBiome icyBiome = new IcyBiome();

	public LassoHud lassoHud = new LassoHud();

	public static class HuntingBox {
		public boolean enabled = true;
	}

	// TODO rename to MoongladeMobs
	public static class HuntingMobs {
		public boolean silencePhantoms = true;

		public boolean highlightHideonleaf = true;

		public Color hideonleafGlowColor = new Color(DyeColor.YELLOW.getTextColor(), false);

		public boolean highlightShellwise = true;

		public Color shellwiseGlowColor = new Color(DyeColor.ORANGE.getTextColor(), false);

		public boolean highlightCoralot = true;

		public Color coralotGlowColor = new Color(DyeColor.BLUE.getTextColor(), false);
	}

	public static class TorrhusMobs {
		public boolean highlightHideonsun = true;

		public Color hideonsunHighlightColor = new Color(DyeColor.CYAN.getTextColor(), false);
	}

	public static class Safari {
		public boolean silencePhantoms = true;
	}

	public static class HauntedBiome {
		public boolean highlightDuplico = true;

		public Color duplicoHighlightColor = new Color(DyeColor.RED.getTextColor(), false);
	}

	public static class ForestBiome {
		public boolean highlightHideonfloor = true;

		public Color hideonfloorHighlightColor = new Color(DyeColor.MAGENTA.getTextColor(), false);
	}

	public static class CanyonBiome {

	}

	public static class IcyBiome {
		public boolean hideColdOverlay = false;
	}

	public static class LassoHud {
		public boolean enabled = true;
	}
}
