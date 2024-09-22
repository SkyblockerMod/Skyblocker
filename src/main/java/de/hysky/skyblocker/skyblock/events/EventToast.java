package de.hysky.skyblocker.skyblock.events;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.SkyblockTime;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class EventToast implements Toast {
    protected static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "notification");

    private final long eventStartTime;

    protected final List<OrderedText> message;
    protected final List<OrderedText> messageNow;
    protected final int messageWidth;
    protected final int messageNowWidth;
    protected final ItemStack icon;

    protected boolean started;

    public EventToast(long eventStartTime, String name, ItemStack icon) {
        this.eventStartTime = eventStartTime;
        MutableText formatted = Text.translatable("skyblocker.events.startsSoon", Text.literal(name).formatted(Formatting.YELLOW)).formatted(Formatting.WHITE);
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        message = renderer.wrapLines(formatted, 150);
        messageWidth = message.stream().mapToInt(renderer::getWidth).max().orElse(150);

        MutableText formattedNow = Text.translatable("skyblocker.events.startsNow", Text.literal(name).formatted(Formatting.YELLOW)).formatted(Formatting.WHITE);
        messageNow = renderer.wrapLines(formattedNow, 150);
        messageNowWidth = messageNow.stream().mapToInt(renderer::getWidth).max().orElse(150);
        this.icon = icon;
        this.started = eventStartTime - System.currentTimeMillis() / 1000 < 0;

    }
    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        context.drawGuiTexture(TEXTURE, 0, 0, getWidth(), getHeight());

        int y = (getHeight() - getInnerContentsHeight())/2;
        y = 2 + drawMessage(context, 30, y, Colors.WHITE);
        drawTimer(context, 30, y);

        context.drawItemWithoutEntity(icon, 8, getHeight()/2 - 8);
        return startTime > 5_000 ? Visibility.HIDE: Visibility.SHOW;
    }

    protected int drawMessage(DrawContext context, int x, int y, int color) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        for (OrderedText orderedText : started ? messageNow: message) {
            context.drawText(textRenderer, orderedText, x, y, color, false);
            y += textRenderer.fontHeight;
        }
        return y;
    }

    protected void drawTimer(DrawContext context, int x, int y) {
        long currentTime = System.currentTimeMillis() / 1000;
        int timeTillEvent = (int) (eventStartTime - currentTime);
        started = timeTillEvent < 0;
        if (started) return;

        Text time = SkyblockTime.formatTime(timeTillEvent);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawText(textRenderer, time, x, y, Colors.LIGHT_YELLOW, false);
    }

    @Override
    public int getWidth() {
        return (started ? messageNowWidth: messageWidth) + 30 + 6;
    }

    protected int getInnerContentsHeight() {
        return message.size() * 9 + (started ? 0 : 9);
    }

    @Override
    public int getHeight() {
        return Math.max(getInnerContentsHeight() + 12 + 2, 32);
    }
}
