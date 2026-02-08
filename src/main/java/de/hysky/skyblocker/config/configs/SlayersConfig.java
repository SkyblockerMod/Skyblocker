package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import net.minecraft.client.resources.language.I18n;

import java.awt.Color;

public class SlayersConfig {
	public HighlightSlayerEntities highlightMinis = HighlightSlayerEntities.GLOW;

	public HighlightSlayerEntities highlightBosses = HighlightSlayerEntities.GLOW;

	public Color highlightColor = new Color(0xFFFF4800, true);

	public boolean displayBossbar = true;

	public enum HighlightSlayerEntities {
		OFF, GLOW, HITBOX;

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.slayer.highlightBosses." + name());
		}
	}

	public boolean bossSpawnAlert = true;

	public boolean miniBossSpawnAlert = true;

	public boolean alertOtherMinibosses = false;

	public boolean showMiniBossNameInAlert = false;

	public boolean slainTime = true;

	public boolean enableHud = true;

	public ChatFilterResult hideSlayerMinibossSpawn = ChatFilterResult.PASS;

	public boolean highlightUnclaimedRewards = true;

	public CallMaddox callMaddox = new CallMaddox();

	public EndermanSlayer endermanSlayer = new EndermanSlayer();

	public VampireSlayer vampireSlayer = new VampireSlayer();

	public BlazeSlayer blazeSlayer = new BlazeSlayer();

	public WolfSlayer wolfSlayer = new WolfSlayer();

	public SpiderSlayer spiderSlayer = new SpiderSlayer();

	public static class CallMaddox {
		public boolean sendMessageOnFail = true;

		public boolean sendMessageOnKill = false;
	}

	public static class SpiderSlayer {
		public boolean muteSpiderSounds = false;
	}

	public static class WolfSlayer {
		public boolean muteWolfSounds = false;

		public boolean hideSvenPupNametag = true;
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

		public boolean attunementHighlights = true;

		public boolean muteBlazeSounds = true;

		public ChatFilterResult hideHellionShield = ChatFilterResult.PASS;

		public enum FirePillar {
			OFF,
			VISUAL,
			SOUND_AND_VISUAL;

			@Override
			public String toString() {
				return I18n.get("skyblocker.config.slayer.blazeSlayer.enableFirePillarAnnouncer.mode." + name());
			}
		}
	}

}
