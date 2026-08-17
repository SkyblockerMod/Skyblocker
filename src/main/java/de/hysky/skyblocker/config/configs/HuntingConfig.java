package de.hysky.skyblocker.config.configs;

import java.awt.Color;

import net.minecraft.world.item.DyeColor;

import de.hysky.skyblocker.utils.SkyBlockColors;

public class HuntingConfig {
	public HuntingBox huntingBox = new HuntingBox();

	public MoongladeMobs moongladeMobs = new MoongladeMobs();

	public TorrhusMobs torrhusMobs = new TorrhusMobs();

	public Safari safari = new Safari();

	public CavernBiome cavernBiome = new CavernBiome();

	public ForestBiome forestBiome = new ForestBiome();

	public HauntedBiome hauntedBiome = new HauntedBiome();

	public IcyBiome icyBiome = new IcyBiome();

	public FloorDrops floorDrops = new FloorDrops();

	public LassoHud lassoHud = new LassoHud();

	public static class HuntingBox {
		public boolean enabled = true;
	}

	public static class MoongladeMobs {
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

		public boolean highlightBlueJay = true;

		public Color blueJayHighlightColor = new Color(DyeColor.GREEN.getTextColor(), false);
	}

	public static class Safari {
		public boolean silencePhantoms = true;

		public boolean highlightSparklingCritters = false;

		public Color sparklingCritterHighlightColor = new Color(SkyBlockColors.GOLD.getValue(), false);
	}

	public static class CavernBiome {
		public boolean highlightRockmiteMounds = true;

		public Color rockmiteMoundHighlightColor = new Color(DyeColor.LIGHT_BLUE.getTextColor(), false);
	}

	public static class ForestBiome {
		public boolean highlightHideonfloor = true;

		public Color hideonfloorHighlightColor = new Color(DyeColor.MAGENTA.getTextColor(), false);
	}

	public static class HauntedBiome {
		public boolean highlightDuplico = true;

		public Color duplicoHighlightColor = new Color(DyeColor.RED.getTextColor(), false);

		public boolean highlightBloodbat = true;

		public Color bloodbatHighlightColor = new Color(DyeColor.LIME.getTextColor(), false);

		public boolean ignoreSlotLockingForShiningCoins = false;
	}

	public static class IcyBiome {
		public boolean hideColdOverlay = false;
	}

	public static class FloorDrops {
		public boolean highlightFloorDrops = true;

		public Color floorDropHighlightColor = new Color(DyeColor.ORANGE.getTextureDiffuseColor(), false);
	}

	public static class LassoHud {
		public boolean enabled = true;
	}
}
