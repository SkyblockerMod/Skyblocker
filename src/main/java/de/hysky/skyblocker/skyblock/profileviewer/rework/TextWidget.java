package de.hysky.skyblocker.skyblock.profileviewer.rework;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class TextWidget implements ProfileViewerWidget {
	private final TextRenderer textRenderer;
	private final Text text;
	private final int offset;

	public TextWidget(TextRenderer textRenderer, Text text, boolean centered) {
		this.textRenderer = textRenderer;
		this.text = text;
		this.offset = centered ? -textRenderer.getWidth(text) / 2 : 0;
	}

	public static TextWidget leftAligned(Text text) {
		return new TextWidget(MinecraftClient.getInstance().textRenderer, text, false);
	}

	public static TextWidget centered(Text text) {
		return new TextWidget(MinecraftClient.getInstance().textRenderer, text, true);
	}

	@Override
	public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, float deltaTicks) {
		drawContext.drawText(textRenderer, text, x + offset, y, -1, true);
	}
}
