package de.hysky.skyblocker.skyblock.tabhud.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class PlaceholderWidget extends HudWidget {

	public PlaceholderWidget(String id) {
		super(new Information(id, Component.literal(id), _ -> false));
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, float delta) {
		w = h = 0;
	}

	@Override
	public void extractWidgetRenderStateForConfig(GuiGraphicsExtractor context, float delta) {
		Font textRenderer = Minecraft.getInstance().font;
		h = 15;
		w = textRenderer.width(getInformation().displayName()) + 10;
		context.fill(0, 0, getWidth(), getHeight(), 0xAA_00_00_00);
		context.centeredText(textRenderer, getInformation().displayName(), getWidth() / 2, 3, -1);
	}
}
