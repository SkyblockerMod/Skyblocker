package de.hysky.skyblocker.config.configs;

import net.minecraft.client.resources.language.I18n;

public class FarmingConfig {
	public FarmingHud farmingHud = new FarmingHud();

	public PestHighlighter pestHighlighter = new PestHighlighter();

	public MouseLock mouseLock = new MouseLock();

	public PlotsWidget plotsWidget = new PlotsWidget();

	public VisitorHelper visitorHelper = new VisitorHelper();

	public static class PestHighlighter {
		public boolean enabled = true;

		public boolean vinylHighlighter = true;

		public boolean enableStereoHarmonyHelperForContest = true;
	}

	public static class MouseLock {
		public boolean lockMouseTool = false;

		public boolean lockMouseGroundOnly = false;
	}

	public static class PlotsWidget {
		public boolean enabled = true;

		public boolean closeScreenOnPlotClick = false;
	}

	public static class VisitorHelper {
		public boolean enabled = true;

		public boolean showInGardenOnly = true;

		public boolean showInStacks = false;
	}

	public static class FarmingHud {
		public boolean enabled = true;

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
