package de.hysky.skyblocker.skyblock.tabhud.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class PlaceholderWidget extends HudWidget {

	private final Information information;

	public PlaceholderWidget(String id) {
		information = new Information(id, Text.literal(id), l -> false);
	}

	@Override
	protected void renderWidget(DrawContext context, float delta) {
		w = h = 0;
	}

	@Override
	public void renderWidgetConfig(DrawContext context, float delta) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		h = 15;
		w = textRenderer.getWidth(information.displayName()) + 10;
		context.fill(0, 0, getWidth(), getHeight(), 0xAA_00_00_00);
		context.drawCenteredTextWithShadow(textRenderer, information.displayName(), getWidth() / 2, 3, -1);
	}

	@Override
	public @NotNull Information getInformation() {
		return information;
	}
}
