package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.List;

public class OptionDropdownWidget extends ElementListWidget<OptionDropdownWidget.Option> {
    private final int slotId;
    private int backButtonId = -1;
    private final Text name;
    private @Nullable Option selectedOption;
    protected final PartyFinderScreen screen;
    private boolean isOpen = false;
	private final int maxHeight;
	private static final int CLOSED_HEIGHT = 35;

    private float animationProgress = 0f;

    public OptionDropdownWidget(PartyFinderScreen screen, Text name, @Nullable Option selectedOption, int x, int y, int width, int maxHeight, int slotId) {
        super(screen.getClient(), width, CLOSED_HEIGHT, y, 15, 25);
		this.maxHeight = maxHeight;
        this.screen = screen;
        this.slotId = slotId;
        setX(x);
        this.name = name;
        this.selectedOption = selectedOption;
    }

    private boolean clickedHeader(int x, int y) {
        if (!(x >= 0 && y >= 10 && x < getWidth() && y < 26)) return false;
        if (screen.isWaitingForServer()) return false;
        if (isOpen) {
            if (backButtonId != -1) screen.clickAndWaitForServer(backButtonId);
        } else {
			animationProgress = 0f;
            screen.clickAndWaitForServer(slotId);
            screen.partyFinderButton.active = false;
        }
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
        if (isOpen) {
			if (!isMouseOver(mouseX, mouseY) && backButtonId != -1) {
				screen.clickAndWaitForServer(backButtonId);
				return true;
			}

			if (super.mouseClicked(mouseX, mouseY, button)) return true;
        }

        return clickedHeader(
				(int) (mouseX - (double) (this.getX() + this.width / 2 - this.getRowWidth() / 2)),
				(int) (mouseY - (double) this.getY()) + (int) this.getScrollY() - 4
		);
    }

    @Override
    protected void renderHeader(DrawContext context, int x, int y) {
        context.drawText(MinecraftClient.getInstance().textRenderer, name, x, y + 1, 0xFFD0D0D0, false);
        int offset = 10;
        context.fill(x - 2, y + offset, x - 3 + getWidth(), y + 15 + offset, 0xFFF0F0F0);
        context.fill(x - 1, y + 1 + offset, x - 3 + getWidth() - 1, y + 14 + offset, Colors.BLACK);
        if (selectedOption != null) {
            context.drawText(MinecraftClient.getInstance().textRenderer, selectedOption.message, x + 2, y + 3 + offset, Colors.WHITE, true);
        }
        else context.drawText(MinecraftClient.getInstance().textRenderer, "???", x + 2, y + 3 + offset, Colors.WHITE, true);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if (isOpen) {
			if (animationProgress < 1) animationProgress += delta * 0.5f;
			else if (animationProgress != 1) animationProgress = 1;
		} else {
			animationProgress = 0;
		}

        if (PartyFinderScreen.DEBUG) {
            context.drawText(MinecraftClient.getInstance().textRenderer, String.valueOf(slotId), getX(), getY() - 10, Colors.RED, true);
            context.drawText(MinecraftClient.getInstance().textRenderer, String.valueOf(backButtonId), getX() + 50, getY() - 10, Colors.RED, true);
			context.drawText(client.textRenderer, String.valueOf(animationProgress), getX() - 10, getY(), Colors.GREEN, true);
        }

        int listHeight = Math.min(getHeight(), getEntryCount() * itemHeight + 4);
        int openedListHeight = isOpen ? (int) (listHeight * animationProgress) : (int) (listHeight * (1 - animationProgress));
        context.fill(getX(), getY() + headerHeight, getX() + getWidth() - 1, getY() + openedListHeight + headerHeight, 0xFFE0E0E0);
        context.fill(getX() + 1, getY() + headerHeight + 1, getX() + getWidth() - 2, getY() + openedListHeight + headerHeight - 1, Colors.BLACK);

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected void drawHeaderAndFooterSeparators(DrawContext context) {
    }

    @Override
    protected void drawMenuListBackground(DrawContext context) {
    }

    public void open(List<Option> entries, int backButtonId) {
		if (isOpen) return;
        isOpen = true;
		height = maxHeight;
		animationProgress = 0f;
        this.replaceEntries(entries);
        this.backButtonId = backButtonId;
    }

    public void close() {
		if (!isOpen) return;
        isOpen = false;
		height = CLOSED_HEIGHT;
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
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            int iconY = y + 1;
            matrices.translate(x, iconY);
            matrices.scale(0.8f, 0.8f);
            matrices.translate(-x, -iconY);
            context.drawItem(icon, x, iconY);
            matrices.popMatrix();

            if (PartyFinderScreen.DEBUG) context.drawText(client.textRenderer, String.valueOf(optionSlotId), x + 8, y, Colors.RED, true);
            context.drawText(client.textRenderer, Text.literal(message).fillStyle(Style.EMPTY.withUnderline(hovered)), x + 14, y + 3, Colors.WHITE, false);
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
