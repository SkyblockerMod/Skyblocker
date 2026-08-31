package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.model.Dungeons;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.TimeFormatUtils;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;

public final class DailyRunsWidget extends BasicInfoBoxWidget {
	private static final int INFO_OFFSET = 2;
	private final LoadingInformation info;

	public DailyRunsWidget(int width, LoadingInformation info) {
		super(width, 22);
		this.info = info;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

		Font font = Minecraft.getInstance().font;
		ProfileMember member = this.info.member();
		Dungeons.DailyRuns dailyRuns = member.dungeons.dailyRuns;

		int x = this.getX() + INFO_OFFSET;
		int y = this.getY() + INFO_OFFSET;
		final int textYStep = font.lineHeight + 1;

		// Icon
		ItemStack icon = ItemRepository.getItemStack("ARCHITECT_FIRST_DRAFT", Ico.BARRIER).getStackOrThrow();
		int iconAreaSize = this.getHeight() - (INFO_OFFSET * 2);
		graphics.fakeItem(icon, x, y + (iconAreaSize - GuiRenderer.DEFAULT_ITEM_SIZE) / 2);
		x += GuiRenderer.DEFAULT_ITEM_SIZE + 2;

		int dailiesCompleted = dailyRuns.getDailyRunsCompleted();
		Component dailyRunsTest = Component.empty()
				.append(Component.literal("Daily Runs ").withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal(dailiesCompleted + "/5").withStyle(dailiesCompleted == Dungeons.DailyRuns.MAX_DAILIES ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
		graphics.text(font, dailyRunsTest, x, y, CommonColors.WHITE);
		y += textYStep;

		Component resetsInText = Component.empty()
				.append(Component.literal("Resets In ").withStyle(ChatFormatting.DARK_PURPLE))
				.append(TimeFormatUtils.getShortestReasonableUnit(dailyRuns.timeUntilReset()));
		graphics.text(font, resetsInText, x, y, CommonColors.WHITE);
		y += textYStep;
	}
}
