package de.hysky.skyblocker.skyblock.profileviewer.dungeons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DungeonFloorRunsWidget {
    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/dungeons_body.png");

    private static final String[] DUNGEONS = {"catacombs", "master_catacombs"};
    private JsonObject dungeonsStats;

    public DungeonFloorRunsWidget(JsonObject pProfile) {
        try {
            dungeonsStats = pProfile.getAsJsonObject("dungeons").getAsJsonObject("dungeon_types");
        } catch (Exception ignored) {}
    }

    public void render(DrawContext context, int mouseX, int mouseY, int x, int y) {
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x, y, 0, 0, 109, 110, 109, 110);
        context.drawText(textRenderer, Text.literal("Floor Runs").formatted(Formatting.BOLD), x + 6, y + 4, Color.WHITE.getRGB(), true);

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
                    context.drawText(textRenderer, textToRender, columnX + 2, elementY + 2, Color.WHITE.getRGB(), true);
                    if (!entry.getKey().equals("0") && mouseX >= columnX && mouseX <= columnX + 40 && mouseY >= elementY && mouseY <= elementY + 9) {
                        List<Text> tooltipText = new ArrayList<>();
                        tooltipText.add(Text.literal("Personal Bests").formatted(Formatting.BOLD, Formatting.LIGHT_PURPLE));

                        JsonObject fastestTimes = dungeonsStats.getAsJsonObject(dungeon).getAsJsonObject("fastest_time_s");
                        if (fastestTimes != null && fastestTimes.has(entry.getKey())) {
                            tooltipText.add(Text.literal("S Run:  " + formatTime(fastestTimes.get(entry.getKey()).getAsLong())).formatted(Formatting.GOLD));
                        }

                        fastestTimes = dungeonsStats.getAsJsonObject(dungeon).getAsJsonObject("fastest_time_s_plus");
                        if (fastestTimes != null && fastestTimes.has(entry.getKey())) {
                            tooltipText.add(Text.literal("S+ Run: " + formatTime(fastestTimes.get(entry.getKey()).getAsLong())).formatted(Formatting.GOLD));
                        }

                        fastestTimes = dungeonsStats.getAsJsonObject(dungeon).getAsJsonObject("fastest_time");
                        if (fastestTimes != null && fastestTimes.has(entry.getKey()) && tooltipText.size() == 1) {
                            tooltipText.add(Text.literal("Completion:  " + formatTime(fastestTimes.get(entry.getKey()).getAsLong())).formatted(Formatting.GOLD));
                        }

                        context.drawTooltip(textRenderer, tooltipText, mouseX, mouseY);
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
