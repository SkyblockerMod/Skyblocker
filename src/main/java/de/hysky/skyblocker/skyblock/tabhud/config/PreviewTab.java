package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PreviewTab implements Tab {

    private final MinecraftClient client;
    private final PreviewWidget previewWidget;
    private final WidgetsConfigurationScreen parent;
    private ScreenBuilder.Layer currentLayer = ScreenBuilder.Layer.MAIN_TAB;
    private final ButtonWidget[] layerButtons;

    public PreviewTab(MinecraftClient client, WidgetsConfigurationScreen parent) {
        this.client = client;
        previewWidget = new PreviewWidget();
        this.parent = parent;

        ScreenBuilder.Layer[] values = ScreenBuilder.Layer.values();
        layerButtons = new ButtonWidget[values.length];
        for (int i = 0; i < values.length; i++) {
            ScreenBuilder.Layer layer = values[i];
            layerButtons[i] = ButtonWidget.builder(Text.literal(layer.toString()), button -> {
                        this.currentLayer = layer;
                        for (ButtonWidget layerButton : this.layerButtons) {
                            layerButton.active = !layerButton.equals(button);
                        }
                    })
                    .size(80, 15)
                    .build();
        }
    }

    @Override
    public Text getTitle() {
        return Text.literal("Preview");
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        consumer.accept(previewWidget);
        for (ButtonWidget layerButton : layerButtons) {
            consumer.accept(layerButton);
        }
    }

    @Override
    public void refreshGrid(ScreenRect tabArea) {
        float ratio = Math.min((tabArea.height() - 5) / (float) parent.height, (tabArea.width() - 105) / (float) parent.width);
        previewWidget.setPosition(5, tabArea.getTop() + 5);
        previewWidget.setWidth((int) (parent.width * ratio));
        previewWidget.setHeight((int) (parent.height * ratio));
        previewWidget.ratio = ratio;
        updatePlayerListFromPreview();
        ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(parent.getCurrentLocation());
        screenBuilder.positionWidgets(parent.width, parent.height);

        for (int i = 0; i < layerButtons.length; i++) {
            ButtonWidget layerButton = layerButtons[i];
            layerButton.setPosition(tabArea.width() - 90, tabArea.getTop() + 10 + i * 15);
        }

        forEachChild(clickableWidget -> clickableWidget.visible = parent.isPreviewVisible());
    }

    private void updatePlayerListFromPreview() {
        if (!parent.isPreviewVisible()) return;
        List<Text> lines = new ArrayList<>();
        for (int i = 3; i <= 5; i++) {
            ItemStack stack = parent.getHandler().getSlot(i).getStack();

            for (Text text : ItemUtils.getLore(stack)) {
                MutableText mutableText = Text.empty();
                AtomicBoolean foundSquare = new AtomicBoolean(false);

                text.visit((style, asString) -> {
                    if (!asString.startsWith("⬛")) {
                        mutableText.append(Text.literal(asString).fillStyle(style));
                    } else foundSquare.set(true);
                    return Optional.empty();
                }, Style.EMPTY);

                if (foundSquare.get()) {
                    lines.add(mutableText);
                    System.out.println(mutableText.getString());
                    System.out.println(mutableText);
                }
            }
        }
        PlayerListMgr.updateWidgetsFrom(lines);
    }

    public class PreviewWidget extends ClickableWidget {

        private float ratio = 1f;

        public PreviewWidget() {
            this(0, 0, 0, 0, Text.literal("Preview widget"));
        }

        public PreviewWidget(int x, int y, int width, int height, Text message) {
            super(x, y, width, height, message);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(parent.getCurrentLocation());
            context.drawBorder(getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, -1);
            context.enableScissor(getX(), getY(), getRight(), getBottom());
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(getX(), getY(), 0f);
            matrices.scale(ratio, ratio, 1f);

            screenBuilder.renderWidgets(context, PreviewTab.this.currentLayer);

            float localMouseX = (mouseX - getX()) / ratio;
            float localMouseY = (mouseY - getY()) / ratio;

            context.drawBorder((int) localMouseX, (int) localMouseY, 2, 2, Colors.RED);

            for (HudWidget hudWidget : screenBuilder.getHudWidgets(PreviewTab.this.currentLayer)) {
                if (hudWidget.isMouseOver(localMouseX, localMouseY)) {
                    context.drawBorder(
                            hudWidget.getX() - 1,
                            hudWidget.getY() - 1,
                            hudWidget.getWidth() + 2,
                            hudWidget.getHeight() + 2,
                            Colors.LIGHT_YELLOW);
                    break;
                }
            }

            matrices.pop();
            context.disableScissor();
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }
    }
}
