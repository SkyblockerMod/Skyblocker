package de.hysky.skyblocker.skyblock.tabhud.config.entries;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsOrderingTab;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.List;

public class WidgetEntry extends WidgetsListEntry {

    private static final Text ENABLED = Text.literal("ENABLED").formatted(Formatting.GREEN);
    private static final Text DISABLED = Text.literal("DISABLED").formatted(Formatting.RED);

    private final ButtonWidget editButton;
    private final State state;
    private final ButtonWidget enableButton;

    public WidgetEntry(WidgetsOrderingTab parent, int slotId, ItemStack icon) {
        super(parent, slotId, icon);
        editButton = ButtonWidget.builder(Text.literal("EDIT"), button -> this.parent.clickAndWaitForServer(this.slotId, 1))
                .size(32, 12)
                .build();

        String string = icon.getName().getString().trim();
        if (string.startsWith("✔")) {
            state = State.ENABLED;
        } else if (string.startsWith("✖")) {
            state = State.DISABLED;
        } else state = State.LOCKED;
        enableButton = ButtonWidget.builder(state.equals(State.ENABLED) ? ENABLED : DISABLED, button -> this.parent.clickAndWaitForServer(this.slotId, 0))
                .size(64, 12)
                .build();

    }


    @Override
    public void renderTooltip(DrawContext context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {
        if (mouseX >= x && mouseX <= x + entryWidth - 50 && mouseY >= y && mouseY <= y + entryHeight) {
            List<Text> lore = ItemUtils.getLore(icon);
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, state == State.LOCKED ? lore : lore.subList(0, lore.size() - 3), mouseX, mouseY);
        }
    }

    @Override
    public List<? extends Element> children() {
        return List.of(editButton, enableButton);
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int textY = y + (entryHeight - 9) / 2;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        renderIconAndText(context, y, x, entryHeight);
        if (state != State.LOCKED) {
            editButton.setPosition(x + entryWidth - 40, y + (entryHeight - 12) / 2);
            editButton.render(context, mouseX, mouseY, tickDelta);

            enableButton.setPosition(x + entryWidth - 110, y + (entryHeight - 12) / 2);
            enableButton.render(context, mouseX, mouseY, tickDelta);
        } else {
            context.drawText(textRenderer, "LOCKED", x + entryWidth - 50, textY, Colors.RED, true);
        }
    }

    enum State {
        ENABLED,
        DISABLED,
        LOCKED
    }
}
