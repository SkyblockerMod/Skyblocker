package de.hysky.skyblocker.skyblock.profileviewer.dungeons;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.text.DecimalFormat;

public class DungeonHeaderWidget {
    private LevelFinder.LevelInfo classLevel;
    private float classAvg;

    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static final DecimalFormat DF = new DecimalFormat("#.##");
    private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/dungeons_header.png");

    public DungeonHeaderWidget(JsonObject playerProfile, String[] classes) {
        try {
            JsonObject DUNGEONS_PROFILE = playerProfile.getAsJsonObject("dungeons").getAsJsonObject("dungeon_types").getAsJsonObject("catacombs");
            this.classLevel = LevelFinder.getLevelInfo("Catacombs", DUNGEONS_PROFILE.get("experience").getAsLong());

            float avg = 0;
            JsonObject CLASS_DATA = playerProfile.getAsJsonObject("dungeons").getAsJsonObject("player_classes");
            for (String element : classes) {
                avg += LevelFinder.getLevelInfo("Catacombs", CLASS_DATA.getAsJsonObject(element.toLowerCase()).get("experience").getAsLong()).level;
            }
            classAvg = avg/classes.length;
        }  catch (Exception ignored) {
            this.classLevel = LevelFinder.getLevelInfo("", 0);
            classAvg = 0;
        }
    }

    public void render(DrawContext context, int x, int y) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 109, 26, 109, 26);

        context.drawText(textRenderer, "§i§6§lCatacombs §r" + this.classLevel.level, x + 3, y + 4, Color.WHITE.getRGB(), true);

        context.drawText(textRenderer, "§eClass Average §r" + DF.format(this.classAvg), x + 3, y + 14, Color.WHITE.getRGB(), true);
    }
}
