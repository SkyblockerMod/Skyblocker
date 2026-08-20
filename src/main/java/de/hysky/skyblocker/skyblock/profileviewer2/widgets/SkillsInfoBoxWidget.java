package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.time.Instant;
import java.time.ZoneId;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.GuiHelper;

public final class SkillsInfoBoxWidget extends BasicInfoBoxWidget {
	private static final int INFO_OFFSET = 2;
	private final LoadingInformation info;

	public SkillsInfoBoxWidget(int width, int height, LoadingInformation info) {
		super(width, height);
		this.info = info;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

		Font font = Minecraft.getInstance().font;
		ApiProfile profile = this.info.profile();
		ProfileMember member = this.info.member();

		final int x = this.getX() + INFO_OFFSET;
		int y = this.getY() + INFO_OFFSET;
		final int textYStep = font.lineHeight + 1;

		Tooltip tooltip = null;

		// Profile name
		graphics.pose().pushMatrix();
		// Offset by 1 upwards so that the painting does not intersect the joined text
		graphics.pose().translate(x, y - 1);
		graphics.pose().scale(0.75f);
		graphics.fakeItem(Ico.PAINTING.getStackOrThrow(), 0, 0);
		graphics.pose().popMatrix();

		// The item is scaled down to 12x12 so we use 12 as the base x offset and offset by 2 more so that there's space between the text and icon
		Component profileText = Component.literal(profile.cuteName).withStyle(ChatFormatting.UNDERLINE);
		graphics.text(font, profileText, x + 12 + 2, y, CommonColors.WHITE);

		// Offset all following elements by 1 to ensure that the joined text does not "clash" with the underline
		y += 1;

		// Joined
		Instant firstJoin = Instant.ofEpochMilli(member.profile.firstJoin);
		int firstJoinYear = Instant.ofEpochMilli(member.profile.firstJoin)
				.atZone(ZoneId.systemDefault())
				.getYear();
		Component joinedText = Component.empty()
				.append(Component.literal("Joined: ").withStyle(ChatFormatting.GREEN))
				.append(String.valueOf(firstJoinYear));
		y += textYStep;
		graphics.text(font, joinedText, x, y, CommonColors.WHITE);

		// Add the date as a tooltip when the text is hovered over
		if (GuiHelper.pointIsInArea(mouseX, mouseY, x, y, x + font.width(joinedText), y + font.lineHeight)) {
			tooltip = Tooltip.create(Component.literal(Formatters.DATE_FORMATTER.format(firstJoin)));
		}

		// SkyBlock Emblem
		y += textYStep;
		Component levelText = Component.empty()
				.append(Component.literal("Emblem: X").withStyle(ChatFormatting.GREEN));
		graphics.text(font, levelText, x, y, CommonColors.WHITE);

		// Skill Average
		y += textYStep;
		Component skillAverageText = Component.empty()
				.append(Component.literal("Skill Avg: ").withStyle(ChatFormatting.GREEN))
				.append(Formatters.FLOAT_NUMBERS.format(member.playerData.getSkillAverage(this.info)));
		graphics.text(font, skillAverageText, x, y, CommonColors.WHITE);

		// Purse
		y += textYStep;
		Component purseText = Component.empty()
				.append(Component.literal("Purse: ").withStyle(ChatFormatting.GOLD))
				.append(Formatters.SHORT_FLOAT_NUMBERS.format(member.currencies.coinsInPurse));
		graphics.text(font, purseText, x, y, CommonColors.WHITE);

		// Bank
		y += textYStep;
		// TODO check that banking can't be null
		Component bankText = Component.empty()
				.append(Component.literal("Bank: ").withStyle(ChatFormatting.GOLD))
				.append(Formatters.SHORT_FLOAT_NUMBERS.format(profile.banking.balance));
		graphics.text(font, bankText, x, y, CommonColors.WHITE);

		this.setTooltip(tooltip);
	}
}
