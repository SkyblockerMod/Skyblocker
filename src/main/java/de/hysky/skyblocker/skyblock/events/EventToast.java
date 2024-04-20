package de.hysky.skyblocker.skyblock.events;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class EventToast implements Toast {
    private static final Identifier TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "notification");

    private final long eventStartTime;

    private final List<OrderedText> message;
    private final ItemStack icon;

    public EventToast(long eventStartTime, String name, ItemStack icon) {
        this.eventStartTime = eventStartTime;
        MutableText formatted = Text.translatable("skyblocker.events.startsSoon", Text.literal(name).formatted(Formatting.YELLOW)).formatted(Formatting.WHITE);
        message = MinecraftClient.getInstance().textRenderer.wrapLines(formatted, 200);
        this.icon = icon;

    }
    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        context.drawGuiTexture(TEXTURE, 0, 0, getWidth(), getHeight());

        long currentTime = System.currentTimeMillis() / 1000;
        int timeTillEvent = (int) (eventStartTime - currentTime);

        int seconds = timeTillEvent % 60;
        int minutes = (timeTillEvent/60) % 60;
        int hours = (timeTillEvent/3600) % 24;

        MutableText time = Text.empty();
        if (hours > 0) {
            time.append(hours + "h").append(" ");
        }
        if (hours > 0 || minutes > 0) {
            time.append(minutes + "m").append(" ");
        }
        time.append(seconds + "s");

        int y = 4;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        for (OrderedText orderedText : message) {
            context.drawText(textRenderer, orderedText, 30, y, Colors.WHITE, false);
            y += textRenderer.fontHeight;
        }
        context.drawText(textRenderer, time, 30, y, Colors.LIGHT_YELLOW, false);

        context.drawItemWithoutEntity(icon, 8, getHeight()/2 - 8);
        return startTime > 5_000 ? Visibility.HIDE: Visibility.SHOW;
    }

    @Override
    public int getWidth() {
        return 200 + 30 + 5;
    }

    @Override
    public int getHeight() {
        return 8 + 9 + message.size()*9;
    }
}
