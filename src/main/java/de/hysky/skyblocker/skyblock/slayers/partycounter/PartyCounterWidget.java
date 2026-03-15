package de.hysky.skyblocker.skyblock.slayers.partycounter;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class PartyCounterWidget {
	private static final int BACKGROUND_COLOR = 0x80000000;
	private static final int BORDER_COLOR = 0xFF555555;
	private static final int HEADER_COLOR = 0xFFFFAA00;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int COUNT_COLOR = 0xFF55FF55;
	private static final int PADDING = 4;
	private static final int LINE_HEIGHT = 10;

	private PartyCounterWidget() {
	}

	public static void render(GuiGraphics graphics) {
		if (!SkyblockerConfigManager.get().slayers.partySlayerCounter.enablePartyCounter) return;
		if (!SkyblockerConfigManager.get().slayers.partySlayerCounter.showWidget) return;
		if (!PartyTracker.isInParty()) return;

		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		Map<String, Integer> killCounts = PartySlayerCounter.getKillCounts();
		if (killCounts.isEmpty()) return;

		Font font = client.font;

		List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(killCounts.entrySet());
		sortedEntries.sort(Comparator.comparingInt(e -> -e.getValue()));

		int maxWidth = font.width("Party Slayer Counter");
		for (Map.Entry<String, Integer> entry : sortedEntries) {
			String line = entry.getKey() + ": " + entry.getValue();
			int width = font.width(line);
			if (width > maxWidth) maxWidth = width;
		}

		int totalKills = PartySlayerCounter.getTotalKills();
		String totalLine = "Total: " + totalKills;
		int totalWidth = font.width(totalLine);
		if (totalWidth > maxWidth) maxWidth = totalWidth;

		int boxWidth = maxWidth + PADDING * 2;
		int boxHeight = PADDING * 2 + LINE_HEIGHT + (sortedEntries.size() + 1) * LINE_HEIGHT + 2;

		int screenWidth = client.getWindow().getGuiScaledWidth();
		int screenHeight = client.getWindow().getGuiScaledHeight();

		var config = SkyblockerConfigManager.get().slayers.partySlayerCounter;
		int x = (int) (config.widgetX * screenWidth);
		int y = (int) (config.widgetY * screenHeight);

		x = Math.max(0, Math.min(x, screenWidth - boxWidth));
		y = Math.max(0, Math.min(y, screenHeight - boxHeight));

		graphics.fill(x, y, x + boxWidth, y + boxHeight, BACKGROUND_COLOR);

		graphics.hLine(x, x + boxWidth - 1, y, BORDER_COLOR);
		graphics.hLine(x, x + boxWidth - 1, y + boxHeight - 1, BORDER_COLOR);
		graphics.vLine(x, y, y + boxHeight - 1, BORDER_COLOR);
		graphics.vLine(x + boxWidth - 1, y, y + boxHeight - 1, BORDER_COLOR);

		int textY = y + PADDING;
		graphics.drawString(font, Component.literal("Party Slayer Counter").withStyle(ChatFormatting.GOLD), x + PADDING, textY, HEADER_COLOR, true);
		textY += LINE_HEIGHT + 2;

		for (Map.Entry<String, Integer> entry : sortedEntries) {
			graphics.drawString(font, Component.literal(entry.getKey() + ": ").withStyle(ChatFormatting.WHITE), x + PADDING, textY, TEXT_COLOR, true);
			int nameWidth = font.width(entry.getKey() + ": ");
			graphics.drawString(font, Component.literal(String.valueOf(entry.getValue())).withStyle(ChatFormatting.GREEN), x + PADDING + nameWidth, textY, COUNT_COLOR, true);
			textY += LINE_HEIGHT;
		}

		graphics.hLine(x + PADDING, x + boxWidth - PADDING - 1, textY - 2, 0xFF444444);
		graphics.drawString(font, Component.literal("Total: ").withStyle(ChatFormatting.YELLOW), x + PADDING, textY, 0xFFFFFF55, true);
		int totalTextWidth = font.width("Total: ");
		graphics.drawString(font, Component.literal(String.valueOf(totalKills)).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), x + PADDING + totalTextWidth, textY, COUNT_COLOR, true);
	}
}
