package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public final class TestTextWidget extends ProfileViewerWidget {

	public TestTextWidget(Component message) {
		// Statements before super cannot come fast enough ;(
		super(18, 18, Minecraft.getInstance().font.width(message), Minecraft.getInstance().font.lineHeight, message);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		graphics.drawString(Minecraft.getInstance().font, this.getMessage(), this.getX(), this.getY(), CommonColors.WHITE);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {}
}
