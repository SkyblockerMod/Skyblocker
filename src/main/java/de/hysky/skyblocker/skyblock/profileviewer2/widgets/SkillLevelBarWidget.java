package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelInfo;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.Skill;

public final class SkillLevelBarWidget extends LevelBarWidget {
	private static final int NORMAL_LEVEL_DIVISOR = 10;
	private static final int COSMETIC_LEVEL_DIVISOR = 5;

	private SkillLevelBarWidget(int width, ItemStack icon, Component label, double barFillPercentage, int barFillColour, List<Component> tooltip, @Nullable Identifier tooltipStyle) {
		super(width, icon, label, barFillPercentage, barFillColour, tooltip, tooltipStyle);
	}

	public static LevelBarWidget create(int width, Skill skill, LoadingInformation info) {
		LevelInfo levelInfo = info.member().playerData.getSkillLevel(skill, info);
		String name = skill.getFriendlyName();
		String leaderboardId = name.toLowerCase(Locale.ENGLISH);
		boolean isCosmetic = skill == Skill.RUNECRAFTING || skill == Skill.SOCIAL;

		Component label = Component.literal(name + " " + levelInfo.level());
		double barFillPercentage = getBarFillPercentage(levelInfo);
		int barFillColour = getBarFillColour(levelInfo);
		List<Component> tooltip = buildTooltip(label, levelInfo, info.getLeaderboardPosition(leaderboardId));
		Identifier tooltipStyle = getTooltipStyle(levelInfo, isCosmetic ? COSMETIC_LEVEL_DIVISOR : NORMAL_LEVEL_DIVISOR);

		return new SkillLevelBarWidget(width, skill.getIcon().getStackOrThrow(), label, barFillPercentage, barFillColour, tooltip, tooltipStyle);
	}
}
