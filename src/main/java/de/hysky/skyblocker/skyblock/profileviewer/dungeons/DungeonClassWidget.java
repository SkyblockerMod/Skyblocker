package de.hysky.skyblocker.skyblock.profileviewer.dungeons;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DungeonClassWidget {
    private final String className;
    private LevelFinder.LevelInfo classLevel;
    private static final int CLASS_CAP = 50;
    private JsonObject classData;
    private final ItemStack stack;
    private boolean active = false;

    private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/icon_data_widget.png");
    private static final Identifier ACTIVE_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/item_protection.png");
    private static final Identifier BAR_FILL = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_fill");
    private static final Identifier BAR_BACK = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_back");

    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static final Map<String, ItemStack> CLASS_ICON = Map.ofEntries(
            Map.entry("Healer", Ico.S_POTION),
            Map.entry("Mage", Ico.B_ROD),
            Map.entry("Berserk", Ico.IRON_SWORD),
            Map.entry("Archer", Ico.BOW),
            Map.entry("Tank", Ico.CHESTPLATE)
    );

    public DungeonClassWidget(String className, JsonObject playerProfile) {
        this.className = className;
        stack = CLASS_ICON.getOrDefault(className, Ico.BARRIER);
        try {
            classData = playerProfile.getAsJsonObject("dungeons").getAsJsonObject("player_classes").getAsJsonObject(this.className.toLowerCase());
            classLevel = LevelFinder.getLevelInfo("Catacombs", classData.get("experience").getAsLong());
            active = playerProfile.getAsJsonObject("dungeons").get("selected_dungeon_class").getAsString().equals(className.toLowerCase());
        } catch (Exception ignored) {
            classLevel = LevelFinder.getLevelInfo("", 0);
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, int x, int y) {
        context.drawTexture(TEXTURE, x, y, 0, 0, 109, 26, 109, 26);
        context.drawItem(stack, x + 3, y + 5);
        if (active) context.drawTexture(ACTIVE_TEXTURE, x + 3, y + 5, 0, 0, 16, 16, 16, 16);

        context.drawText(textRenderer, className + " " + classLevel.level, x + 31, y + 5, Color.WHITE.getRGB(), false);
        Color fillColor = classLevel.level >= CLASS_CAP ? Color.MAGENTA : Color.GREEN;
        context.drawGuiTexture(BAR_BACK, x + 30, y + 15, 75, 6);
        RenderHelper.renderNineSliceColored(context, BAR_FILL, x + 30, y + 15, (int) (75 * classLevel.fill), 6, fillColor);

        if (mouseX > x + 30 && mouseX < x + 105 && mouseY > y + 12 && mouseY < y + 22){
            List<Text> tooltipText = new ArrayList<>();
            tooltipText.add(Text.literal(this.className).formatted(Formatting.GREEN));
            tooltipText.add(Text.literal("XP: " + ProfileViewerUtils.COMMA_FORMATTER.format(this.classLevel.xp)).formatted(Formatting.GOLD));
            context.drawTooltip(textRenderer, tooltipText, mouseX, mouseY);
        }
    }
}
