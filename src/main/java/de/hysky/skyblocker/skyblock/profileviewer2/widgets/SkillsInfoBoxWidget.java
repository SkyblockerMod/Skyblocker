package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.time.Instant;
import java.time.ZoneId;

import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelCalculator;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.GuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public final class SkillsInfoBoxWidget extends BasicInfoBoxWidget {
	private static final int INFO_OFFSET = 2;
	private final ApiProfile profile;
	private final ProfileMember member;

	public SkillsInfoBoxWidget(int width, int height, ApiProfile profile, ProfileMember member) {
		super(width, height);
		this.profile = profile;
		this.member = member;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

		graphics.pose().pushMatrix();
		graphics.pose().translate(INFO_OFFSET, INFO_OFFSET);

		Font font = Minecraft.getInstance().font;
		Tooltip tooltip = null;

		// Profile name
		graphics.pose().pushMatrix();
		// Offset by 1 upwards so that the painting does not intersect the joined text
		graphics.pose().translate(this.getX(), this.getY() - 1);
		graphics.pose().scale(0.75f);
		graphics.fakeItem(Ico.PAINTING.getStackOrThrow(), 0, 0);
		graphics.pose().popMatrix();

		// The item is scaled down to 12x12 so we use 12 as the base x offset and offset by 2 more so that there's space between
		// the text and icon
		Component profileText = Component.literal(this.profile.cuteName).withStyle(ChatFormatting.UNDERLINE);
		graphics.text(font, profileText, this.getX() + 12 + 2, this.getY(), CommonColors.WHITE);

		// Offset all following elements by 1 to ensure that the joined text does not "clash" with the underline
		graphics.pose().translate(0f, 1f);

		// Joined
		Instant firstJoin = Instant.ofEpochMilli(this.member.profile.firstJoin);
		int firstJoinYear = Instant.ofEpochMilli(this.member.profile.firstJoin)
				.atZone(ZoneId.systemDefault())
				.getYear();
		Component joinedText = Component.empty()
				.append(Component.literal("Joined: ").withStyle(ChatFormatting.GREEN))
				.append(String.valueOf(firstJoinYear));
		int joinedY = this.getY() + font.lineHeight + 1;
		graphics.text(font, joinedText, this.getX(), joinedY, CommonColors.WHITE);

		// Add the date as a tooltip when the text is hovered over
		if (GuiHelper.pointIsInArea(mouseX, mouseY, this.getX() + INFO_OFFSET, joinedY + INFO_OFFSET, this.getX() + INFO_OFFSET + font.width(joinedText), joinedY + font.lineHeight)) {
			tooltip = Tooltip.create(Component.literal(Formatters.DATE_FORMATTER.format(firstJoin)));
		}

		// SkyBlock Level
		// TODO add emblems
		int skyblockLevel = LevelCalculator.getSkyblockLevel(member.levelling.experience);
		Component levelText = Component.empty()
				.append(Component.literal("Level: ").withStyle(ChatFormatting.GREEN))
				.append(String.valueOf(skyblockLevel));
		graphics.text(font, levelText, this.getX(), this.getY() + (font.lineHeight * 2) + 1, CommonColors.WHITE);

		// Skill Average
		Component skillAverageText = Component.empty()
				.append(Component.literal("Skill Avg: ").withStyle(ChatFormatting.GREEN))
				.append(Formatters.FLOAT_NUMBERS.format(this.member.playerData.getSkillAverage(this.member)));
		graphics.text(font, skillAverageText, this.getX(), this.getY() + (font.lineHeight * 3) + 1, CommonColors.WHITE);

		// Purse
		Component purseText = Component.empty()
				.append(Component.literal("Purse: ").withStyle(ChatFormatting.GOLD))
				.append(Formatters.SHORT_FLOAT_NUMBERS.format(this.member.currencies.coinsInPurse));
		graphics.text(font, purseText, this.getX(), this.getY() + (font.lineHeight * 4) + 1, CommonColors.WHITE);

		// Bank
		// TODO check that banking can't be null
		Component bankText = Component.empty()
				.append(Component.literal("Bank: ").withStyle(ChatFormatting.GOLD))
				.append(Formatters.SHORT_FLOAT_NUMBERS.format(this.profile.banking.balance));
		graphics.text(font, bankText, this.getX(), this.getY() + (font.lineHeight * 5) + 1, CommonColors.WHITE);

		graphics.pose().popMatrix();
		this.setTooltip(tooltip);
	}
}
