package de.hysky.skyblocker.skyblock.slayers.partycounter;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class PartyCounterWidgetScreen extends Screen {
	private static final int BACKGROUND_COLOR = 0x80000000;
	private static final int HEADER_COLOR = 0xFFFFAA00;
	private static final int PADDING = 4;
	private static final int LINE_HEIGHT = 10;

	private final Screen parent;
	private boolean dragging = false;
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;
	private float widgetX;
	private float widgetY;

	public PartyCounterWidgetScreen(Screen parent) {
		super(Component.translatable("skyblocker.config.slayer.partySlayerCounter.widgetPosition"));
		this.parent = parent;
		this.widgetX = SkyblockerConfigManager.get().slayers.partySlayerCounter.widgetX;
		this.widgetY = SkyblockerConfigManager.get().slayers.partySlayerCounter.widgetY;
	}

	@Override
	protected void init() {
		addRenderableWidget(Button.builder(Component.translatable("skyblocker.partySlayerCounter.widgetScreen.save"), button -> {
			SkyblockerConfigManager.update(config -> {
				config.slayers.partySlayerCounter.widgetX = widgetX;
				config.slayers.partySlayerCounter.widgetY = widgetY;
			});
			minecraft.setScreen(parent);
		}).bounds(width / 2 - 100, height - 30, 95, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("skyblocker.partySlayerCounter.widgetScreen.cancel"), button -> {
			minecraft.setScreen(parent);
		}).bounds(width / 2 + 5, height - 30, 95, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("skyblocker.partySlayerCounter.widgetScreen.reset"), button -> {
			widgetX = 0.01f;
			widgetY = 0.3f;
		}).bounds(width / 2 - 50, height - 55, 100, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawCenteredString(font,
				Component.literal("Drag the widget to reposition it").withStyle(ChatFormatting.YELLOW),
				width / 2, 20, 0xFFFFFF);
		graphics.drawCenteredString(font,
				Component.literal("Click and drag the box below").withStyle(ChatFormatting.GRAY),
				width / 2, 35, 0xAAAAAA);
		renderPreviewWidget(graphics, mouseX, mouseY);
	}

	private void renderPreviewWidget(GuiGraphics graphics, int mouseX, int mouseY) {
		String[] previewNames = {"Player1", "Player2", "Player3"};
		int[] previewCounts = {5, 3, 1};

		int maxWidth = font.width("Party Slayer Counter");
		for (int i = 0; i < previewNames.length; i++) {
			String line = previewNames[i] + ": " + previewCounts[i];
			int w = font.width(line);
			if (w > maxWidth) maxWidth = w;
		}

		int boxWidth = maxWidth + PADDING * 2;
		int boxHeight = PADDING * 2 + LINE_HEIGHT + (previewNames.length + 1) * LINE_HEIGHT + 2;

		int x = (int) (widgetX * width);
		int y = (int) (widgetY * height);
		x = Math.max(0, Math.min(x, width - boxWidth));
		y = Math.max(50, Math.min(y, height - boxHeight - 60));

		graphics.fill(x, y, x + boxWidth, y + boxHeight, BACKGROUND_COLOR);

		boolean hovered = mouseX >= x && mouseX <= x + boxWidth && mouseY >= y && mouseY <= y + boxHeight;
		int borderColor = hovered || dragging ? 0xFFFFAA00 : 0xFF555555;

		graphics.hLine(x, x + boxWidth - 1, y, borderColor);
		graphics.hLine(x, x + boxWidth - 1, y + boxHeight - 1, borderColor);
		graphics.vLine(x, y, y + boxHeight - 1, borderColor);
		graphics.vLine(x + boxWidth - 1, y, y + boxHeight - 1, borderColor);

		int textY = y + PADDING;
		graphics.drawString(font, Component.literal("Party Slayer Counter").withStyle(ChatFormatting.GOLD), x + PADDING, textY, HEADER_COLOR, true);
		textY += LINE_HEIGHT + 2;

		for (int i = 0; i < previewNames.length; i++) {
			graphics.drawString(font, Component.literal(previewNames[i] + ": ").withStyle(ChatFormatting.WHITE), x + PADDING, textY, 0xFFFFFFFF, true);
			int nameWidth = font.width(previewNames[i] + ": ");
			graphics.drawString(font, Component.literal(String.valueOf(previewCounts[i])).withStyle(ChatFormatting.GREEN), x + PADDING + nameWidth, textY, 0xFF55FF55, true);
			textY += LINE_HEIGHT;
		}

		int total = 9;
		graphics.hLine(x + PADDING, x + boxWidth - PADDING - 1, textY - 2, 0xFF444444);
		graphics.drawString(font, Component.literal("Total: ").withStyle(ChatFormatting.YELLOW), x + PADDING, textY, 0xFFFFFF55, true);
		int totalTextWidth = font.width("Total: ");
		graphics.drawString(font, Component.literal(String.valueOf(total)).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), x + PADDING + totalTextWidth, textY, 0xFF55FF55, true);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (click.button() == 0) {
			int maxWidth = font.width("Party Slayer Counter");
			int boxWidth = maxWidth + PADDING * 2;
			int boxHeight = PADDING * 2 + LINE_HEIGHT + 4 * LINE_HEIGHT + 2;

			int x = (int) (widgetX * width);
			int y = (int) (widgetY * height);
			x = Math.max(0, Math.min(x, width - boxWidth));
			y = Math.max(50, Math.min(y, height - boxHeight - 60));

			if (click.x() >= x && click.x() <= x + boxWidth && click.y() >= y && click.y() <= y + boxHeight) {
				dragging = true;
				dragOffsetX = (int) click.x() - x;
				dragOffsetY = (int) click.y() - y;
				return true;
			}
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent click) {
		if (click.button() == 0) {
			dragging = false;
		}
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
		if (dragging) {
			int newX = (int) click.x() - dragOffsetX;
			int newY = (int) click.y() - dragOffsetY;

			widgetX = (float) newX / width;
			widgetY = (float) newY / height;

			widgetX = Math.max(0, Math.min(widgetX, 1.0f));
			widgetY = Math.max(0, Math.min(widgetY, 1.0f));
			return true;
		}
		return super.mouseDragged(click, deltaX, deltaY);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
