package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.utils.Formatters;

public final class DungeonRunsWidget extends BasicInfoBoxWidget {
	private static final int INFO_OFFSET = 2;
	private static final Identifier RUNNING_MAN = SkyblockerMod.id("profile_viewer2/running_man");
	private final LoadingInformation info;

	public DungeonRunsWidget(int width, LoadingInformation info) {
		super(width, 22);
		this.info = info;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

		Font font = Minecraft.getInstance().font;
		ProfileMember member = this.info.member();

		int x = this.getX() + INFO_OFFSET;
		int y = this.getY() + INFO_OFFSET;
		final int textYStep = font.lineHeight + 1;

		// Icon
		int iconSize = GuiRenderer.DEFAULT_ITEM_SIZE;
		int iconAreaSize = this.getHeight() - (INFO_OFFSET * 2);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, RUNNING_MAN, x, y + (iconAreaSize - iconSize) / 2, iconSize, iconSize);
		x += GuiRenderer.DEFAULT_ITEM_SIZE + 2;

		Component normalRunsText = Component.empty()
				.append(Component.literal("Normal ").withStyle(ChatFormatting.GREEN))
				.append(Formatters.INTEGER_NUMBERS.format(member.dungeons.dungeonTypes.catacombs.tierCompletions.getManuallyCalculatedTotal()));
		graphics.text(font, normalRunsText, x, y, CommonColors.WHITE);
		y += textYStep;

		Component masterRunsText = Component.empty()
				.append(Component.literal("Master ").withStyle(ChatFormatting.RED))
				.append(Formatters.INTEGER_NUMBERS.format(member.dungeons.dungeonTypes.masterModeCatacombs.tierCompletions.getManuallyCalculatedTotal()));
		graphics.text(font, masterRunsText, x, y, CommonColors.WHITE);
		y += textYStep;
	}
}
