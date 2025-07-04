package de.hysky.skyblocker.skyblock.profileviewer;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;

public class ProfileViewerTextWidget {
    private static final int ROW_GAP = 9;

    private String PROFILE_NAME = "UNKNOWN";
    private int SKYBLOCK_LEVEL = 0;
    private double PURSE = 0;
    private double BANK = 0;

    public ProfileViewerTextWidget(JsonObject hypixelProfile, JsonObject playerProfile){
        try {
            this.PROFILE_NAME = hypixelProfile.get("cute_name").getAsString();
            this.SKYBLOCK_LEVEL = playerProfile.getAsJsonObject("leveling").get("experience").getAsInt() / 100;
            this.PURSE = playerProfile.getAsJsonObject("currencies").get("coin_purse").getAsDouble();
            this.BANK = hypixelProfile.getAsJsonObject("banking").get("balance").getAsDouble();
        } catch (Exception ignored) {}
    }

    public void render(DrawContext context, TextRenderer textRenderer, int root_x, int root_y){
        // Profile Icon
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.scale(0.75f, 0.75f, 1);
        int rootAdjustedX = (int) ((root_x) / 0.75f);
        int rootAdjustedY = (int) ((root_y) / 0.75f);
        context.drawItem(Ico.PAINTING, rootAdjustedX, rootAdjustedY);
        matrices.pop();

        context.drawText(textRenderer, "§n"+PROFILE_NAME, root_x + 14, root_y + 3, Colors.WHITE, true);
        context.drawText(textRenderer, "§aLevel:§r " + SKYBLOCK_LEVEL, root_x + 2, root_y + 6 + ROW_GAP, Colors.WHITE, true);
        context.drawText(textRenderer, "§6Purse:§r " + ProfileViewerUtils.numLetterFormat(PURSE), root_x + 2, root_y + 6 + ROW_GAP * 2, Colors.WHITE, true);
        context.drawText(textRenderer, "§6Bank:§r " + ProfileViewerUtils.numLetterFormat(BANK), root_x + 2, root_y + 6 + ROW_GAP * 3, Colors.WHITE, true);
        context.drawText(textRenderer, "§6NW:§r " + "Soon™", root_x + 2, root_y + 6 + ROW_GAP * 4, Colors.WHITE, true);
    }
}
