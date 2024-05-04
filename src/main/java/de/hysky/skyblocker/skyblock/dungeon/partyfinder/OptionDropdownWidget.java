package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OptionDropdownWidget extends ElementListWidget<OptionDropdownWidget.Option> {
    private final int slotId;
    private int backButtonId = -1;
    private final Text name;
    private @Nullable Option selectedOption;
    protected final PartyFinderScreen screen;
    private boolean isOpen = false;

    private float animationProgress = 0f;

    public OptionDropdownWidget(PartyFinderScreen screen, Text name, @Nullable Option selectedOption, int x, int y, int width, int height, int slotId) {
        super(screen.getClient(), width, height, y, 15);
        this.screen = screen;
        this.slotId = slotId;
        setX(x);
        setRenderHeader(true, 25);
        this.name = name;
        this.selectedOption = selectedOption;
    }

    @Override
    protected boolean clickedHeader(int x, int y) {
        if (!(x >= 0 && y >= 10 && x < getWidth() && y < 26)) return false;
        if (screen.isWaitingForServer()) return false;
        if (isOpen) {
            if (backButtonId != -1) screen.clickAndWaitForServer(backButtonId);
        } else {
            screen.clickAndWaitForServer(slotId);
            screen.partyFinderButton.active = false;
        }
        animationProgress = 0f;
        return true;
    }

    @Override
    public int getRowLeft() {
        return getX() + 2;
    }

    @Override
    protected int getScrollbarX() {
        return getRowLeft() + getRowWidth();
    }

    @Override
    public int getRowWidth() {
        return getWidth() - 6;
    }

    public void setSelectedOption(@NotNull OptionDropdownWidget.Option entry) {
        selectedOption = entry;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!screen.getSettingsContainer().canInteract(this)) return false;
        if (isOpen && !isMouseOver(mouseX, mouseY) && backButtonId != -1) {
            screen.clickAndWaitForServer(backButtonId);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (screen.getSettingsContainer().canInteract(this)) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return false;
    }

    @Override
    protected void renderHeader(DrawContext context, int x, int y) {
        context.drawText(MinecraftClient.getInstance().textRenderer, name, x, y + 1, 0xFFD0D0D0, false);
        int offset = 10;
        context.fill(x - 2, y + offset, x - 3 + getWidth(), y + 15 + offset, 0xFFF0F0F0);
        context.fill(x - 1, y + 1 + offset, x - 3 + getWidth() - 1, y + 14 + offset, 0xFF000000);
        if (selectedOption != null) {
            context.drawText(MinecraftClient.getInstance().textRenderer, selectedOption.message, x + 2, y + 3 + offset, 0xFFFFFFFF, true);
        }
        else context.drawText(MinecraftClient.getInstance().textRenderer, "???", x + 2, y + 3 + offset, 0xFFFFFFFF, true);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        if (isOpen) {
            matrices.push();
            matrices.translate(0, 0, 100);
        }
        if (animationProgress < 1) animationProgress += delta * 0.5f;
        else if (animationProgress != 1) animationProgress = 1;
        if (PartyFinderScreen.DEBUG) {
            context.drawText(MinecraftClient.getInstance().textRenderer, String.valueOf(slotId), getX(), getY() - 10, 0xFFFF0000, true);
            context.drawText(MinecraftClient.getInstance().textRenderer, String.valueOf(backButtonId), getX() + 50, getY() - 10, 0xFFFF0000, true);
        }

        int height1 = Math.min(getHeight(), getEntryCount() * itemHeight + 4);
        int idk = isOpen ? (int) (height1 * animationProgress) : (int) (height1 * (1 - animationProgress));
        context.fill(getX(), getY() + headerHeight, getX() + getWidth() - 1, getY() + idk + headerHeight, 0xFFE0E0E0);
        context.fill(getX() + 1, getY() + headerHeight + 1, getX() + getWidth() - 2, getY() + idk + headerHeight - 1, 0xFF000000);

        super.renderWidget(context, mouseX, mouseY, delta);
        if (isOpen) {
            matrices.pop();
        }
    }
    
    @Override
    protected void drawHeaderAndFooterSeparators(DrawContext context) {
    }

    @Override
    protected void drawMenuListBackground(DrawContext context) {
    }

    public void open(List<Option> entries, int backButtonId) {
        isOpen = true;
        this.replaceEntries(entries);
        animationProgress = 0f;
        this.backButtonId = backButtonId;
    }

    public void close() {
        isOpen = false;
        this.clearEntries();

    }

    public class Option extends ElementListWidget.Entry<Option> {

        private final String message;
        private final ItemStack icon;
        private final int optionSlotId;

        public Option(@NotNull String message, @Nullable ItemStack icon, int slotId) {

            this.message = message;
            this.icon = icon;
            this.optionSlotId = slotId;
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
            /*if (hovered) {
                context.fill(x, y, x + entryWidth, y + 13, 0xFFF0F0F0);
                context.fill(x+1, y+1, x + entryWidth-1, y + 12, 0xFF000000);
            } else context.fill(x, y, x + entryWidth, y + 13, 0xFF000000);*/
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            int iconY = y + 1;
            matrices.translate(x, iconY, 0);
            matrices.scale(0.8f, 0.8f, 1f);
            matrices.translate(-x, -iconY, 0);
            context.drawItem(icon, x, iconY);
            matrices.pop();
            if (PartyFinderScreen.DEBUG) context.drawText(MinecraftClient.getInstance().textRenderer, String.valueOf(optionSlotId), x + 8, y, 0xFFFF0000, true);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(message).fillStyle(Style.EMPTY.withUnderline(hovered)), x + 14, y + 3, 0xFFFFFFFF, false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Option that = (Option) o;

            return message.equals(that.message);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (screen.isWaitingForServer()) return false;
            if (button == 0) {
                screen.clickAndWaitForServer(this.optionSlotId);
                setSelectedOption(this);
            }
            return true;
        }

        @Override
        public int hashCode() {
            return message.hashCode();
        }
    }
}
