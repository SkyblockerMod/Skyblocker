package de.hysky.skyblocker.skyblock.fancybars;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class EditBarWidget extends ContainerWidget {

    private final EnumCyclingOption<StatusBar.IconPosition> iconOption;
    private final BooleanOption booleanOption;

    private final ColorOption color1;
    private final ColorOption color2;
    private final ColorOption textColor;
    private final TextWidget nameWidget;

    private int contentsWidth = 0;

    public EditBarWidget(int x, int y, Screen parent) {
        super(x, y, 100, 66, Text.literal("Edit bar"));

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        nameWidget = new TextWidget(Text.empty(), textRenderer);

        MutableText translatable = Text.translatable("skyblocker.bars.config.icon");
        contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + textRenderer.getWidth("RIGHT") + 10);
        iconOption = new EnumCyclingOption<>(0, 11, getWidth(), translatable, StatusBar.IconPosition.class);

        translatable = Text.translatable("skyblocker.bars.config.showValue");
        contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + 9 + 10);
        booleanOption = new BooleanOption(0, 22, getWidth(), translatable);

        // COLO(u)RS
        translatable = Text.translatable("skyblocker.bars.config.mainColor");
        contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + 9 + 10);
        color1 = new ColorOption(0, 33, getWidth(), translatable, parent);

        translatable = Text.translatable("skyblocker.bars.config.overflowColor");
        contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + 9 + 10);
        color2 = new ColorOption(0, 44, getWidth(), translatable, parent);

        translatable = Text.translatable("skyblocker.bars.config.textColor");
        contentsWidth = Math.max(contentsWidth, textRenderer.getWidth(translatable) + 9 + 10);
        textColor = new ColorOption(0, 55, getWidth(), translatable, parent);

        setWidth(contentsWidth);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(iconOption, booleanOption, color1, color2, textColor);
    }

    public int insideMouseX = 0;
    public int insideMouseY = 0;

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (isHovered()) {
            insideMouseX = mouseX;
            insideMouseY = mouseY;
        } else {
            int i = mouseX - insideMouseX;
            int j = mouseY - insideMouseY;
            if (i * i + j * j > 30 * 30) visible = false;
        }
        TooltipBackgroundRenderer.render(context, getX(), getY(), getWidth(), getHeight(), 0);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(getX(), getY(), 0);
        nameWidget.render(context, mouseX, mouseY, delta);
        iconOption.renderWidget(context, mouseX - getX(), mouseY - getY(), delta);
        booleanOption.renderWidget(context, mouseX - getX(), mouseY - getY(), delta);
        color1.renderWidget(context, mouseX - getX(), mouseY - getY(), delta);
        color2.renderWidget(context, mouseX - getX(), mouseY - getY(), delta);
        textColor.renderWidget(context, mouseX - getX(), mouseY - getY(), delta);
        matrices.pop();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        if (!isHovered()) visible = false;
        return super.mouseClicked(mouseX - getX(), mouseY - getY(), button);
    }

    public void setStatusBar(StatusBar statusBar) {
        iconOption.setCurrent(statusBar.getIconPosition());
        iconOption.setOnChange(statusBar::setIconPosition);
        booleanOption.setCurrent(statusBar.showText());
        booleanOption.setOnChange(statusBar::setShowText);

        color1.setCurrent(statusBar.getColors()[0].getRGB());
        color1.setOnChange(color ->  statusBar.getColors()[0] = color);

        color2.active = statusBar.hasOverflow();
        if (color2.active) {
            color2.setCurrent(statusBar.getColors()[1].getRGB());
            color2.setOnChange(color ->  statusBar.getColors()[1] = color);
        }

        if (statusBar.getTextColor() != null) {
            textColor.setCurrent(statusBar.getTextColor().getRGB());
        }
        textColor.setOnChange(statusBar::setTextColor);

        MutableText formatted = statusBar.getName().copy().formatted(Formatting.BOLD);
        nameWidget.setMessage(formatted);
        setWidth(Math.max(MinecraftClient.getInstance().textRenderer.getWidth(formatted), contentsWidth));
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        iconOption.setWidth(width);
        booleanOption.setWidth(width);
        color1.setWidth(width);
        color2.setWidth(width);
        textColor.setWidth(width);
        nameWidget.setWidth(width);

    }

    public static class EnumCyclingOption<T extends Enum<T>> extends ClickableWidget {

        private T current;
        private final T[] values;
        private Consumer<T> onChange = null;

        public EnumCyclingOption(int x, int y, int width, Text message, Class<T> enumClass) {
            super(x, y, width, 11, message);
            values = enumClass.getEnumConstants();
            current = values[0];
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            if (isMouseOver(mouseX, mouseY)) {
                context.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
            }
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.drawText(textRenderer, getMessage(), getX() + 1, getY() + 1, -1, true);
            String string = current.toString();
            context.drawText(textRenderer, string, getRight() - textRenderer.getWidth(string) - 1, getY() + 1, -1, true);
        }

        public void setCurrent(T current) {
            this.current = current;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            current = values[(current.ordinal() + 1) % values.length];
            if (onChange != null) onChange.accept(current);
            super.onClick(mouseX, mouseY);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        }

        public void setOnChange(Consumer<T> onChange) {
            this.onChange = onChange;
        }
    }

    public static class BooleanOption extends ClickableWidget {

        private boolean current = false;
        private Consumer<Boolean> onChange = null;

        public BooleanOption(int x, int y, int width, Text message) {
            super(x, y, width, 11, message);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            if (isMouseOver(mouseX, mouseY)) {
                context.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
            }
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.drawText(textRenderer, getMessage(), getX() + 1, getY() + 1, -1, true);
            context.drawBorder(getRight() - 10, getY() + 1, 9, 9, -1);
            if (current) context.fill(getRight() - 8, getY() + 3, getRight() - 3, getY() + 8, -1);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            current = !current;
            if (onChange != null) onChange.accept(current);
            super.onClick(mouseX, mouseY);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        }

        public void setCurrent(boolean current) {
            this.current = current;
        }

        public void setOnChange(Consumer<Boolean> onChange) {
            this.onChange = onChange;
        }
    }

    public static class ColorOption extends ClickableWidget {

        public void setCurrent(int current) {
            this.current = current;
        }

        private int current = 0;
        private Consumer<Color> onChange = null;
        private final Screen parent;

        public ColorOption(int x, int y, int width, Text message, Screen parent) {
            super(x, y, width, 11, message);
            this.parent = parent;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            if (isMouseOver(mouseX, mouseY)) {
                context.fill(getX(), getY(), getRight(), getBottom(), 0x20FFFFFF);
            }
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.drawText(textRenderer, getMessage(), getX() + 1, getY() + 1,  active ? -1 : Colors.GRAY, true);
            context.drawBorder(getRight() - 10, getY() + 1, 9, 9, active ? -1 : Colors.GRAY);
            context.fill(getRight() - 8, getY() + 3, getRight() - 3, getY() + 8, active ? current : Colors.GRAY);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            super.onClick(mouseX, mouseY);
            MinecraftClient.getInstance().setScreen(new EditBarColorPopup(Text.literal("Edit ").append(getMessage()), parent, this::set));
        }

        private void set(Color color) {
            current = color.getRGB();
            if (onChange != null) onChange.accept(color);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }

        public void setOnChange(Consumer<Color> onChange) {
            this.onChange = onChange;
        }
    }
}
