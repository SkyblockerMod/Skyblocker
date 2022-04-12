package me.xmrvizzy.skyblocker.skyblock;

import com.mojang.blaze3d.systems.RenderSystem;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FancyStatusBars extends DrawableHelper {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Identifier BARS = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/bars.png");
    private static final Pattern ACTION_BAR_MANA = Pattern.compile("§b-\\d+ Mana \\(.*\\) +");
    private static final Pattern ACTION_BAR_STATUS = Pattern.compile("^§[6c](\\d+)/(\\d+)❤(\\+§c\\d+.)?(?: +§a(\\d+)§a❈ Defense)?(?: +(\\S+(?:\\s\\S+)*))??(?: +§b(\\d+)/(\\d+)✎ +(?:Mana|§3(\\d+)ʬ))?(?: +(§[27].*))?$");

    private final Resource[] resources = new Resource[]{
            // Health
            new Resource(16733525),
            // Mana
            new Resource(5636095),
            // Defense
            new Resource(12106180),
            // Experience
            new Resource(8453920),
    };

    public boolean update(String actionBar) {
        if (!SkyblockerConfig.get().general.bars.enableBars) {
            if (SkyblockerConfig.get().messages.hideMana) {
                Matcher mana = ACTION_BAR_MANA.matcher(actionBar);
                if (mana.find()) {
                    assert client.player != null;
                    client.player.sendMessage(Text.of(actionBar.replace(mana.group(), "")), true);
                    return true;
                }
            }
            return false;
        }

        Matcher matcher = ACTION_BAR_STATUS.matcher(actionBar);
        if (!matcher.matches())
            return false;

        resources[0].setMax(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        if (matcher.group(4) != null) {
            int def = Integer.parseInt(matcher.group(4));
            resources[2].setFillLevel(def, (double) def / ((double) def + 100D));
        }
        if (matcher.group(6) != null) {
            int m = Integer.parseInt(matcher.group(6));
            if (matcher.group(8) != null)
                m += Integer.parseInt(matcher.group(8));
            resources[1].setMax(m, Integer.parseInt(matcher.group(7)));
        }
        assert client.player != null;
        resources[3].setFillLevel(client.player.experienceLevel, client.player.experienceProgress);

        StringBuilder sb = new StringBuilder();
        if (matcher.group(3) != null) {
            sb.append("§c").append(matcher.group(3));
        }
        if (SkyblockerConfig.get().messages.hideMana) {
            Matcher mana = ACTION_BAR_MANA.matcher(actionBar);
            if (!mana.find())
                appendIfNotNull(sb, matcher.group(5));
        } else {
            appendIfNotNull(sb, matcher.group(5));
        }
        appendIfNotNull(sb, matcher.group(9));

        if (!sb.isEmpty()) {
            assert client.player != null;
            client.player.sendMessage(Text.of(sb.toString()), true);
        }

        return true;
    }

    private void appendIfNotNull(StringBuilder sb, String str) {
        if (str == null)
            return;
        if (!sb.isEmpty())
            sb.append("    ");
        sb.append(str);
    }

    private static final int BAR_SPACING = 46;

    public boolean render(MatrixStack matrices, int scaledWidth, int scaledHeight) {
        if (!SkyblockerConfig.get().general.bars.enableBars)
            return false;
        int left = scaledWidth / 2 - 91;
        int top = scaledHeight - 35;
        RenderSystem.setShaderTexture(0, BARS);
        for (int i = 0; i < 4; i++) {
            this.drawTexture(matrices, left + i * BAR_SPACING, top, 0, 9 * i, 43, 9);
            int fillCount = resources[i].getFillCount();
            for (int j = 0; j < fillCount; j++) {
                this.drawTexture(matrices, left + 11 + i * BAR_SPACING, top, 43 + 31 * j, 9 * i, Resource.INNER_WIDTH, 9);
            }
            int fillLevel = resources[i].getFillLevel();
            if (0 < fillLevel)
                this.drawTexture(matrices, left + 11 + i * BAR_SPACING, top, 43 + 31 * fillCount, 9 * i, fillLevel, 9);
        }
        for (int i = 0; i < 4; i++) {
            renderText(matrices, resources[i].getValue(), left + 11 + i * BAR_SPACING, top, resources[i].getTextColor());
        }
        return true;
    }

    private void renderText(MatrixStack matrices, int value, int left, int top, int color) {
        TextRenderer textRenderer = client.textRenderer;
        String text = Integer.toString(value);
        int x = left + (33 - textRenderer.getWidth(text)) / 2;
        int y = top - 3;

        // for i in [-1, 1]
        for (int i = -1; i < 2; i += 2) {
            textRenderer.draw(matrices, text, (float) (x + i), (float) y, 0);
            textRenderer.draw(matrices, text, (float) x, (float) (y + i), 0);
        }

        textRenderer.draw(matrices, text, (float) x, (float) y, color);
    }

    private static class Resource {
        static final int INNER_WIDTH = 31;
        private int value;
        private int fillLevel;
        private final int textColor;

        public Resource(int textColor) {
            this.value = 0;
            this.fillLevel = INNER_WIDTH;
            this.textColor = textColor;
        }

        public void setMax(int value, int max) {
            this.value = value;
            this.fillLevel = value * INNER_WIDTH / max;
        }

        public void setFillLevel(int value, double fillLevel) {
            this.value = value;
            this.fillLevel = (int) (INNER_WIDTH * fillLevel);
        }

        public int getValue() {
            return value;
        }

        public int getFillCount() {
            return fillLevel / INNER_WIDTH;
        }

        public int getFillLevel() {
            return fillLevel % INNER_WIDTH;
        }

        public int getTextColor() {
            return textColor;
        }
    }
}