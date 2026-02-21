package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class CompositeWidget extends ProfileViewerWidget {

	protected CompositeWidget(int x, int y, int width, int height, boolean padToContent, Component message) {
		super(x, y, width, height, padToContent, message);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {

	}
}
