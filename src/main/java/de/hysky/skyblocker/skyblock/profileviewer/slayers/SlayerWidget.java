package de.hysky.skyblocker.skyblock.profileviewer.slayers;

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

public class SlayerWidget {
    private final String slayerName;
    private final LevelFinder.LevelInfo slayerLevel;
    private JsonObject slayerData = null;

    private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/icon_data_widget.png");
    private static final Identifier BAR_FILL = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_fill");
    private static final Identifier BAR_BACK = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_back");
    private final Identifier item;
    private final ItemStack drop;
    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static final Map<String, Identifier> HEAD_ICON = Map.ofEntries(
            Map.entry("Zombie", Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/zombie.png")),
            Map.entry("Spider", Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/spider.png")),
            Map.entry("Wolf", Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/wolf.png")),
            Map.entry("Enderman", Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/enderman.png")),
            Map.entry("Vampire", Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/vampire.png")),
            Map.entry("Blaze", Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/blaze.png"))
    );

    private static final Map<String, ItemStack> DROP_ICON = Map.ofEntries(
            Map.entry("Zombie", Ico.FLESH),
            Map.entry("Spider", Ico.STRING),
            Map.entry("Wolf", Ico.MUTTON),
            Map.entry("Enderman", Ico.E_PEARL),
            Map.entry("Vampire", Ico.REDSTONE),
            Map.entry("Blaze", Ico.B_POWDER)
    );

    public SlayerWidget(String slayer, long xp, JsonObject playerProfile) {
        this.slayerName = slayer;
        this.slayerLevel = LevelFinder.getLevelInfo(slayer, xp);
        this.item = HEAD_ICON.get(slayer);
        this.drop = DROP_ICON.getOrDefault(slayer, Ico.BARRIER);
        try {
            this.slayerData = playerProfile.getAsJsonObject("slayer").getAsJsonObject("slayer_bosses").getAsJsonObject(this.slayerName.toLowerCase());
        } catch (Exception ignored) {}
    }

    public void render(DrawContext context, int mouseX, int mouseY, int x, int y) {
        context.drawTexture(TEXTURE, x, y, 0, 0, 109, 26, 109, 26);
        context.drawTexture(this.item, x + 1, y + 3, 0, 0, 20, 20, 20, 20);
        context.drawText(textRenderer, slayerName + " " + slayerLevel.level, x + 31, y + 5, Color.white.hashCode(), false);

        int col2 = x + 113;
        context.drawTexture(TEXTURE, col2, y, 0, 0, 109, 26, 109, 26);
        context.drawItem(this.drop, col2 + 3, y + 5);
        context.drawText(textRenderer, "§aKills: §r" + findTotalKills(), col2 + 30, y + 4, Color.white.hashCode(), true);
        context.drawText(textRenderer, findTopTierKills(), findTopTierKills().equals("No Data") ? col2 + 30 : col2 + 29, y + 15, Color.white.hashCode(), true);

        context.drawGuiTexture(BAR_BACK, x + 30, y + 15, 75, 6);
        Color fillColor = slayerLevel.fill == 1 ? Color.MAGENTA : Color.green;
        RenderHelper.renderNineSliceColored(context, BAR_FILL, x + 30, y + 15, (int) (75 * slayerLevel.fill), 6, fillColor);

        if (mouseX > x + 30 && mouseX < x + 105 && mouseY > y + 12 && mouseY < y + 22){
            List<Text> tooltipText = new ArrayList<>();
            tooltipText.add(Text.literal(this.slayerName).formatted(Formatting.GREEN));
            tooltipText.add(Text.literal("XP: " + ProfileViewerUtils.COMMA_FORMATTER.format(this.slayerLevel.xp)).formatted(Formatting.GOLD));
            context.drawTooltip(textRenderer, tooltipText, mouseX, mouseY);
        }
    }

    private int findTotalKills() {
        try {
            int totalKills = 0;
            for (String key : this.slayerData.keySet()) {
                if (key.startsWith("boss_kills_tier_")) totalKills += this.slayerData.get(key).getAsInt();
            }
            return totalKills;
        } catch (Exception e) {
            return 0;
        }
    }

    private String findTopTierKills() {
        try {
            for (int tier = 4; tier >= 0; tier--) {
                String key = "boss_kills_tier_" + tier;
                if (this.slayerData.has(key)) return "§cT" + (tier + 1) + " Kills: §r" + this.slayerData.get(key).getAsInt();
            }
        } catch (Exception ignored) {}
        return "No Data";
    }
}
