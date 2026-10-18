package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.model.GenericCatacombs;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.TimeFormatUtils;
import de.hysky.skyblocker.utils.Formatters;

public final class FloorRunsWidget extends BasicInfoBoxWidget {
	private static final int INFO_OFFSET = 2;
	private final LoadingInformation info;

	public FloorRunsWidget(int width, int height, LoadingInformation info) {
		super(width, height);
		this.info = info;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

		Font font = Minecraft.getInstance().font;
		ProfileMember member = this.info.member();
		GenericCatacombs normal = member.dungeons.dungeonTypes.catacombs;
		GenericCatacombs masterMode = member.dungeons.dungeonTypes.masterModeCatacombs;

		int x = this.getX() + INFO_OFFSET;
		int y = this.getY() + INFO_OFFSET;
		final int textYStep = font.lineHeight + 1;

		// Title (and after have the text start an additional 2 px below)
		Component title = Component.literal("Floor Runs").withStyle(ChatFormatting.BOLD);
		graphics.centeredText(font, title, x + (this.getWidth() - INFO_OFFSET * 2) / 2, y, CommonColors.WHITE);
		final int afterTitleY = (y += textYStep + 2);

		List<Component> normalStats = List.of(
				compileStatsLine("E ", 0, false, normal),
				compileStatsLine("F1", 1, false, normal),
				compileStatsLine("F2", 2, false, normal),
				compileStatsLine("F3", 3, false, normal),
				compileStatsLine("F4", 4, false, normal),
				compileStatsLine("F5", 5, false, normal),
				compileStatsLine("F6", 6, false, normal),
				compileStatsLine("F7", 7, false, normal)
				);
		List<Component> masterModeStats = List.of(
				Component.empty(),
				compileStatsLine("M1", 1, true, masterMode),
				compileStatsLine("M2", 2, true, masterMode),
				compileStatsLine("M3", 3, true, masterMode),
				compileStatsLine("M4", 4, true, masterMode),
				compileStatsLine("M5", 5, true, masterMode),
				compileStatsLine("M6", 6, true, masterMode),
				compileStatsLine("M7", 7, true, masterMode)
				);

		// Normal mode
		for (Component line : normalStats) {
			graphics.text(font, line, x, y, CommonColors.WHITE);
			y += textYStep;
		}

		// Start master mode stats from middle & top
		x = this.getX() + (this.getWidth() / 2);
		y = afterTitleY;

		// Master Mode
		for (Component line : masterModeStats) {
			graphics.text(font, line, x, y, CommonColors.WHITE);
			y += textYStep;
		}
	}

	private static Component compileStatsLine(String floorName, int floorIndex, boolean master, GenericCatacombs stats) {
		long sPlusTime = (long) stats.fastestTimeSPlus.getValueOrZero(floorIndex);
		long sTime = (long) stats.fastestTimeS.getValueOrZero(floorIndex);

		Component pbText = Component.literal("(None)").withStyle(ChatFormatting.GRAY);

		if (sPlusTime != 0) {
			pbText = Component.empty()
					.append(Component.literal("(").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(TimeFormatUtils.getDurationString(sPlusTime)).withStyle(ChatFormatting.GRAY))
					.append(Component.literal(" S+").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
		} else if (sTime != 0) {
			pbText = Component.empty()
					.append(Component.literal("(").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(TimeFormatUtils.getDurationString(sTime)).withStyle(ChatFormatting.GRAY))
					.append(Component.literal(" S").withStyle(ChatFormatting.LIGHT_PURPLE))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
		}

		Component floorText = Component.literal(floorName).withStyle(master ? ChatFormatting.RED : ChatFormatting.GREEN);
		Component runsText = Component.literal(Formatters.INTEGER_NUMBERS.format(stats.tierCompletions.getValueOrZero(floorIndex)));

		Component line = Component.empty()
				.append(floorText)
				.append(" ")
				.append(runsText)
				.append(" ")
				.append(pbText);

		return line;
	}
}
