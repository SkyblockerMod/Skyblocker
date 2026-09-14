package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.model.Dungeons;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.EliteLeaderboards;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelCalculator;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelInfo;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.Skill;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.Formatters;

public final class DungeonLevelBarWidget extends LevelBarWidget {

	private DungeonLevelBarWidget(int width, ItemStack icon, Component label, double barFillPercentage, int barFillColour, List<Component> tooltip, @Nullable Identifier tooltipStyle) {
		super(width, icon, label, barFillPercentage, barFillColour, tooltip, tooltipStyle);
	}

	public static DungeonLevelBarWidget createCatacombs(int width, LoadingInformation info) {
		long catacombsXp = (long) info.member().dungeons.dungeonTypes.catacombs.experience;
		LevelInfo levelInfo = LevelCalculator.getSkillLevel(catacombsXp, Skill.CATACOMBS, info);

		Component label = Component.literal("Catacombs " + levelInfo.level());
		ItemStack icon = Ico.CATACOMBS.getStackOrThrow();
		double barFillPercentage = getBarFillPercentage(levelInfo);
		int barFillColour = getBarFillColour(levelInfo);
		List<Component> tooltip = buildTooltip(label, levelInfo, info.getLeaderboardPosition("catacombs"));
		Identifier tooltipStyle = getTooltipStyle(levelInfo, 10);

		return new DungeonLevelBarWidget(width, icon, label, barFillPercentage, barFillColour, tooltip, tooltipStyle);
	}

	public static DungeonLevelBarWidget createClass(int width, LoadingInformation info, DungeonClass clazz) {
		Dungeons dungeons = info.member().dungeons;
		LevelInfo levelInfo = dungeons.getClassData(clazz).getLevelInfo(info);

		Component label;

		if (dungeons.selectedDungeonClass.equals(clazz.apiName())) {
			label = Component.literal(clazz.displayName() + " " + levelInfo.level() + " ★").withStyle(ChatFormatting.GREEN);
		} else {
			label = Component.literal(clazz.displayName() + " " + levelInfo.level());
		}

		ItemStack icon = clazz.icon().getStackOrThrow();
		double barFillPercentage = getBarFillPercentage(levelInfo);
		int barFillColour = getBarFillColour(levelInfo);
		List<Component> tooltip = buildTooltip(label, levelInfo, info.getLeaderboardPosition(clazz.apiName() + "-xp"));
		Identifier tooltipStyle = getTooltipStyle(levelInfo, 10);

		return new DungeonLevelBarWidget(width, icon, label, barFillPercentage, barFillColour, tooltip, tooltipStyle);
	}

	public static DungeonLevelBarWidget createClassAverage(int width, LoadingInformation info) {
		LevelInfo levelInfo = info.member().dungeons.getClassAverage(info);

		Component label = Component.literal("Class Avg " + Formatters.FLOAT_NUMBERS.format(levelInfo.levelWithProgress()));
		ItemStack icon = ItemRepository.getItemStack("SPIRIT_WING", Ico.BARRIER).getStackOrThrow();
		double barFillPercentage = getBarFillPercentage(levelInfo);
		int barFillColour = getBarFillColour(levelInfo);
		List<Component> tooltip = buildTooltip(label, levelInfo, EliteLeaderboards.NO_POSITION);
		Identifier tooltipStyle = getTooltipStyle(levelInfo, 10);

		return new DungeonLevelBarWidget(width, icon, label, barFillPercentage, barFillColour, tooltip, tooltipStyle);
	}
}
