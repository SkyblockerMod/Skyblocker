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
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.Formatters;

public final class DungeonSecretsWidget extends BasicInfoBoxWidget {
	private static final int INFO_OFFSET = 2;
	private final LoadingInformation info;

	public DungeonSecretsWidget(int width, LoadingInformation info) {
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
		ItemStack icon = ItemRepository.getItemStack("KISMET_FEATHER", Ico.BARRIER).getStackOrThrow();
		int iconAreaSize = this.getHeight() - (INFO_OFFSET * 2);
		graphics.fakeItem(icon, x, y + (iconAreaSize - GuiRenderer.DEFAULT_ITEM_SIZE) / 2);
		x += GuiRenderer.DEFAULT_ITEM_SIZE + 2;

		Component totalSecretsText = Component.empty()
				.append(Component.literal("Secrets ").withStyle(ChatFormatting.GOLD))
				.append(Formatters.INTEGER_NUMBERS.format(member.dungeons.secrets));
		graphics.text(font, totalSecretsText, x, y, CommonColors.WHITE);
		y += textYStep;

		Component avgSecretsPerRunText = Component.empty()
				.append(Component.literal("Avg/Run ").withStyle(ChatFormatting.YELLOW))
				.append(Formatters.DOUBLE_NUMBERS.format(member.dungeons.getSecretsPerRun()));
		graphics.text(font, avgSecretsPerRunText, x, y, CommonColors.WHITE);
		y += textYStep;
	}
}
