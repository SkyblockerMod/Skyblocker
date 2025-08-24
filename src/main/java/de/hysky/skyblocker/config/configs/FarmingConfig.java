package de.hysky.skyblocker.config.configs;

import net.minecraft.client.resource.language.I18n;

public class FarmingConfig {
	public Garden garden = new Garden();

	public VisitorHelper visitorHelper = new VisitorHelper();

	public static class Garden {
		public FarmingHud farmingHud = new FarmingHud();

		public boolean dicerTitlePrevent = true;

		public boolean pestHighlighter = true;

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
			return I18n.translate("skyblocker.config.farming.garden.farmingHud.type." + name());
		}
	}
}
