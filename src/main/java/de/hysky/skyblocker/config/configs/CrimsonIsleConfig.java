package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.utils.waypoint.Waypoint;

public class CrimsonIsleConfig {
	public Kuudra kuudra = new Kuudra();

	public Dojo dojo = new Dojo();

	public boolean extendNetherFog = true;


	public static class Kuudra {
		public boolean supplyWaypoints = true;

		public boolean fuelWaypoints = true;

		public Waypoint.Type suppliesAndFuelWaypointType = Waypoint.Type.WAYPOINT;

		public boolean ballistaBuildWaypoints = true;

		public boolean safeSpotWaypoints = true;

		public boolean pearlWaypoints = true;

		public boolean noArrowPoisonWarning = true;

		public int arrowPoisonThreshold = 32;

		public boolean kuudraGlow = true;

		public boolean dangerWarning = true;
	}

	public static class Dojo {
		public boolean enableForceHelper = true;

		public boolean enableStaminaHelper = true;

		public boolean enableMasteryHelper = true;

		public boolean enableDisciplineHelper = true;

		public boolean enableSwiftnessHelper = true;

		public boolean enableControlHelper = true;

		public boolean enableTenacityHelper = true;
	}
}
