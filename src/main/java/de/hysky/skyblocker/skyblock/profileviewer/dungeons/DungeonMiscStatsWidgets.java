package de.hysky.skyblocker.skyblock.profileviewer.dungeons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class DungeonMiscStatsWidgets {
	private static final ResourceLocation TEXTURE = SkyblockerMod.id("textures/gui/profile_viewer/icon_data_widget.png");
	private static final ResourceLocation RUN_ICON = SkyblockerMod.id("textures/gui/profile_viewer/run_icon.png");
	private static final Font textRenderer = Minecraft.getInstance().font;
	private static final DecimalFormat DF = new DecimalFormat("#.##");
	private static final String[] DUNGEONS = {"catacombs", "master_catacombs"};

	private final Object2IntMap<String> dungeonRuns = new Object2IntOpenHashMap<>();
	private int secrets = 0;
	private int totalRuns = 0;

	public DungeonMiscStatsWidgets(JsonObject pProfile) {
		JsonObject DUNGEONS_DATA = pProfile.getAsJsonObject("dungeons");
		try {
			secrets = DUNGEONS_DATA.get("secrets").getAsInt();

			for (String dungeon : DUNGEONS) {
				JsonObject dungeonData = DUNGEONS_DATA.getAsJsonObject("dungeon_types").getAsJsonObject(dungeon).getAsJsonObject("tier_completions");
				int runs = 0;
				for (Map.Entry<String, JsonElement> entry : dungeonData.entrySet()) {
					String key = entry.getKey();
					if (key.equals("total")) continue;
					runs += entry.getValue().getAsInt();
				}
				dungeonRuns.put(dungeon, runs);
				totalRuns += runs;
			}

		} catch (Exception ignored) {}
	}

	public void render(GuiGraphics context, int x, int y) {
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 109, 26, 109, 26);
		context.renderItem(Ico.FEATHER, x + 2, y + 4);

		context.drawString(textRenderer, "Secrets " + secrets, x + 30, y + 4, Color.WHITE.getRGB(), true);
		context.drawString(textRenderer, "Avg " + (totalRuns > 0 ? DF.format(secrets / (float) totalRuns) : 0) + "/Run", x + 30, y + 14, Color.WHITE.getRGB(), true);

		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 28, 0, 0, 109, 26, 109, 26);
		context.blit(RenderPipelines.GUI_TEXTURED, RUN_ICON, x + 4, y + 33, 0, 0, 14, 16, 14, 16);

		context.drawString(textRenderer, "§aNormal §r" + dungeonRuns.getOrDefault("catacombs", 0), x + 30, y + 32, Color.WHITE.getRGB(), true);
		context.drawString(textRenderer, "§cMaster §r" + dungeonRuns.getOrDefault("master_catacombs", 0), x + 30, y + 42, Color.WHITE.getRGB(), true);
	}
}
