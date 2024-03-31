package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.fancybars.BarGrid;
import de.hysky.skyblocker.skyblock.fancybars.StatusBar;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FancyStatusBars {
    private static final Identifier BARS = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/bars.png");

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final StatusBarTracker statusBarTracker = SkyblockerMod.getInstance().statusBarTracker;

    private final OldStatusBar[] bars = new OldStatusBar[]{
            new OldStatusBar(0, 16733525, 2, new Color[]{new Color(255, 0, 0), new Color(255, 220, 0)}), // Health Bar
            new OldStatusBar(1, 5636095, 2, new Color[]{new Color(0, 255, 255), new Color(180, 0, 255)}),  // Intelligence Bar
            new OldStatusBar(2, 12106180, 1, new Color[]{new Color(255, 255, 255)}), // Defence Bar
            new OldStatusBar(3, 8453920, 1, new Color[]{new Color(100, 220, 70)}),  // Experience Bar
    };

    // Positions to show the bars
    // 0: Hotbar Layer 1, 1: Hotbar Layer 2, 2: Right of hotbar
    // Anything outside the set values hides the bar
    private final int[] anchorsX = new int[3];
    private final int[] anchorsY = new int[3];

    public static BarGrid barGrid = new BarGrid();
    public static Map<String, StatusBar> statusBars = new HashMap<>();

    static {
        statusBars.put("health", new StatusBar(new Identifier(SkyblockerMod.NAMESPACE, "temp"), new Color[]{new Color(255, 0, 0), new Color(255, 220, 0)}, true, null));
        statusBars.put("intelligence", new StatusBar(new Identifier(SkyblockerMod.NAMESPACE, "temp"), new Color[]{new Color(0, 255, 255), new Color(180, 0, 255)}, true, null));
        statusBars.put("defense", new StatusBar(new Identifier(SkyblockerMod.NAMESPACE, "temp"),  new Color[]{new Color(255, 255, 255)}, false, null));
        statusBars.put("experience", new StatusBar(new Identifier(SkyblockerMod.NAMESPACE, "temp"), new Color[]{new Color(100, 220, 70)}, false, null));
    }

    public FancyStatusBars() {
        moveBar(0, 0);
        moveBar(1, 0);
        moveBar(2, 0);
        moveBar(3, 0);
    }

    private int fill(int value, int max) {
        return (100 * value) / max;
    }

    private static final Identifier BAR_FILL = new Identifier(SkyblockerMod.NAMESPACE, "bars/bar_fill");
    private static final Identifier BAR_BACK = new Identifier(SkyblockerMod.NAMESPACE, "bars/bar_back");
    private static final Supplier<Sprite> SUPPLIER = () -> MinecraftClient.getInstance().getGuiAtlasManager().getSprite(BAR_FILL);

    public boolean render(DrawContext context, int scaledWidth, int scaledHeight) {
        var player = client.player;
        if (!SkyblockerConfigManager.get().general.bars.enableBars || player == null || Utils.isInTheRift())
            return false;
        anchorsX[0] = scaledWidth / 2 - 91;
        anchorsY[0] = scaledHeight - 33;
        anchorsX[1] = anchorsX[0];
        anchorsY[1] = anchorsY[0] - 10;
        anchorsX[2] = (scaledWidth / 2 + 91) + 2;
        anchorsY[2] = scaledHeight - 16;

        bars[0].update(statusBarTracker.getHealth());
        bars[1].update(statusBarTracker.getMana());
        int def = statusBarTracker.getDefense();
        bars[2].fill[0] = fill(def, def + 100);
        bars[2].text = def;
        bars[3].fill[0] = (int) (100 * player.experienceProgress);
        bars[3].text = player.experienceLevel;

        // Update positions of bars from config
        for (int i = 0; i < 4; i++) {
            int configAnchorNum = switch (i) {
                case 0 -> SkyblockerConfigManager.get().general.bars.barPositions.healthBarPosition.toInt();
                case 1 -> SkyblockerConfigManager.get().general.bars.barPositions.manaBarPosition.toInt();
                case 2 -> SkyblockerConfigManager.get().general.bars.barPositions.defenceBarPosition.toInt();
                case 3 -> SkyblockerConfigManager.get().general.bars.barPositions.experienceBarPosition.toInt();
                default -> 0;
            };

            if (bars[i].anchorNum != configAnchorNum)
                moveBar(i, configAnchorNum);
        }

        for (var bar : bars) {
            bar.draw(context);
        }
        for (var bar : bars) {
            bar.drawText(context);
        }
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(50, 50, 0);
        matrices.scale(2,2,1);
        context.drawSprite(0, 0, 0, 60, 5, SUPPLIER.get(), 1, 0.25f, 0.25f, 1);
        matrices.pop();
        return true;
    }

    public void moveBar(int bar, int location) {
        // Set the bar to the new anchor
        bars[bar].anchorNum = location;

        // Count how many bars are in each location
        int layer1Count = 0, layer2Count = 0;
        for (int i = 0; i < 4; i++) {
            switch (bars[i].anchorNum) {
                case 0 -> layer1Count++;
                case 1 -> layer2Count++;
            }
        }

        // Set the bars width and offsetX according to their anchor and how many bars are on that layer
        int adjustedLayer1Count = 0, adjustedLayer2Count = 0, adjustedRightCount = 0;
        for (int i = 0; i < 4; i++) {
            switch (bars[i].anchorNum) {
                case 0 -> {
                    bars[i].bar_width = (172 - ((layer1Count - 1) * 11)) / layer1Count;
                    bars[i].offsetX = adjustedLayer1Count * (bars[i].bar_width + 11 + (layer1Count == 3 ? 0 : 1));
                    adjustedLayer1Count++;
                }
                case 1 -> {
                    bars[i].bar_width = (172 - ((layer2Count - 1) * 11)) / layer2Count;
                    bars[i].offsetX = adjustedLayer2Count * (bars[i].bar_width + 11 + (layer2Count == 3 ? 0 : 1));
                    adjustedLayer2Count++;
                }
                case 2 -> {
                    bars[i].bar_width = 50;
                    bars[i].offsetX = adjustedRightCount * (50 + 11);
                    adjustedRightCount++;
                }
            }
        }
    }

    private class OldStatusBar {
        public final int[] fill;
        private final Color[] colors;
        public int offsetX;
        private final int v;
        private final int text_color;
        public int anchorNum;
        public int bar_width;
        public Object text;

        private OldStatusBar(int i, int textColor, int fillNum, Color[] colors) {
            this.v = i * 9;
            this.text_color = textColor;
            this.fill = new int[fillNum];
            this.fill[0] = 100;
            this.anchorNum = 0;
            this.text = "";
            this.colors = colors;
        }

        public void update(StatusBarTracker.Resource resource) {
            int max = resource.max();
            int val = resource.value();
            this.fill[0] = fill(val, max);
            this.fill[1] = fill(resource.overflow(), max);
            this.text = val;
        }

        public void draw(DrawContext context) {
            // Dont draw if anchorNum is outside of range
            if (anchorNum < 0 || anchorNum > 2) return;

            // Draw the icon for the bar
            context.drawTexture(BARS, anchorsX[anchorNum] + offsetX, anchorsY[anchorNum], 0, v, 9, 9);

            // Draw the background for the bar
            context.drawGuiTexture(BAR_BACK, anchorsX[anchorNum]+ offsetX+10, anchorsY[anchorNum]+1, bar_width, 7);

            // Draw the filled part of the bar
            for (int i = 0; i < fill.length; i++) {
                int fill_width = this.fill[i] * (bar_width - 2) / 100;
                if (fill_width >= 1) {
                    RenderHelper.renderNineSliceColored(context, BAR_FILL, anchorsX[anchorNum] + offsetX + 11, anchorsY[anchorNum]+2, fill_width, 5, colors[i]);
                }
            }
        }

        public void drawText(DrawContext context) {
            // Dont draw if anchorNum is outside of range
            if (anchorNum < 0 || anchorNum > 2) return;

            TextRenderer textRenderer = client.textRenderer;
            String text = this.text.toString();
            int x = anchorsX[anchorNum] + this.offsetX + 11 + (bar_width - textRenderer.getWidth(text)) / 2;
            int y = anchorsY[anchorNum] - 3;

            final int[] offsets = new int[]{-1, 1};
            for (int i : offsets) {
                context.drawText(textRenderer, text, x + i, y, 0, false);
                context.drawText(textRenderer, text, x, y + i, 0, false);
            }
            context.drawText(textRenderer, text, x, y, text_color, false);
        }
    }
}
