package de.hysky.skyblocker.skyblock.profileviewer.skills;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SkillsPage implements ProfileViewerPage {
    private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/icon_data_widget.png");
    private static final String[] SKILLS = {"Combat", "Mining", "Farming", "Foraging", "Fishing", "Enchanting", "Alchemy", "Taming", "Carpentry", "Catacombs", "Runecraft", "Social"};
    private static final int ROW_GAP = 28;

    private final JsonObject HYPIXEL_PROFILE;
    private final JsonObject PLAYER_PROFILE;

    private final List<SkillWidget> skillWidgets = new ArrayList<>();
    private JsonObject skills;

    public SkillsPage(JsonObject hProfile, JsonObject pProfile) {
        this.HYPIXEL_PROFILE = hProfile;
        this.PLAYER_PROFILE = pProfile;

        try {
            this.skills = this.PLAYER_PROFILE.getAsJsonObject("player_data").getAsJsonObject("experience");
            for (String skill : SKILLS) {
                skillWidgets.add(new SkillWidget(skill, getSkillXP("SKILL_" + skill.toUpperCase()), getSkillCap(skill)));
            }
        } catch (Exception e) {
            ProfileViewerScreen.LOGGER.error("[Skyblocker Profile Viewer] Error creating widgets.", e);
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, int rootX, int rootY) {
        int column2 = rootX + 113;
        for (int i = 0; i < skillWidgets.size(); i++) {
            int x = (i < 6) ? rootX : column2;
            int y = rootY + (i % 6) * ROW_GAP;
            context.drawTexture(TEXTURE, x, y, 0, 0, 109, 26, 109, 26);
            skillWidgets.get(i).render(context, mouseX, mouseY, x, y + 3);
        }
    }

    private int getSkillCap(String skill) {
        try {
            return switch (skill) {
                case "Farming" -> this.PLAYER_PROFILE.getAsJsonObject("jacobs_contest").getAsJsonObject("perks").get("farming_level_cap").getAsInt();
                default -> -1;
            };
        } catch (Exception e) {
            return 0;
        }
    }

    private long getSkillXP(String skill) {
        try {
            return switch (skill) {
                case "SKILL_CATACOMBS" -> getCatacombsXP();
                case "SKILL_SOCIAL" -> getCoopSocialXP();
                case "SKILL_RUNECRAFT" -> this.skills.get("SKILL_RUNECRAFTING").getAsLong();
                default -> this.skills.get(skill).getAsLong();
            };
        } catch (Exception e) {
            return 0;
        }
    }

    private long getCatacombsXP() {
        try {
            JsonObject dungeonSkills = this.PLAYER_PROFILE.getAsJsonObject("dungeons").getAsJsonObject("dungeon_types");
            return dungeonSkills.getAsJsonObject("catacombs").get("experience").getAsLong();
        } catch (Exception e) {
            return 0;
        }
    }

    private long getCoopSocialXP() {
        long socialXP = 0;
        JsonObject members = HYPIXEL_PROFILE.getAsJsonObject("members");
        for (String memberId : members.keySet()) {
            try {
                socialXP += members.getAsJsonObject(memberId).getAsJsonObject("player_data").getAsJsonObject("experience").get("SKILL_SOCIAL").getAsLong();
            } catch (Exception e) {
                ProfileViewerScreen.LOGGER.warn("[Skyblocker Profile Viewer] Error calculating co-op social xp", e);
            }
        }
        return socialXP;
    }
}
