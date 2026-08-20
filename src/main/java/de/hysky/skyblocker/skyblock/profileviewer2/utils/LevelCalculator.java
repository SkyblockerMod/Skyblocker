package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import java.util.List;
import java.util.Locale;

import io.github.moulberry.repo.constants.Leveling;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.NEURepoManager;

public class LevelCalculator {
	private static final int SKYBLOCK_LEVEL_INTERVAL = 100;

	public static LevelInfo getSkillLevel(long xp, Skill skill, LoadingInformation info) {
		Leveling levelling = NEURepoManager.getConstants().getLeveling();
		boolean hasLevellingConstants = !NEURepoManager.isLoading() && levelling != null;

		List<Integer> xpChart = hasLevellingConstants ? switch (skill) {
			case CATACOMBS -> levelling.getCatacombsExperienceRequiredPerLevel();
			case RUNECRAFTING -> levelling.getRunecraftingExperienceRequiredPerLevel();
			case SOCIAL -> levelling.getSocialExperienceRequiredPerLevel();

			default -> levelling.getSkillExperienceRequiredPerLevel();
		} : List.of();
		int levelCapIncrease = getSkillCapIncrease(skill, info);
		int levelCap = skill.getBaseCap() + levelCapIncrease;

		int level = 0;
		long remainingXp = xp;

		for (int xpRequired : xpChart) {
			if (remainingXp >= xpRequired) {
				level++;
				remainingXp -= xpRequired;
			} else {
				break;
			}
		}

		// Don't do the cap for cata since it can go over & setting the cap in the Skill enum causes bars to look weird
		boolean isCatacombs = skill == Skill.CATACOMBS;
		int cappedLevel = isCatacombs ? level : Math.min(levelCap, level);
		LevelInfo.Progress progress = null;

		// Ensure cata has level progress information beyond level 50
		if (cappedLevel < levelCap || (isCatacombs && xpChart.size() > cappedLevel)) {
			long xpForNextLevel = xpChart.get(cappedLevel);

			progress = new LevelInfo.Progress(remainingXp, xpForNextLevel);
		}

		return new LevelInfo(xp, cappedLevel, new LevelInfo.Cap(levelCap, skill.getAbsoluteCap()), progress);
	}

	private static int getSkillCapIncrease(Skill skill, LoadingInformation info) {
		ApiProfile profile = info.profile();
		ProfileMember currentMember = info.member();

		return switch (skill) {
			case FARMING -> currentMember.jacobsContest.perks.farmingLevelCap;
			case FORAGING -> {
				int increase = 0;

				if (CollectionTiers.unlockedTier(profile, "FIG_LOG", 9)) {
					increase++;
				}

				if (CollectionTiers.unlockedTier(profile, "MANGROVE_LOG", 9)) {
					increase++;
				}

				if (CollectionTiers.unlockedTier(profile, "HELIX_LOG", 9)) {
					increase++;
				}

				yield increase;
			}
			case TAMING -> currentMember.petsData.petCare.petTypesSacrificed.size();
			default -> 0;
		};
	}

	public static LevelInfo getSlayerLevel(long xp, SlayerType slayer) {
		Leveling levelling = NEURepoManager.getConstants().getLeveling();
		boolean hasLevellingConstants = !NEURepoManager.isLoading() && levelling != null;

		String neuSlayerId = slayer.friendlyName.toLowerCase(Locale.ENGLISH);
		List<Integer> xpChart = hasLevellingConstants ? levelling.getSlayerExperienceRequiredPerLevel().getOrDefault(neuSlayerId, List.of()) : List.of();

		int level = 0;
		long remainingXp = xp;

		for (int xpRequired : xpChart) {
			if (remainingXp >= xpRequired) {
				level++;
				remainingXp -= xpRequired;
			} else {
				break;
			}
		}

		int cappedLevel = Math.min(slayer.maxLevel, level);
		LevelInfo.Progress progress = null;

		if (cappedLevel < slayer.maxLevel) {
			long xpForNextLevel = xpChart.get(cappedLevel);

			progress = new LevelInfo.Progress(remainingXp, xpForNextLevel);
		}

		return new LevelInfo(xp, cappedLevel, new LevelInfo.Cap(slayer.maxLevel, slayer.maxLevel), progress);
	}

	public static LevelInfo getSkyblockLevel(int xp) {
		int level = (int) (xp / SKYBLOCK_LEVEL_INTERVAL);

		LevelInfo.Cap cap = new LevelInfo.Cap(Integer.MAX_VALUE, Integer.MAX_VALUE);
		LevelInfo.Progress progress = new LevelInfo.Progress(xp - (level * SKYBLOCK_LEVEL_INTERVAL), SKYBLOCK_LEVEL_INTERVAL);
		LevelInfo levelInfo = new LevelInfo(xp, level, cap, progress);

		return levelInfo;
	}
}
