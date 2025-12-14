package de.hysky.skyblocker.skyblock.tabhud.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class PlaceholderWidget extends HudWidget {

	public PlaceholderWidget(String id) {
		super(new Information(id, Component.literal(id), l -> false));
	}

	@Override
	protected void renderWidget(GuiGraphics context, float delta) {
		w = h = 0;
	}

	@Override
	public void renderWidgetConfig(GuiGraphics context, float delta) {
		Font textRenderer = Minecraft.getInstance().font;
		h = 15;
		w = textRenderer.width(getInformation().displayName()) + 10;
		context.fill(0, 0, getWidth(), getHeight(), 0xAA_00_00_00);
		context.drawCenteredString(textRenderer, getInformation().displayName(), getWidth() / 2, 3, -1);
	}
}
