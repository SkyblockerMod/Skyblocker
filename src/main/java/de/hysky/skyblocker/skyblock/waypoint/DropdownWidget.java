package de.hysky.skyblocker.skyblock.waypoint;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class DropdownWidget<T> extends ElementListWidget<DropdownWidget.Entry<T>> {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static final int ENTRY_HEIGHT = 15;
    protected final List<T> entries;
    protected final Consumer<T> selectCallback;
    protected T prevSelected;
    protected T selected;
    protected boolean open;

    public DropdownWidget(MinecraftClient minecraftClient, int x, int y, int width, int maxHeight, List<T> entries, Consumer<T> selectCallback, T selected) {
        super(minecraftClient, width, Math.min((entries.size() + 1) * ENTRY_HEIGHT + 8, maxHeight), y, ENTRY_HEIGHT);
        setX(x);
        this.entries = entries;
        this.selectCallback = selectCallback;
        this.selected = selected;
        setRenderHeader(true, ENTRY_HEIGHT + 4);
        for (T entry : entries) {
            addEntry(new Entry<>(this, entry));
        }
    }

    @Override
    public int getRowLeft() {
        return getX();
    }

    @Override
    public int getRowWidth() {
        return getWidth();
    }

    @Override
    protected boolean clickedHeader(int x, int y) {
        open = !open;
        return true;
    }

    @Override
    protected void renderHeader(DrawContext context, int x, int y) {
        context.drawTextWithShadow(client.textRenderer, selected.toString(), x + 4, y + 2, 0xFFFFFFFF);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 100);

        int y = getY() - (int) getScrollAmount();
        int height = getMaxPosition();

        context.fill(getX(), y, getX() + width, y + headerHeight, 0xFF000000);
        context.drawHorizontalLine(getX(), getX() + width, y, 0xFFFFFFFF);
        context.drawHorizontalLine(getX(), getX() + width, y + headerHeight, 0xFFFFFFFF);
        context.drawVerticalLine(getX(), y, y + headerHeight, 0xFFFFFFFF);
        context.drawVerticalLine(getX() + width, y, y + headerHeight, 0xFFFFFFFF);

        if (open) {
            context.fill(getX(), y + headerHeight + 1, getX() + width, y + height, 0xFF000000);
            context.drawHorizontalLine(getX(), getX() + width, y + height, 0xFFFFFFFF);
            context.drawVerticalLine(getX(), y + headerHeight, y + height, 0xFFFFFFFF);
            context.drawVerticalLine(getX() + width, y + headerHeight, y + height, 0xFFFFFFFF);

            super.renderWidget(context, mouseX, mouseY, delta);
        } else {
            renderHeader(context, getRowLeft(), y + 4);
        }

        context.getMatrices().pop();
    }

    @Override
    protected void drawMenuListBackground(DrawContext context) {}

    @Override
    protected void drawHeaderAndFooterSeparators(DrawContext context) {}

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return open && super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    protected void select(T entry) {
        selected = entry;
        open = false;
        setScrollAmount(0);
        if (selected != prevSelected) {
            selectCallback.accept(entry);
            prevSelected = selected;
        }
    }

    static class Entry<T> extends ElementListWidget.Entry<Entry<T>> {
        private final DropdownWidget<T> dropdownWidget;
        private final T entry;

        public Entry(DropdownWidget<T> dropdownWidget, T entry) {
            this.dropdownWidget = dropdownWidget;
            this.entry = entry;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(client.textRenderer, Text.literal(entry.toString()).fillStyle(Style.EMPTY.withUnderline(hovered)), x + 14, y + 2, 0xFFFFFFFF);
            if (dropdownWidget.selected == this.entry) {
                context.drawTextWithShadow(client.textRenderer, "✔", x + 4, y + 2, 0xFFFFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && dropdownWidget.open) {
                dropdownWidget.select(entry);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
