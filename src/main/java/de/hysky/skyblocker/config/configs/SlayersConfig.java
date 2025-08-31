package de.hysky.skyblocker.config.configs;

import net.minecraft.client.resource.language.I18n;

public class SlayersConfig {
	public HighlightSlayerEntities highlightMinis = HighlightSlayerEntities.GLOW;

	public HighlightSlayerEntities highlightBosses = HighlightSlayerEntities.GLOW;

	public boolean displayBossbar = true;

	public enum HighlightSlayerEntities {
		OFF, GLOW, HITBOX;

		@Override
		public String toString() {
			return I18n.translate("skyblocker.config.slayer.highlightBosses." + name());
		}
	}

	public boolean bossSpawnAlert = true;

	public boolean miniBossSpawnAlert = true;

	public boolean slainTime = true;

	public boolean enableHud = true;

	public CallMaddox callMaddox = new CallMaddox();

	public EndermanSlayer endermanSlayer = new EndermanSlayer();

	public VampireSlayer vampireSlayer = new VampireSlayer();

	public BlazeSlayer blazeSlayer = new BlazeSlayer();

	public static class CallMaddox {
		public boolean sendMessageOnFail = true;

		public boolean sendMessageOnKill = false;
	}

	public static class EndermanSlayer {
		public boolean enableYangGlyphsNotification = true;

		public boolean highlightBeacons = true;

		public boolean highlightNukekubiHeads = true;

		public boolean lazerTimer = true;
	}

	public static class VampireSlayer {
		public boolean enableEffigyWaypoints = true;

		public boolean compactEffigyWaypoints = false;

		public int effigyUpdateFrequency = 5;

		public boolean enableHolyIceIndicator = true;

		public int holyIceIndicatorTickDelay = 5;

		public int holyIceUpdateFrequency = 5;

		public boolean enableHealingMelonIndicator = true;

		public float healingMelonHealthThreshold = 4f;

		public boolean enableSteakStakeIndicator = true;

		public int steakStakeUpdateFrequency = 5;

		public boolean enableManiaIndicator = true;

		public int maniaUpdateFrequency = 5;
	}

	public static class BlazeSlayer {
		public FirePillar firePillarCountdown = FirePillar.SOUND_AND_VISUAL;
		public Boolean attunementHighlights = true;

		public enum FirePillar {
			OFF,
			VISUAL,
			SOUND_AND_VISUAL;

			@Override
			public String toString() {
				return I18n.translate("skyblocker.config.slayer.blazeSlayer.enableFirePillarAnnouncer.mode." + name());
			}
		}
	}
}
