package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PreviewTab implements Tab {

    private static final int RIGHT_SIDE_WIDTH = 120;

    private final MinecraftClient client;
    private final PreviewWidget previewWidget;
    private final WidgetsConfigurationScreen parent;
    private final WidgetOptionsScrollable widgetOptions;
    private ScreenBuilder.Layer currentLayer = ScreenBuilder.Layer.MAIN_TAB;
    private final ButtonWidget[] layerButtons;

    public PreviewTab(MinecraftClient client, WidgetsConfigurationScreen parent) {
        this.client = client;
        this.parent = parent;

        previewWidget = new PreviewWidget();
        previewWidget.setOnSelectedChanged(this::onHudWidgetSelected);
        widgetOptions = new WidgetOptionsScrollable();
        widgetOptions.setWidth(RIGHT_SIDE_WIDTH - 10);

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
                    .size(RIGHT_SIDE_WIDTH - 20, 15)
                    .build();
            if (layer == currentLayer) layerButtons[i].active = false;
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
        consumer.accept(widgetOptions);
    }

    @Override
    public void refreshGrid(ScreenRect tabArea) {
        float ratio = Math.min((tabArea.height() - 5) / (float) parent.height, (tabArea.width() - RIGHT_SIDE_WIDTH - 5) / (float) parent.width);
        previewWidget.setPosition(5, tabArea.getTop() + 5);
        previewWidget.setWidth((int) (parent.width * ratio));
        previewWidget.setHeight((int) (parent.height * ratio));
        previewWidget.ratio = ratio;
        updatePlayerListFromPreview();
        ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(parent.getCurrentLocation());
        screenBuilder.positionWidgets(parent.width, parent.height);

        for (int i = 0; i < layerButtons.length; i++) {
            ButtonWidget layerButton = layerButtons[i];
            layerButton.setPosition(tabArea.width() - layerButton.getWidth() - 10, tabArea.getTop() + 10 + i * 15);
        }
        int optionsY = tabArea.getTop() + 10 + layerButtons.length * 15 + 5;
        widgetOptions.setPosition(tabArea.width() - widgetOptions.getWidth() - 5, optionsY);
        widgetOptions.setHeight(tabArea.height() - optionsY - 5);

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

    void onHudWidgetSelected(@Nullable HudWidget hudWidget) {
        widgetOptions.clearWidgets();
        if (hudWidget == null) return;
        ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(parent.getCurrentLocation());
        if (screenBuilder.getPositionRule(hudWidget.getInternalID()) == null) {
            widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Positioning: Auto"), button -> {
                        screenBuilder.setPositionRule(hudWidget.getInternalID(), PositionRule.DEFAULT);
                        updatePlayerListFromPreview();
                        screenBuilder.positionWidgets(parent.width, parent.height);
                        onHudWidgetSelected(hudWidget);
                    })
                    .width(widgetOptions.getWidth() - widgetOptions.getScrollerWidth())
                    .build());
        } else {
            widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Positioning: Custom"), button -> {
                        screenBuilder.setPositionRule(hudWidget.getInternalID(), null);
                        updatePlayerListFromPreview();
                        screenBuilder.positionWidgets(parent.width, parent.height);
                        onHudWidgetSelected(hudWidget);
                    })
                    .width(widgetOptions.getWidth() - widgetOptions.getScrollerWidth())
                    .build());
        }
    }

    /**
     * The preview widget that captures clicks and displays the current state of the widgets.
     */
    public class PreviewWidget extends ClickableWidget {

        private float ratio = 1f;
        private @Nullable HudWidget hoveredWidget = null;
        private @Nullable HudWidget selectedWidget = null;
        private @Nullable ScreenPos selectedOriginalPos = null;

        private @Nullable Consumer<@Nullable HudWidget> onSelectedChanged = null;

        public void setOnSelectedChanged(@Nullable Consumer<HudWidget> onSelectedChanged) {
            this.onSelectedChanged = onSelectedChanged;
        }

        public PreviewWidget() {
            this(0, 0, 0, 0, Text.literal("Preview widget"));
        }

        public PreviewWidget(int x, int y, int width, int height, Text message) {
            super(x, y, width, height, message);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            hoveredWidget = null;

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
                // SELECTED
                if (hudWidget.equals(selectedWidget)) {
                    //noinspection DataFlowIssue
                    context.drawBorder(
                            hudWidget.getX() - 1,
                            hudWidget.getY() - 1,
                            hudWidget.getWidth() + 2,
                            hudWidget.getHeight() + 2,
                            Formatting.GREEN.getColorValue() | 0xFF000000);

                    PositionRule rule = screenBuilder.getPositionRule(selectedWidget.getInternalID());
                    if (rule != null) {
                        int thisAnchorX = (int) (hudWidget.getX() + rule.thisPoint().horizontalPoint().getPercentage() * hudWidget.getWidth());
                        int thisAnchorY = (int) (hudWidget.getY() + rule.thisPoint().verticalPoint().getPercentage() * hudWidget.getHeight());

                        context.drawHorizontalLine(thisAnchorX - rule.relativeX(), thisAnchorX, thisAnchorY, Colors.LIGHT_RED);
                        context.drawVerticalLine(thisAnchorX - rule.relativeX(), thisAnchorY - rule.relativeY(), thisAnchorY, Colors.LIGHT_RED);
                    }
                }
                // HOVERED
                else if (hudWidget.isMouseOver(localMouseX, localMouseY) && hoveredWidget == null) {
                    context.drawBorder(
                            hudWidget.getX() - 1,
                            hudWidget.getY() - 1,
                            hudWidget.getWidth() + 2,
                            hudWidget.getHeight() + 2,
                            Colors.LIGHT_YELLOW);
                    hoveredWidget = hudWidget;
                }
            }

            matrices.pop();
            context.disableScissor();
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            if (!Objects.equals(hoveredWidget, selectedWidget) && onSelectedChanged != null) {
                System.out.println("HEY!!!");
                onSelectedChanged.accept(hoveredWidget);
            }
            selectedWidget = hoveredWidget;
            selectedOriginalPos = null;
            super.onRelease(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (selectedWidget != null) {
                selectedOriginalPos = new ScreenPos(selectedWidget.getX(), selectedWidget.getY());
            }
            return this.active && this.visible && isMouseOver(mouseX, mouseY);
        }
    }

    private static class WidgetOptionsScrollable extends ScrollableWidget {

        private final List<ClickableWidget> widgets = new ArrayList<>();
        private int height = 0;

        public WidgetOptionsScrollable() {
            super(0, 0, 0, 0, Text.literal("Widget Options Scrollable"));
        }

        @Override
        protected int getContentsHeight() {
            return height;
        }

        @Override
        protected double getDeltaYPerScroll() {
            return 6;
        }

        @Override
        protected void renderContents(DrawContext context, int mouseX, int mouseY, float delta) {
            height = 0;
            for (ClickableWidget widget : widgets) {
                widget.setX(getX() + 1);
                widget.setY(getY() + 1 + height);

                height += widget.getHeight() + 1;
                if (!isVisible(widget.getY(), widget.getBottom())) continue;
                widget.render(context, mouseX, mouseY + (int) getScrollY(), delta);

            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            for (ClickableWidget widget : widgets) {
                if (!isVisible(widget.getY(), widget.getBottom())) continue;
                if (widget.mouseClicked(mouseX, mouseY + getScrollY(), button)) return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        protected void drawBox(DrawContext context) {
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        }

        void clearWidgets() {
            widgets.clear();
        }

        boolean addWidget(ClickableWidget clickableWidget) {
            return widgets.add(clickableWidget);
        }
    }
}
