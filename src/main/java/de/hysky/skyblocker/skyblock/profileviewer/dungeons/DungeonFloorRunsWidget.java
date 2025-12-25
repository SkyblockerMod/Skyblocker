package de.hysky.skyblocker.skyblock.profileviewer.dungeons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DungeonFloorRunsWidget {
	private static final Font textRenderer = Minecraft.getInstance().font;
	private static final ResourceLocation TEXTURE = SkyblockerMod.id("textures/gui/profile_viewer/dungeons_body.png");

	private static final String[] DUNGEONS = {"catacombs", "master_catacombs"};
	private JsonObject dungeonsStats;

	public DungeonFloorRunsWidget(JsonObject pProfile) {
		try {
			dungeonsStats = pProfile.getAsJsonObject("dungeons").getAsJsonObject("dungeon_types");
		} catch (Exception ignored) {}
	}

	public void render(GuiGraphics context, int mouseX, int mouseY, int x, int y) {
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 109, 110, 109, 110);
		context.drawString(textRenderer, Component.literal("Floor Runs").withStyle(ChatFormatting.BOLD), x + 6, y + 4, Color.WHITE.getRGB(), true);

		int columnX = x + 4;
		int elementY = y + 15;
		for (String dungeon : DUNGEONS) {
			JsonObject dungeonData;
			try {
				dungeonData = dungeonsStats.getAsJsonObject(dungeon).getAsJsonObject("tier_completions");
				List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(dungeonData.entrySet());
				entries.sort(Comparator.comparing(Map.Entry::getKey));

				for (Map.Entry<String, JsonElement> entry : entries) {
					if (entry.getKey().equals("total")) continue;

					String textToRender = String.format((dungeon.equals("catacombs") ? "§aF" : "§cM") + "%s§r %s", entry.getKey(), entry.getValue().getAsInt());
					context.drawString(textRenderer, textToRender, columnX + 2, elementY + 2, Color.WHITE.getRGB(), true);
					if (!entry.getKey().equals("0") && mouseX >= columnX && mouseX <= columnX + 40 && mouseY >= elementY && mouseY <= elementY + 9) {
						List<Component> tooltipText = new ArrayList<>();
						tooltipText.add(Component.literal("Personal Bests").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE));

						JsonObject fastestTimes = dungeonsStats.getAsJsonObject(dungeon).getAsJsonObject("fastest_time_s");
						if (fastestTimes != null && fastestTimes.has(entry.getKey())) {
							tooltipText.add(Component.literal("S Run:  " + formatTime(fastestTimes.get(entry.getKey()).getAsLong())).withStyle(ChatFormatting.GOLD));
						}

						fastestTimes = dungeonsStats.getAsJsonObject(dungeon).getAsJsonObject("fastest_time_s_plus");
						if (fastestTimes != null && fastestTimes.has(entry.getKey())) {
							tooltipText.add(Component.literal("S+ Run: " + formatTime(fastestTimes.get(entry.getKey()).getAsLong())).withStyle(ChatFormatting.GOLD));
						}

						fastestTimes = dungeonsStats.getAsJsonObject(dungeon).getAsJsonObject("fastest_time");
						if (fastestTimes != null && fastestTimes.has(entry.getKey()) && tooltipText.size() == 1) {
							tooltipText.add(Component.literal("Completion:  " + formatTime(fastestTimes.get(entry.getKey()).getAsLong())).withStyle(ChatFormatting.GOLD));
						}

						context.setComponentTooltipForNextFrame(textRenderer, tooltipText, mouseX, mouseY);
					}

					elementY += 11;
				}
				columnX += 52;
				elementY = y + 26;
			} catch (Exception e) {
				return;
			}
		}
	}

	private String formatTime(long milliseconds) {
		long seconds = milliseconds / 1000;
		long minutes = seconds / 60;
		seconds %= 60;
		return String.format("%2d:%02d", minutes, seconds);
	}
}
