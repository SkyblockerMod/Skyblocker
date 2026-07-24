package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.config.entries.WidgetsListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import java.util.List;

public final class SeparatorEntry extends WidgetsListEntry {
	@Override
	public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		graphics.centeredText(Minecraft.getInstance().font, Component.nullToEmpty("- Skyblocker Widgets -"), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 9) / 2, CommonColors.WHITE);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of();
	}

	@Override
	public void extractBorder(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {}
}
