package de.hysky.skyblocker.config.configs;

import net.minecraft.client.resources.language.I18n;

public class FarmingConfig {
	public Garden garden = new Garden();

	public VisitorHelper visitorHelper = new VisitorHelper();

	public static class Garden {
		public FarmingHud farmingHud = new FarmingHud();

		@Deprecated
		public transient boolean dicerTitlePrevent;

		public boolean pestHighlighter = true;

		public boolean vinylHighlighter = true;

		public boolean lockMouseTool = false;

		public boolean lockMouseGroundOnly = false;

		public boolean gardenPlotsWidget = true;

		public boolean closeScreenOnPlotClick = false;

		public boolean enableStereoHarmonyHelperForContest = true;
	}

	public static class VisitorHelper {
		public boolean visitorHelper = true;

		public boolean visitorHelperGardenOnly = true;

		public boolean showStacksInVisitorHelper = false;
	}

	public static class FarmingHud {
		public boolean enableHud = true;

		public int x;

		public int y;

		public Type type = Type.BOTH;
	}

	public enum Type {
		BOTH,
		NPC,
		BAZAAR;

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.farming.farmingHud.type." + name());
		}
	}
}
