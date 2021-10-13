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
    private static final Identifier BARS = new Identifier(SkyblockerMod.NAMESPACE,"textures/gui/bars.png");
    private static final Pattern ACTION_BAR_STATUS = Pattern.compile("^§[6c]([0-9]+)/([0-9]+)❤(?:\\+§c[0-9]+\\S)? {3,}(?:§a([0-9]+)§a❈ Defense|(\\S+(?: \\S+)*)) {3,}(?:§b([0-9]+)/([0-9]+)✎ Mana|(\\S+(?: \\S+)*))(.*)$");
    private final Resource health;
    private final Resource mana;
    private int defense;

    public FancyStatusBars() {
        health = new Resource(100, 100);
        mana = new Resource(100, 100);
        defense = 0;
    }

    public boolean update(String actionBar) {
        if(!SkyblockerConfig.get().general.bars.enableBars)
            return false;
        Matcher matcher = ACTION_BAR_STATUS.matcher(actionBar);
        if(!matcher.matches())
            return false;
        health.set(matcher.group(1), matcher.group(2));
        if(matcher.group(3) != null)
            defense = Integer.parseInt(matcher.group(3));
        if(matcher.group(5) != null)
            mana.set(matcher.group(5), matcher.group(6));

        StringBuilder sb = new StringBuilder();
        appendIfNotNull(sb, matcher.group(4));
        appendIfNotNull(sb, matcher.group(7));
        appendIfNotNull(sb, matcher.group(8));

        if(!sb.isEmpty()) {
            assert client.player != null;
            client.player.sendMessage(Text.of(sb.toString()), true);
        }

        return true;
    }

    private void appendIfNotNull(StringBuilder sb, String str) {
        if(str == null)
            return;
        if(!sb.isEmpty())
            sb.append("    ");
        sb.append(str);
    }

    public boolean render(MatrixStack matrices, int scaledWidth, int scaledHeight) {
        if(!SkyblockerConfig.get().general.bars.enableBars)
            return false;
        int left = scaledWidth / 2 - 91;
        int top = scaledHeight - 35;

        int hpFillWidth = (int) (health.getFillLevel() * 33.0F);
        int hpOverflowWidth = (int) (health.getOverflow() * 33.0F);
        int manaFillWidth = (int) (mana.getFillLevel() * 33.0F);
        assert client.player != null;
        int xp = (int) (client.player.experienceProgress * 33.0F);

        // Icons
//        this.client.getTextureManager().bindTexture(BARS);
        RenderSystem.setShaderTexture(0, BARS);
        this.drawTexture(matrices, left, top, 0, 0, 9, 9);
        this.drawTexture(matrices, left + 47, top, 9, 0, 7, 9);
        this.drawTexture(matrices, left + 92, top, 16, 0, 9, 9);
        this.drawTexture(matrices, left + 139, top, 25, 0, 9, 9);

        // Empty Bars
        this.drawTexture(matrices, left + 10, top + 1, 0, 9, 33, 7);
        this.drawTexture(matrices, left + 55, top + 1, 0, 9, 33, 7);
        this.drawTexture(matrices, left + 102, top + 1, 0, 9, 33, 7);
        this.drawTexture(matrices, left + 149, top + 1, 0, 9, 33, 7);

        // Progress Bars
        this.drawTexture(matrices, left + 10, top + 1, 0, 16, hpFillWidth, 7);
        this.drawTexture(matrices, left + 10, top + 1, 0, 44, hpOverflowWidth, 7);
        this.drawTexture(matrices, left + 55, top + 1, 0, 23, manaFillWidth, 7);
        this.drawTexture(matrices, left + 102, top + 1, 0, 30, 33, 7);
        this.drawTexture(matrices, left + 149, top + 1, 0, 37, xp, 7);

        // Progress Texts
        renderText(matrices, health.getValue(), left + 11, top, 16733525);
        renderText(matrices, mana.getValue(), left + 56, top, 5636095);
        renderText(matrices, defense, left + 103, top, 12106180);
        renderText(matrices, client.player.experienceLevel, left + 150, top, 8453920);
        return true;
    }

    private void renderText(MatrixStack matrices, int value, int left, int top, int color) {
        TextRenderer textRenderer = client.textRenderer;
        String text = Integer.toString(value);
        int x = left + (33 - textRenderer.getWidth(text)) / 2;
        int y = top - 3;

        textRenderer.draw(matrices, text, (float) (x + 1), (float) y, 0);
        textRenderer.draw(matrices, text, (float) (x - 1), (float) y, 0);
        textRenderer.draw(matrices, text, (float) x, (float) (y + 1), 0);
        textRenderer.draw(matrices, text, (float) x, (float) (y - 1), 0);
        textRenderer.draw(matrices, text, (float) x, (float) y, color);
    }

    private static class Resource {
        private int value;
        private int max;
        public Resource(int value, int max) {
            this.value = value;
            this.max = max;
        }
        public void set(String value, String max) {
            this.value = Integer.parseInt(value);
            this.max = Integer.parseInt(max);
        }
        public int getValue() {
            return value;
        }
        public double getFillLevel() {
            return Math.min(((double)value)/((double)max),  1);
        }
        public double getOverflow() {
            return Math.max(((double)value)/((double)max) - 1,  0);
        }
    }
}