package de.hysky.skyblocker.skyblock.tabhud.config.entries;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import de.hysky.skyblocker.utils.render.GuiHelper;

public abstract class WidgetsListEntry extends ContainerObjectSelectionList.Entry<WidgetsListEntry> {
	public static final Component ENABLED_TEXT = Component.literal("ENABLED").withStyle(ChatFormatting.GREEN);
	public static final Component DISABLED_TEXT = Component.literal("DISABLED").withStyle(ChatFormatting.RED);

	public void extractTooltip(GuiGraphicsExtractor graphics, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {}

	public void extractBorder(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		if (hovered) GuiHelper.border(graphics, x, y, entryWidth, entryHeight, -1);
	}

	@Override
	public List<? extends NarratableEntry> narratables() {
		return List.of();
	}

	protected void extractIconAndText(GuiGraphicsExtractor graphics, ItemStack icon, int y, int x, int entryHeight) {
		Font textRenderer = Minecraft.getInstance().font;
		graphics.item(icon, x + 2, y + (entryHeight - 16) / 2);
		graphics.text(textRenderer, icon.getHoverName(), x + 20, y + (entryHeight - 9) / 2, CommonColors.WHITE, true);
	}

}
