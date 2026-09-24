package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.awt.Color;
import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelCalculator;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelInfo;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;

public final class SkyBlockLevelBarWidget extends LevelBarWidget {

	private SkyBlockLevelBarWidget(int width, ItemStack icon, Component label, double barFillPercentage, int barFillColour, List<Component> tooltip, @Nullable Identifier tooltipStyle) {
		super(width, icon, label, barFillPercentage, barFillColour, tooltip, tooltipStyle);
	}

	public static LevelBarWidget create(int width, LoadingInformation info) {
		LevelInfo levelInfo = LevelCalculator.getSkyblockLevel(info.member().levelling.experience);
		int level = levelInfo.level();

		Component label = Component.empty()
				.append("SB Level ")
				.append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
				.append(Component.literal(String.valueOf(level)).withStyle(getLevelColour(level)))
				.append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
		ItemStack icon = Ico.EXPERIENCE_BOTTLE.getStackOrThrow();
		double barFillPercentage = getBarFillPercentage(levelInfo);
		int barFillColour = Color.CYAN.getRGB();
		List<Component> tooltip = buildTooltip(label, levelInfo, info.getLeaderboardPosition("skyblockxp"));
		Identifier tooltipStyle = getTooltipStyle(levelInfo, 100);

		return new SkyBlockLevelBarWidget(width, icon, label, barFillPercentage, barFillColour, tooltip, tooltipStyle);
	}

	private static ChatFormatting getLevelColour(int level) {
		if (level >= 480) {
			return ChatFormatting.DARK_RED;
		} else if (level >= 440) {
			return ChatFormatting.RED;
		} else if (level >= 400) {
			return ChatFormatting.GOLD;
		} else if (level >= 360) {
			return ChatFormatting.DARK_PURPLE;
		} else if (level >= 320) {
			return ChatFormatting.LIGHT_PURPLE;
		} else if (level >= 280) {
			return ChatFormatting.BLUE;
		} else if (level >= 240) {
			return ChatFormatting.DARK_AQUA;
		} else if (level >= 200) {
			return ChatFormatting.AQUA;
		} else if (level >= 160) {
			return ChatFormatting.DARK_GREEN;
		} else if (level >= 140) {
			return ChatFormatting.GREEN;
		} else if (level >= 100) {
			return ChatFormatting.YELLOW;
		} else {
			return ChatFormatting.WHITE;
		}
	}
}
