package de.hysky.skyblocker.skyblock.profileviewer.skills;

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

public class SkillWidget {
    private final String SKILL_NAME;
    private final LevelFinder.LevelInfo SKILL_LEVEL;

    private static final Identifier BAR_FILL = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_fill");
    private static final Identifier BAR_BACK = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_back");

    private final ItemStack stack;
    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private static final Map<String, ItemStack> SKILL_LOGO = Map.ofEntries(
            Map.entry("Combat", Ico.STONE_SWORD),
            Map.entry("Farming", Ico.GOLDEN_HOE),
            Map.entry("Mining", Ico.STONE_PICKAXE),
            Map.entry("Foraging", Ico.JUNGLE_SAPLING),
            Map.entry("Fishing", Ico.FISH_ROD),
            Map.entry("Enchanting", Ico.ENCHANTING_TABLE),
            Map.entry("Alchemy", Ico.BREWING_STAND),
            Map.entry("Taming", Ico.SPAWN_EGG),
            Map.entry("Carpentry", Ico.CRAFTING_TABLE),
            Map.entry("Catacombs", ProfileViewerUtils.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzliNTY4OTViOTY1OTg5NmFkNjQ3ZjU4NTk5MjM4YWY1MzJkNDZkYjljMWIwMzg5YjhiYmViNzA5OTlkYWIzM2QifX19")),
            Map.entry("Runecraft", Ico.MAGMA_CREAM),
            Map.entry("Social", Ico.EMERALD)
    );
    private static final Map<String, Integer> SKILL_CAP = Map.ofEntries(
            Map.entry("Combat", 60),
            Map.entry("Farming", 60),
            Map.entry("Mining", 60),
            Map.entry("Foraging", 50),
            Map.entry("Fishing", 50),
            Map.entry("Enchanting", 60),
            Map.entry("Alchemy", 50),
            Map.entry("Taming", 60),
            Map.entry("Carpentry", 50),
            Map.entry("Catacombs", 50),
            Map.entry("Runecraft", 25),
            Map.entry("Social", 25)
    );
    private static final Map<String, Integer> SOFT_SKILL_CAP = Map.of(
            "Taming", 50,
            "Farming", 50
    );

    private static final Map<String, Integer> INFINITE = Map.of(
            "Catacombs", 0
    );

    public SkillWidget(String skill, long xp, int playerCap) {
        this.SKILL_NAME = skill;
        this.SKILL_LEVEL = LevelFinder.getLevelInfo(skill, xp);
        if (SKILL_LEVEL.level >= SKILL_CAP.get(skill) && !INFINITE.containsKey(skill)) {
            SKILL_LEVEL.fill = 1;
            SKILL_LEVEL.level = SKILL_CAP.get(skill);
        }

        this.stack = SKILL_LOGO.getOrDefault(skill, Ico.BARRIER);
        if (playerCap != -1) {
            this.SKILL_LEVEL.level = Math.min(SKILL_LEVEL.level, (SOFT_SKILL_CAP.get(this.SKILL_NAME) + playerCap));
        }

    }

    public void render(DrawContext context, int mouseX, int mouseY, int x, int y) {
        context.drawItem(this.stack, x + 3, y + 2);
        context.drawText(textRenderer, SKILL_NAME + " " + SKILL_LEVEL.level, x + 31, y + 2, Color.white.hashCode(), false);

        Color fillColor = Color.green;
        if (SKILL_LEVEL.level >= SKILL_CAP.get(SKILL_NAME)) {
            fillColor = Color.MAGENTA;
        }

        if ((SOFT_SKILL_CAP.containsKey(SKILL_NAME) && SKILL_LEVEL.level > SOFT_SKILL_CAP.get(SKILL_NAME)) && SKILL_LEVEL.level < SKILL_CAP.get(SKILL_NAME) && SKILL_LEVEL.fill == 1 ||
                (SKILL_NAME.equals("Taming") && SKILL_LEVEL.level >= SOFT_SKILL_CAP.get(SKILL_NAME))) {
            fillColor = Color.YELLOW;
        }

        context.drawGuiTexture(BAR_BACK, x + 30, y + 12, 75, 6);
        RenderHelper.renderNineSliceColored(context, BAR_FILL, x + 30, y + 12, (int) (75 * SKILL_LEVEL.fill), 6, fillColor);

        if (mouseX > x + 30 && mouseX < x + 105 && mouseY > y + 10 && mouseY < y + 19){
            List<Text> tooltipText = new ArrayList<>();
            tooltipText.add(Text.literal(this.SKILL_NAME).formatted(Formatting.GREEN));
            tooltipText.add(Text.literal("XP: " + ProfileViewerUtils.COMMA_FORMATTER.format(this.SKILL_LEVEL.xp)).formatted(Formatting.GOLD));
            context.drawTooltip(textRenderer, tooltipText, mouseX, mouseY);
        }
    }
}