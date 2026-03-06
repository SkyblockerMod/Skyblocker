package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import org.apache.commons.lang3.ArrayUtils;

import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;

public class LevelCalculator {
	private static final int[] REGULAR_XP_CHART = { 50, 125, 200, 300, 500, 750, 1000, 1500, 2000, 3500, 5000, 7500, 10000, 15000, 20000,
			30000, 50000, 75000, 100000, 200000, 300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000,
			1100000, 1200000, 1300000, 1400000, 1500000, 1600000, 1700000, 1800000, 1900000, 2000000, 2100000,
			2200000, 2300000, 2400000, 2500000, 2600000, 2750000, 2900000, 3100000, 3400000, 3700000, 4000000,
			4300000, 4600000, 4900000, 5200000, 5500000, 5800000, 6100000, 6400000, 6700000, 7000000 };
	private static final int[] RUNECRAFTING_XP_CHART = { 50, 100, 125, 160, 200, 250, 315, 400, 500, 625, 785, 1000, 1250, 1600, 2000,
			2465, 3125, 4000, 5000, 6200, 7800, 9800, 12200, 15300, 19050 };
	private static final int[] SOCIAL_XP_CHART = { 50, 100, 150, 250, 500, 750, 1000, 1250, 1500, 2000, 2500, 3000, 3750, 4500, 6000, 8000,
			10000, 12500, 15000, 20000, 25000, 30000, 35000, 40000, 50000 };
	private static final int[] DUNGEONS_XP_CHART = { 50, 75, 110, 160, 230, 330, 470, 670, 950, 1340, 1890, 2665, 3760, 5260, 7380, 10300, 14400,
			20000, 27600, 38000, 52500, 71500, 97000, 132000, 180000, 243000, 328000, 445000, 600000, 800000,
			1065000, 1410000, 1900000, 2500000, 3300000, 4300000, 5600000, 7200000, 9200000, 12000000, 15000000,
			19000000, 24000000, 30000000, 38000000, 48000000, 60000000, 75000000, 93000000, 116250000 };
	private static final int DUNGEONS_OVERFLOW_THRESHOLD = 200_000_000;

	public static LevelInfo getSkillLevel(long xp, Skill skill) {
		int[] xpChart = switch (skill) {
			case Skill.CATACOMBS -> DUNGEONS_XP_CHART;
			case Skill.RUNECRAFTING -> RUNECRAFTING_XP_CHART;
			case Skill.SOCIAL -> SOCIAL_XP_CHART;

			default -> REGULAR_XP_CHART;
		};
		int levelCap = skill.baseCap() + getSkillCapIncrease(skill, null);

		long xpTotal = 0;
		int level = 1;

		for (int i = 0; i < xpChart.length; i++) {
			xpTotal += xpChart[i];
			level = i + 1;

			if (xp < xpTotal) {
				level = i;
				break;
			}
		}

		// For Catacombs we want to apply the overflow levels and return the level without it being capped to 50
		if (skill == Skill.CATACOMBS) {
			long xpLeft = xp - xpTotal;

			while (xpLeft >= DUNGEONS_OVERFLOW_THRESHOLD) {
				level++;
				xpLeft -= DUNGEONS_OVERFLOW_THRESHOLD;
			}

			return new LevelInfo(xp, level);
		} else {
			return new LevelInfo(xp, Math.min(level, levelCap));
		}
	}

	private static int getSkillCapIncrease(Skill skill, ProfileMember member) {
		return switch (skill) {
			case Skill.FARMING -> member.jacobsContest.perks.farmingLevelCap;
			case Skill.TAMING -> member.petsData.petCare.petTypesSacrificed.size();
			default -> 0;
		};
	}

	public static LevelInfo getSlayerLevel(long xp, SlayerType slayer) {
		// Note: Slayer XP is stored cumulatively
		int[] xpChart = slayer.levelMilestones.clone();
		// Reverse the array so its easier to calculate the level, the original array is cloned to avoid mutation
		ArrayUtils.reverse(xpChart);

		for (int i = 0; i < xpChart.length; i++) {
			if (xp >= xpChart[i]) {
				return new LevelInfo(xp, i);
			}
		}

		return new LevelInfo(xp, 0);
	}

	public static int getSkyblockLevel(int xp) {
		return xp / 100;
	}
}
