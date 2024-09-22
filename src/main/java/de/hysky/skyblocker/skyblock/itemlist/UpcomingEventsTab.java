package de.hysky.skyblocker.skyblock.itemlist;

import de.hysky.skyblocker.mixins.accessors.DrawContextInvoker;
import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.skyblock.tabhud.widget.JacobsContestWidget;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class UpcomingEventsTab extends ItemListWidget.TabContainerWidget {
    private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);
    private final MinecraftClient client;
    private final List<EventRenderer> events;

    public UpcomingEventsTab(int x, int y, MinecraftClient client) {
        super(x, y, Text.literal("Upcoming Events Tab"));
        this.client = client;
        events = EventNotifications.getEvents().entrySet()
                .stream()
                .sorted(Comparator.comparingLong(a -> a.getValue().isEmpty() ? Long.MAX_VALUE : a.getValue().peekFirst().start()))
                .map(stringLinkedListEntry -> new EventRenderer(stringLinkedListEntry.getKey(), stringLinkedListEntry.getValue()))
                .toList();
    }

    @Override
    public void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        if (hovered != null) {
            ((DrawContextInvoker) context).invokeDrawTooltip(this.client.textRenderer, hovered.getTooltip(), mouseX, mouseY, HoveredTooltipPositioner.INSTANCE);
        }
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }

    private EventRenderer hovered = null;

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();
        context.enableScissor(x, y, getRight(), getBottom());
        context.drawItem(CLOCK, x, y + 4);
        context.drawText(this.client.textRenderer, "Upcoming Events", x + 17, y + 7, -1, true);

        int eventsY = y + 7 + 24;
        hovered = null;
        for (EventRenderer eventRenderer : events) {
            eventRenderer.render(context, x + 1, eventsY, mouseX, mouseY);
            if (isMouseOver(mouseX, mouseY) && eventRenderer.isMouseOver(mouseX, mouseY, x+1, eventsY)) hovered = eventRenderer;
            eventsY += eventRenderer.getHeight();

        }
        context.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered != null && hovered.getWarpCommand() != null) {
            MessageScheduler.INSTANCE.sendMessageAfterCooldown(hovered.getWarpCommand());
            return true;
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    public static class EventRenderer {

        private final LinkedList<EventNotifications.SkyblockEvent> events;
        private final String eventName;

        public EventRenderer(String eventName, LinkedList<EventNotifications.SkyblockEvent> events) {
            this.events = events;
            this.eventName = eventName;
        }

        public void render(DrawContext context, int x, int y, int mouseX, int mouseY) {
            long time = System.currentTimeMillis() / 1000;
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.drawText(textRenderer, Text.literal(eventName).fillStyle(Style.EMPTY.withUnderline(isMouseOver(mouseX, mouseY, x, y))), x, y, -1, true);
            if (events.isEmpty()) {
                context.drawText(textRenderer, Text.literal(" ").append(Text.translatable("skyblocker.events.tab.noMore")), x, y + textRenderer.fontHeight, Colors.GRAY, false);
            } else if (events.peekFirst().start() > time) {
                MutableText formatted = Text.literal(" ").append(Text.translatable("skyblocker.events.tab.startsIn", SkyblockTime.formatTime((int) (events.peekFirst().start() - time)))).formatted(Formatting.YELLOW);
                context.drawText(textRenderer, formatted, x, y + textRenderer.fontHeight, -1, true);
            } else {
                MutableText formatted = Text.literal(" ").append(Text.translatable( "skyblocker.events.tab.endsIn", SkyblockTime.formatTime((int) (events.peekFirst().start() + events.peekFirst().duration() - time)))).formatted(Formatting.GREEN);
                context.drawText(textRenderer, formatted, x, y + textRenderer.fontHeight, -1, true);
            }

        }

        public int getHeight() {
            return 20;
        }

        public boolean isMouseOver(int mouseX, int mouseY, int x, int y) {
            return mouseX >= x && mouseX <= x + 131 && mouseY >= y && mouseY <= y+getHeight();
        }

        public List<TooltipComponent> getTooltip() {
            List<TooltipComponent> components = new ArrayList<>();
            if (events.peekFirst() == null) return components;
            if (eventName.equals(EventNotifications.JACOBS)) {
                components.add(new JacobsTooltip(events.peekFirst().extras()));
            }
            //noinspection DataFlowIssue
            if (events.peekFirst().warpCommand() != null) {
                components.add(TooltipComponent.of(Text.translatable("skyblocker.events.tab.clickToWarp").formatted(Formatting.ITALIC).asOrderedText()));
            }

            return components;
        }

        public @Nullable String getWarpCommand() {
            if (events.isEmpty()) return null;
            return events.peek().warpCommand();
        }
    }

    private record JacobsTooltip(String[] crops) implements TooltipComponent {

        private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);

        @Override
        public int getHeight() {
            return 20;
        }

        @Override
        public int getWidth(TextRenderer textRenderer) {
            return 16 * 3 + 4;
        }

        @Override
        public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
            for (int i = 0; i < crops.length; i++) {
                String crop = crops[i];
                context.drawItem(JacobsContestWidget.FARM_DATA.getOrDefault(crop, BARRIER), x + 18 * i, y + 2);
            }
        }

    }
}
