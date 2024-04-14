package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class EditBarColorPopup extends AbstractPopupScreen {

    private final Consumer<Color> setColor;

    private DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
    private BasicColorSelector colorSelector;

    protected EditBarColorPopup(Text title, Screen backgroundScreen, Consumer<Color> setColor) {
        super(title, backgroundScreen);
        this.setColor = setColor;
    }

    @Override
    protected void init() {
        super.init();
        layout = DirectionalLayoutWidget.vertical();
        layout.spacing(8).getMainPositioner().alignHorizontalCenter();
        layout.add(new TextWidget(title.copy().fillStyle(Style.EMPTY.withBold(true)), MinecraftClient.getInstance().textRenderer));
        colorSelector = new BasicColorSelector(0, 0, 150, () -> done(null));
        layout.add(colorSelector);

        DirectionalLayoutWidget horizontal = DirectionalLayoutWidget.horizontal();
        ButtonWidget buttonWidget = ButtonWidget.builder(Text.literal("Cancel"), button -> close()).width(80).build();
        horizontal.add(buttonWidget);
        horizontal.add(ButtonWidget.builder(Text.literal("Done"), this::done).width(80).build());

        layout.add(horizontal);
        layout.forEachChild(this::addDrawableChild);
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(layout, this.getNavigationFocus());
    }

    private void done(Object object) {
        if (colorSelector.validColor) setColor.accept(new Color(colorSelector.getColor()));
        close();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        drawPopupBackground(context, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
    }

    private static class BasicColorSelector extends ContainerWidget {

        private final EnterConfirmTextFieldWidget textFieldWidget;

        public BasicColorSelector(int x, int y, int width, Runnable onEnter) {
            super(x, y, width, 15, Text.literal("edit color"));
            textFieldWidget = new EnterConfirmTextFieldWidget(MinecraftClient.getInstance().textRenderer, getX() + 16, getY(), width - 16, 15, Text.empty(), onEnter);
            textFieldWidget.setChangedListener(this::onTextChange);
            textFieldWidget.setTextPredicate(s -> s.length() <= 6);
        }

        @Override
        public List<? extends Element> children() {
            return List.of(textFieldWidget);
        }

        public int getColor() {
            return color;
        }

        private int color = 0xFF000000;
        private boolean validColor = false;

        private void onTextChange(String text) {
            try {
                color = Integer.parseInt(text, 16) | 0xFF000000;
                validColor = true;
            } catch (NumberFormatException e) {
                color = 0;
                validColor = false;
            }
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            context.drawBorder(getX(), getY(), 15, 15, validColor ? -1 : 0xFFDD0000);
            context.fill(getX() + 1, getY() + 1, getX() + 14, getY() + 14, color);
            textFieldWidget.renderWidget(context, mouseX, mouseY, delta);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }

        @Override
        public void setX(int x) {
            super.setX(x);
            textFieldWidget.setX(getX()+16);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            textFieldWidget.setY(getY());
        }
    }
}
