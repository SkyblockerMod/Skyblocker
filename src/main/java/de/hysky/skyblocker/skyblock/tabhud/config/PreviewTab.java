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
import net.minecraft.client.gui.widget.TextWidget;
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

    public static final int RIGHT_SIDE_WIDTH = 120;

    private final MinecraftClient client;
    private final PreviewWidget previewWidget;
    private final WidgetsConfigurationScreen parent;
    private final WidgetOptionsScrollable widgetOptions;
    private ScreenMaster.ScreenLayer currentScreenLayer = ScreenMaster.ScreenLayer.MAIN_TAB;
    private final ButtonWidget[] layerButtons;

    public PreviewTab(MinecraftClient client, WidgetsConfigurationScreen parent) {
        this.client = client;
        this.parent = parent;

        previewWidget = new PreviewWidget();
        widgetOptions = new WidgetOptionsScrollable();
        widgetOptions.setWidth(RIGHT_SIDE_WIDTH - 10);

        ScreenMaster.ScreenLayer[] values = ScreenMaster.ScreenLayer.values();
        layerButtons = new ButtonWidget[values.length];
        for (int i = 0; i < values.length; i++) {
            ScreenMaster.ScreenLayer screenLayer = values[i];
            layerButtons[i] = ButtonWidget.builder(Text.literal(screenLayer.toString()), button -> {
                        this.currentScreenLayer = screenLayer;
                        for (ButtonWidget layerButton : this.layerButtons) {
                            layerButton.active = !layerButton.equals(button);
                        }
                    })
                    .size(RIGHT_SIDE_WIDTH - 20, 15)
                    .build();
            if (screenLayer == currentScreenLayer) layerButtons[i].active = false;
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
                    //System.out.println(mutableText.getString());
                    //System.out.println(mutableText);
                }
            }
        }
        PlayerListMgr.updateWidgetsFrom(lines);
    }

    void onHudWidgetSelected(@Nullable HudWidget hudWidget) {
        widgetOptions.clearWidgets();
        if (hudWidget == null) return;
        ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(parent.getCurrentLocation());
        PositionRule positionRule = screenBuilder.getPositionRule(hudWidget.getInternalID());
        int width = widgetOptions.getWidth() - widgetOptions.getScrollerWidth();
        // TODO localization

        widgetOptions.addWidget(new TextWidget(width, 9, Text.literal(hudWidget.getNiceName()).formatted(Formatting.BOLD, Formatting.UNDERLINE), client.textRenderer));
        if (positionRule == null) {
            widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Positioning: Auto"), button -> {
                        screenBuilder.setPositionRule(hudWidget.getInternalID(), PositionRule.DEFAULT);
                        updateWidgets();
                        onHudWidgetSelected(hudWidget);
                    })
                    .width(width)
                    .build());
        } else {
            widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Positioning: Custom"), button -> {
                        screenBuilder.setPositionRule(hudWidget.getInternalID(), null);
                        updateWidgets();
                        onHudWidgetSelected(hudWidget);
                    })
                    .width(width)
                    .build());

            String ye = "Layer: " + (positionRule.screenLayer() == null ? "Default" : positionRule.screenLayer().toString());

            widgetOptions.addWidget(ButtonWidget.builder(Text.literal(ye), button -> {
                ScreenBuilder builder = ScreenMaster.getScreenBuilder(parent.getCurrentLocation());
                PositionRule rule = builder.getPositionRuleOrDefault(hudWidget.getInternalID());
                ScreenMaster.ScreenLayer[] values = ScreenMaster.ScreenLayer.values();
                ScreenMaster.ScreenLayer newLayer;
                if (rule.screenLayer() == null) {
                    newLayer = values[0];
                } else if (rule.screenLayer().ordinal() == values.length - 1) {
                    newLayer = null;
                } else {
                    newLayer = values[rule.screenLayer().ordinal() + 1];
                }

                PositionRule newRule = new PositionRule(
                        rule.parent(),
                        rule.parentPoint(),
                        rule.thisPoint(),
                        rule.relativeX(),
                        rule.relativeY(),
                        newLayer
                );
                builder.setPositionRule(hudWidget.getInternalID(), newRule);
                button.setMessage(Text.literal("Layer: " + (newRule.screenLayer() == null ? "Default" : newRule.screenLayer().toString())));
                updateWidgets();
                if (newLayer != null) {
                    layerButtons[newLayer.ordinal()].onPress();
                }

            }).width(width).build());

            String parentName = positionRule.parent().equals("screen") ? "Screen" : ScreenMaster.widgetInstances.get(positionRule.parent()).getNiceName();

            widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Parent: " + parentName), button -> {
                this.previewWidget.pickParent = true;
                button.setMessage(Text.literal("Click on a widget"));
            }).width(width).build());

            widgetOptions.addWidget(new AnchorSelectionWidget(width, Text.literal("This anchor"), false));
            widgetOptions.addWidget(new AnchorSelectionWidget(width, Text.literal("Parent anchor"), true));

        }
    }

    void updateWidgets() {
        ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(parent.getCurrentLocation());
        updatePlayerListFromPreview();
        screenBuilder.positionWidgets(parent.width, parent.height);
    }

    /**
     * The preview widget that captures clicks and displays the current state of the widgets.
     */
    public class PreviewWidget extends ClickableWidget {

        private float ratio = 1f;
        /**
         * The widget the user is hovering with the mouse
         */
        private @Nullable HudWidget hoveredWidget = null;
        /**
         * The selected widget, settings for it show on the right, and it can be dragged around
         */
        private @Nullable HudWidget selectedWidget = null;
        /**
         * The original pos, of the selected widget, it is set when you click on it. So when it's done being dragged it can be compared.
         * Effectively, if this ain't null, the user is dragging a widget around.
         */
        private @Nullable ScreenPos selectedOriginalPos = null;
        protected boolean pickParent = false;

        public PreviewWidget() {
            super(0, 0, 0, 0, Text.literal("Preview widget"));
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

            screenBuilder.renderWidgets(context, PreviewTab.this.currentScreenLayer);

            float localMouseX = (mouseX - getX()) / ratio;
            float localMouseY = (mouseY - getY()) / ratio;

            context.drawBorder((int) localMouseX, (int) localMouseY, 2, 2, Colors.RED);

            for (HudWidget hudWidget : screenBuilder.getHudWidgets(PreviewTab.this.currentScreenLayer)) {
                if (hudWidget.isMouseOver(localMouseX, localMouseY)) {
                    hoveredWidget = hudWidget;
                    break;
                }
            }

            // Counter-attack the translation in the widgets
            matrices.translate(0.f, 0.f, 350.f);

            // HOVERED
            if (hoveredWidget != null && !hoveredWidget.equals(selectedWidget)) {
                context.drawBorder(
                        hoveredWidget.getX() - 1,
                        hoveredWidget.getY() - 1,
                        hoveredWidget.getWidth() + 2,
                        hoveredWidget.getHeight() + 2,
                        Colors.LIGHT_YELLOW);
            }

            // SELECTED
            if (selectedWidget != null) {
                //noinspection DataFlowIssue
                context.drawBorder(
                        selectedWidget.getX() - 1,
                        selectedWidget.getY() - 1,
                        selectedWidget.getWidth() + 2,
                        selectedWidget.getHeight() + 2,
                        Formatting.GREEN.getColorValue() | 0xFF000000);

                PositionRule rule = screenBuilder.getPositionRule(selectedWidget.getInternalID());
                if (rule != null) {
                    // TODO: rename that maybe that's a bit wack
                    int relativeX = 0;
                    int relativeY = 0;
                    if (selectedOriginalPos != null) {
                        relativeX = selectedWidget.getX() - selectedOriginalPos.x();
                        relativeY = selectedWidget.getY() - selectedOriginalPos.y();
                    }
                    int thisAnchorX = (int) (selectedWidget.getX() + rule.thisPoint().horizontalPoint().getPercentage() * selectedWidget.getWidth());
                    int thisAnchorY = (int) (selectedWidget.getY() + rule.thisPoint().verticalPoint().getPercentage() * selectedWidget.getHeight());

                    int translatedX = Math.min(thisAnchorX - rule.relativeX() - relativeX, parent.width - 2);
                    int translatedY = Math.min(thisAnchorY - rule.relativeY() - relativeY, parent.height - 2);

                    renderUnits(context, relativeX, rule, thisAnchorX, thisAnchorY, relativeY, translatedX, translatedY);

                    context.drawHorizontalLine(translatedX, thisAnchorX, thisAnchorY + 1, 0xAAAA0000);
                    context.drawVerticalLine(translatedX + 1, translatedY, thisAnchorY, 0xAAAA0000);


                    context.drawHorizontalLine(translatedX, thisAnchorX, thisAnchorY, Colors.RED);
                    context.drawVerticalLine(translatedX, translatedY, thisAnchorY, Colors.RED);
                }
            }

            matrices.pop();
            context.disableScissor();
        }

        private void renderUnits(DrawContext context, int relativeX, PositionRule rule, int thisAnchorX, int thisAnchorY, int relativeY, int translatedX, int translatedY) {
            boolean xUnitOnTop = rule.relativeY() > 0;
            if (xUnitOnTop && thisAnchorY < 10) xUnitOnTop = false;
            if (!xUnitOnTop && thisAnchorY > parent.height - 10) xUnitOnTop = true;

            String yUnitText = String.valueOf(rule.relativeY() + relativeY);
            int yUnitTextWidth = client.textRenderer.getWidth(yUnitText);
            boolean yUnitOnRight = rule.relativeX() > 0;
            if (yUnitOnRight && translatedX + 2 + yUnitTextWidth >= parent.width) yUnitOnRight = false;
            if (!yUnitOnRight && translatedX - 2 - yUnitTextWidth <= 0) yUnitOnRight = true;

            // X
            context.drawCenteredTextWithShadow(client.textRenderer, String.valueOf(relativeX + rule.relativeX()), thisAnchorX - (relativeX + rule.relativeX()) / 2,  xUnitOnTop ? thisAnchorY - 9 : thisAnchorY + 2, Colors.LIGHT_RED);
            // Y
            context.drawText(client.textRenderer, yUnitText, yUnitOnRight ? translatedX + 2 : translatedX - 1 - yUnitTextWidth, thisAnchorY - (relativeY + rule.relativeY() - 9) / 2, Colors.LIGHT_RED, true);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        }

        private double bufferedDeltaX = 0;
        private double bufferedDeltaY = 0;

        @Override
        protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
            double localDeltaX = deltaX / ratio + bufferedDeltaX;
            double localDeltaY = deltaY / ratio + bufferedDeltaY;

            bufferedDeltaX = localDeltaX - (int) localDeltaX;
            bufferedDeltaY = localDeltaY - (int) localDeltaY;

            if (selectedWidget != null && selectedOriginalPos != null) {
                selectedWidget.setX(selectedWidget.getX() + (int) localDeltaX);
                selectedWidget.setY(selectedWidget.getY() + (int) localDeltaY);
            }
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            if (pickParent) {
                pickParent = false;
                return;
            }
            if (!Objects.equals(hoveredWidget, selectedWidget)) {
                PreviewTab.this.onHudWidgetSelected(hoveredWidget);
            }
            // TODO releasing a widget outside of the area causes weird behavior, might wanna look into that
            // Update positioning real
            if (selectedWidget != null && selectedOriginalPos != null) {
                ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(parent.getCurrentLocation());
                PositionRule oldRule = screenBuilder.getPositionRule(selectedWidget.getInternalID());
                if (oldRule == null) oldRule = PositionRule.DEFAULT;
                int relativeX = selectedWidget.getX() - selectedOriginalPos.x();
                int relativeY = selectedWidget.getY() - selectedOriginalPos.y();
                screenBuilder.setPositionRule(selectedWidget.getInternalID(), new PositionRule(
                        oldRule.parent(),
                        oldRule.parentPoint(),
                        oldRule.thisPoint(),
                        oldRule.relativeX() + relativeX,
                        oldRule.relativeY() + relativeY,
                        oldRule.screenLayer()));
                updateWidgets();
            }

            selectedWidget = hoveredWidget;
            selectedOriginalPos = null;
            super.onRelease(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(parent.getCurrentLocation());
            if (pickParent && selectedWidget != null && !selectedWidget.equals(hoveredWidget)) {
                PositionRule oldRule = screenBuilder.getPositionRule(selectedWidget.getInternalID());
                if (oldRule == null) oldRule = PositionRule.DEFAULT;

                int thisAnchorX = (int) (selectedWidget.getX() + oldRule.thisPoint().horizontalPoint().getPercentage() * selectedWidget.getWidth());
                int thisAnchorY = (int) (selectedWidget.getY() + oldRule.thisPoint().verticalPoint().getPercentage() * selectedWidget.getHeight());

                int otherAnchorX = hoveredWidget == null ? 0 : hoveredWidget.getX();
                int otherAnchorY = hoveredWidget == null ? 0 : hoveredWidget.getY();

                PositionRule newRule = new PositionRule(
                        hoveredWidget == null ? "screen" : hoveredWidget.getInternalID(),
                        PositionRule.Point.DEFAULT,
                        oldRule.thisPoint(),
                        thisAnchorX - otherAnchorX,
                        thisAnchorY - otherAnchorY,
                        oldRule.screenLayer()
                );
                screenBuilder.setPositionRule(selectedWidget.getInternalID(), newRule);
                updateWidgets();
                PreviewTab.this.onHudWidgetSelected(selectedWidget);
                return true;
            }

            double localMouseX = (mouseX - getX()) / ratio;
            double localMouseY = (mouseY - getY()) / ratio;

            if (selectedWidget != null && selectedWidget.isMouseOver(localMouseX, localMouseY) &&
                    screenBuilder.getPositionRule(selectedWidget.getInternalID()) != null) {
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

        void addWidget(ClickableWidget clickableWidget) {
            widgets.add(clickableWidget);
        }
    }

    private class AnchorSelectionWidget extends ClickableWidget {

        private final boolean other;
        private @Nullable PositionRule.Point hoveredPoint = null;

        public AnchorSelectionWidget(int width, Text text, boolean other) {
            super(0, 0, width, 40, text);
            this.other = other;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            hoveredPoint = null;
            context.drawText(client.textRenderer, getMessage(), getX(), getY(), Colors.WHITE, true);
            context.getMatrices().push();
            context.getMatrices().translate(getX(), getY() + 10, 0.f);
            // Rectangle thing
            int x = getWidth() / 6;
            int w = (int) (4 * getWidth() / 6f);
            int y = 5; // 30 / 6
            int h = 20;

            context.drawBorder(x, y + 1, w, h, Colors.WHITE);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int squareX = x + (i * getWidth()) / 3;
                    int squareY = y + (j * 30) / 3;
                    boolean selectedAnchor = false;
                    if (previewWidget.selectedWidget != null) {
                        String internalID = previewWidget.selectedWidget.getInternalID();
                        PositionRule positionRule = ScreenMaster.getScreenBuilder(parent.getCurrentLocation()).getPositionRule(internalID);
                        if (positionRule != null) {
                            PositionRule.Point point = other ? positionRule.parentPoint() : positionRule.thisPoint();
                            selectedAnchor = point.horizontalPoint().ordinal() == i && point.verticalPoint().ordinal() == j;
                        }
                    }

                    boolean hoveredAnchor = mouseX >= getX() + i * getWidth()/3 &&
                            mouseX < getX() + (i + 1) * getWidth() / 3 &&
                            mouseY >= getY() + 10 + j * 10 &&
                            mouseY < getY() + 10 + (j + 1) * 10;

                    if (hoveredAnchor) {
                        PositionRule.VerticalPoint[] verticalPoints = PositionRule.VerticalPoint.values();
                        PositionRule.HorizontalPoint[] horizontalPoints = PositionRule.HorizontalPoint.values();
                        hoveredPoint = new PositionRule.Point(verticalPoints[j], horizontalPoints[i]);
                    }

                    context.fill(squareX - 1, squareY - 1, squareX + 2, squareY + 2, hoveredAnchor ? Colors.RED : selectedAnchor ? Colors.YELLOW : Colors.WHITE);
                }
            }
            context.getMatrices().pop();
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (hoveredPoint != null && previewWidget.selectedWidget != null) {
                ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(parent.getCurrentLocation());
                String internalID = previewWidget.selectedWidget.getInternalID();
                PositionRule oldRule = screenBuilder.getPositionRule(internalID);
                if (oldRule == null) oldRule = PositionRule.DEFAULT;
                if (other) {
                    screenBuilder.setPositionRule(internalID, new PositionRule(
                            oldRule.parent(),
                            hoveredPoint,
                            oldRule.thisPoint(),
                            oldRule.relativeX(),
                            oldRule.relativeY(),
                            oldRule.screenLayer()));
                } else {
                    screenBuilder.setPositionRule(internalID, new PositionRule(
                            oldRule.parent(),
                            oldRule.parentPoint(),
                            hoveredPoint,
                            oldRule.relativeX(),
                            oldRule.relativeY(),
                            oldRule.screenLayer()));
                }
            }
            updateWidgets();
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }
}
