package me.xmrvizzy.skyblocker.skyblock;

import com.mojang.blaze3d.systems.RenderSystem;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class FancyStatusBars extends DrawableHelper {
    private static final Identifier BARS = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/bars.png");

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final StatusBarTracker statusBarTracker = SkyblockerMod.getInstance().statusBarTracker;

    private final StatusBar[] bars = new StatusBar[]{
            new StatusBar(0, 16733525, 2), // Health Bar
            new StatusBar(1, 5636095, 2),  // Intelligence Bar
            new StatusBar(2, 12106180, 1), // Defence Bar
            new StatusBar(3, 8453920, 1),  // Experience Bar
    };

    // Positions to show the bars
    // 0: Hotbar Layer 1, 1: Hotbar Layer 2, 2: Right of hotbar
    // Anything outside the set values hides the bar
    private final int[] anchorsX = new int[3];
    private final int[] anchorsY = new int[3];

    public FancyStatusBars() {
        moveBar(0, 0);
        moveBar(1, 0);
        moveBar(2, 0);
        moveBar(3, 0);
    }

    private int fill(int value, int max) {
        return (100 * value) / max;
    }

    public boolean render(MatrixStack matrices, int scaledWidth, int scaledHeight) {
        var player = client.player;
        if (!SkyblockerConfig.get().general.bars.enableBars || player == null)
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
        bars[3].fill[0] = (int) (32 * player.experienceProgress);
        bars[3].text = player.experienceLevel;

        // Update positions of bars from config
        for (int i = 0; i < 4; i++) {
            int configAnchorNum = switch (i) {
                case 0 -> SkyblockerConfig.get().general.bars.barpositions.healthBarPosition.toInt();
                case 1 -> SkyblockerConfig.get().general.bars.barpositions.manaBarPosition.toInt();
                case 2 -> SkyblockerConfig.get().general.bars.barpositions.defenceBarPosition.toInt();
                case 3 -> SkyblockerConfig.get().general.bars.barpositions.experienceBarPosition.toInt();
                default -> 0;
            };

            if (bars[i].anchorNum != configAnchorNum)
                moveBar(i, configAnchorNum);
        }

        RenderSystem.setShaderTexture(0, BARS);
        for (var bar : bars)
            bar.draw(matrices);
        for (var bar : bars)
            bar.drawText(matrices);
        return true;
    }

    public void moveBar(int bar, int location) {
        // Set the bar to the new anchor
        bars[bar].anchorNum = location;

        // Count how many bars are in each location
        int layer1Count = 0, layer2Count = 0, rightCount = 0;
        for (int i = 0; i < 4; i++) {
            switch (bars[i].anchorNum) {
                case 0 -> layer1Count++;
                case 1 -> layer2Count++;
                case 2 -> rightCount++;
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

    private class StatusBar {
        public final int[] fill;
        public int offsetX;
        private final int v;
        private final int text_color;
        public int anchorNum;
        public int bar_width;
        public Object text;

        private StatusBar(int i, int textColor, int fillNum) {
            this.v = i * 9;
            this.text_color = textColor;
            this.fill = new int[fillNum];
            this.fill[0] = 100;
            this.anchorNum = 0;
            this.text = "";
        }

        public void update(StatusBarTracker.Resource resource) {
            int max = resource.max();
            int val = resource.value();
            this.fill[0] = fill(val, max);
            this.fill[1] = fill(resource.overflow(), max);
            this.text = val;
        }

        public void draw(MatrixStack matrices) {
            // Dont draw if anchorNum is outside of range
            if (anchorNum < 0 || anchorNum > 2) return;

            // Draw the icon for the bar
            drawTexture(matrices, anchorsX[anchorNum] + offsetX, anchorsY[anchorNum], 0, v, 9, 9);

            // Draw the background for the bar
            drawTexture(matrices, anchorsX[anchorNum] + offsetX + 10, anchorsY[anchorNum], 10, v, 2, 9);
            for (int i = 2; i < bar_width - 2; i++)
                drawTexture(matrices, anchorsX[anchorNum] + offsetX + 10 + i, anchorsY[anchorNum], 12, v, 1, 9);
            drawTexture(matrices, anchorsX[anchorNum] + offsetX + 10 + bar_width - 2, anchorsY[anchorNum], 41, v, 2, 9);

            // Draw the filled part of the bar
            for (int i = 0; i < fill.length; i++) {
                for (int j = (bar_width - 3); j >= 0; j--) {
                    if ( Math.max((j * 100)/(bar_width - 3), 1) > fill[i]) continue;
                    drawTexture(matrices, anchorsX[anchorNum] + offsetX + 11 + j, anchorsY[anchorNum], ((j == 0 || j == bar_width - 3) ? 43 : 44)  + i * 31, v, 1, 9);
                }
            }
        }

        public void drawText(MatrixStack matrices) {
            // Dont draw if anchorNum is outside of range
            if (anchorNum < 0 || anchorNum > 2) return;

            TextRenderer textRenderer = client.textRenderer;
            String text = this.text.toString();
            int x = anchorsX[anchorNum] + this.offsetX + 11 + (bar_width - textRenderer.getWidth(text)) / 2;
            int y = anchorsY[anchorNum] - 3;

            final int[] offsets = new int[]{-1, 1};
            for (int i : offsets) {
                textRenderer.draw(matrices, text, (float) (x + i), (float) y, 0);
                textRenderer.draw(matrices, text, (float) x, (float) (y + i), 0);
            }
            textRenderer.draw(matrices, text, (float) x, (float) y, text_color);
        }
    }
}
