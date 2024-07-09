package de.hysky.skyblocker.skyblock.profileviewer.slayers;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class SlayersPage implements ProfileViewerPage {
    private static final String[] SLAYERS = {"Zombie", "Spider", "Wolf", "Enderman", "Vampire", "Blaze"};
    private static final int ROW_GAP = 28;

    private final List<SlayerWidget> slayerWidgets = new ArrayList<>();

    public SlayersPage(JsonObject pProfile) {
        try {
            for (String slayer : SLAYERS) {
                slayerWidgets.add(new SlayerWidget(slayer, getSlayerXP(slayer.toLowerCase(), pProfile), pProfile));
            }
        } catch (Exception e) {
            ProfileViewerScreen.LOGGER.error("[Skyblocker Profile Viewer] Error creating slayer widgets", e);
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, int rootX, int rootY) {
        for (int i = 0; i < slayerWidgets.size(); i++) {
            slayerWidgets.get(i).render(context, mouseX, mouseY, rootX, rootY + i * ROW_GAP);
        }
    }

    private long getSlayerXP(String slayer, JsonObject pProfile) {
        try {
            return pProfile.getAsJsonObject("slayer").getAsJsonObject("slayer_bosses")
                    .getAsJsonObject(slayer).get("xp").getAsLong();
        } catch (Exception e) {
            return 0;
        }
    }
}
