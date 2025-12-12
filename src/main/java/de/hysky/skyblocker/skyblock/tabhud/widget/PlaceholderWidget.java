package de.hysky.skyblocker.skyblock.tabhud.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class PlaceholderWidget extends HudWidget {

	public PlaceholderWidget(String id) {
		super(new Information(id, Text.literal(id), l -> false));
	}

	@Override
	protected void renderWidget(DrawContext context, float delta) {
		w = h = 0;
	}

	@Override
	public void renderWidgetConfig(DrawContext context, float delta) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		h = 15;
		w = textRenderer.getWidth(getInformation().displayName()) + 10;
		context.fill(0, 0, getWidth(), getHeight(), 0xAA_00_00_00);
		context.drawCenteredTextWithShadow(textRenderer, getInformation().displayName(), getWidth() / 2, 3, -1);
	}
}
