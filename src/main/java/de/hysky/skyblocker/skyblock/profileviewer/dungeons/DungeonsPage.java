package de.hysky.skyblocker.skyblock.profileviewer.dungeons;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.utils.ProfileUtils;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DungeonsPage implements ProfileViewerPage {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProfileUtils.class);
    private static final String[] CLASSES = {"Healer", "Mage", "Berserk", "Archer", "Tank"};

    private final DungeonHeaderWidget dungeonHeaderWidget;
    private final List<DungeonClassWidget> dungeonClassWidgetsList = new ArrayList<>();
    private final DungeonFloorRunsWidget dungeonFloorRunsWidget;
    private final DungeonMiscStatsWidgets dungeonMiscStatsWidgets;

    public DungeonsPage(JsonObject pProfile) {
        dungeonHeaderWidget = new DungeonHeaderWidget(pProfile, CLASSES);
        dungeonFloorRunsWidget = new DungeonFloorRunsWidget(pProfile);
        dungeonMiscStatsWidgets = new DungeonMiscStatsWidgets(pProfile);
        for (String element : CLASSES) {
            dungeonClassWidgetsList.add(new DungeonClassWidget(element, pProfile));
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, int rootX, int rootY) {
        dungeonHeaderWidget.render(context, rootX, rootY);
        dungeonFloorRunsWidget.render(context, mouseX, mouseY, rootX + 113, rootY + 56);
        dungeonMiscStatsWidgets.render(context, rootX + 113, rootY);
        for (int i = 0; i < dungeonClassWidgetsList.size(); i++) {
            dungeonClassWidgetsList.get(i).render(context, mouseX, mouseY, rootX, rootY + 28 + i * 28);
        }
    }
}
