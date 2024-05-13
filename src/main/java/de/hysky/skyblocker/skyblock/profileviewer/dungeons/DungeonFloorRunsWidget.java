package de.hysky.skyblocker.skyblock.profileviewer.dungeons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
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

    // TODO: Hovering on each floor should probably showcase best run times in a tooltip
    public void render(DrawContext context, int x, int y) {
        context.drawTexture(TEXTURE, x, y, 0, 0, 109, 110, 109, 110);
        context.drawText(textRenderer, Text.literal("Floor Runs").formatted(Formatting.BOLD), x + 6, y + 4, Color.WHITE.getRGB(), true);

        int columnX = x + 4;
        int elementY = y + 15;
        for (String dungeon : DUNGEONS) {
            JsonObject dungeonData;
            try {
                dungeonData = dungeonsStats.getAsJsonObject(dungeon).getAsJsonObject(dungeon.equals("catacombs") ? "times_played" : "tier_completions");
                for (Map.Entry<String, JsonElement> entry : dungeonData.entrySet()) {
                    if (entry.getKey().equals("total")) continue;

                    String textToRender = String.format((dungeon.equals("catacombs") ? "§aF" : "§cM") + "%s§r %s", entry.getKey(), entry.getValue().getAsInt());
                    context.drawText(textRenderer, textToRender, columnX + 2, elementY + 2, Color.WHITE.getRGB(), true);

                    elementY += 11;
                }
                columnX += 52;
                elementY = y + 26;
            } catch (Exception e) {
                return;
            }
        }
    }
}
