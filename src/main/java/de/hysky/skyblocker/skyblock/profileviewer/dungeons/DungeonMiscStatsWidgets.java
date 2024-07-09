package de.hysky.skyblocker.skyblock.profileviewer.dungeons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class DungeonMiscStatsWidgets {
    private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/icon_data_widget.png");
    private static final Identifier RUN_ICON = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/run_icon.png");
    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static final DecimalFormat DF = new DecimalFormat("#.##");
    private static final String[] DUNGEONS = {"catacombs", "master_catacombs"};

    private final Map<String, Integer> dungeonRuns = new HashMap<>();
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

    public void render(DrawContext context, int x, int y) {
        context.drawTexture(TEXTURE, x, y, 0, 0, 109, 26, 109, 26);
        context.drawItem(Ico.FEATHER, x + 2, y + 4);

        context.drawText(textRenderer, "Secrets " + secrets, x + 30, y + 4, Color.WHITE.getRGB(), true);
        context.drawText(textRenderer, "Avg " + (totalRuns > 0 ? DF.format(secrets / (float) totalRuns) : 0) + "/Run", x + 30, y + 14, Color.WHITE.getRGB(), true);

        context.drawTexture(TEXTURE, x, y + 28, 0, 0, 109, 26, 109, 26);
        context.drawTexture(RUN_ICON, x + 4, y + 33, 0, 0, 14, 16, 14, 16);

        context.drawText(textRenderer, "§aNormal §r" + dungeonRuns.getOrDefault("catacombs", 0), x + 30, y + 32, Color.WHITE.getRGB(), true);
        context.drawText(textRenderer, "§cMaster §r" + dungeonRuns.getOrDefault("master_catacombs", 0), x + 30, y + 42, Color.WHITE.getRGB(), true);
    }
}
