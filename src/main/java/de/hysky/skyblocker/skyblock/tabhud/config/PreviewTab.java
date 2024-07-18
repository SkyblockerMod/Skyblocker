package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.InGameHudInvoker;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.TabHudWidget;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

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
    private final boolean dungeon;
    private final ButtonWidget restorePositioning;
    private ScreenMaster.ScreenLayer currentScreenLayer = ScreenMaster.ScreenLayer.MAIN_TAB;
    private final ButtonWidget[] layerButtons;
    private final TextWidget textWidget;
    private final ScoreboardObjective placeHolderObjective;

    public PreviewTab(MinecraftClient client, WidgetsConfigurationScreen parent, boolean dungeon) {
        this.client = client;
        this.parent = parent;
        this.dungeon = dungeon;
        this.textWidget = new TextWidget(
                Text.literal("This tab is specifically for dungeons, as it currently doesn't have hypixel's system"),
                client.textRenderer
        );

        previewWidget = new PreviewWidget();
        widgetOptions = new WidgetOptionsScrollable();
        widgetOptions.setWidth(RIGHT_SIDE_WIDTH - 10);

        ScreenMaster.ScreenLayer[] values = ScreenMaster.ScreenLayer.values();
        layerButtons = new ButtonWidget[3];
        for (int i = 0; i < 3; i++) {
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

        restorePositioning = ButtonWidget.builder(Text.literal("Restore Positioning"), button -> {
                    ScreenMaster.getScreenBuilder(getCurrentLocation()).restorePositioningFromBackup();
                    updateWidgets();
                })
                .width(100)
                .tooltip(Tooltip.of(Text.literal("Reset positions to before you opened this screen!")))
                .build();

        placeHolderObjective = new ScoreboardObjective(
                new Scoreboard(),
                "temp",
                ScoreboardCriterion.DUMMY,
                Text.literal("SKYBLOCK"),
                ScoreboardCriterion.RenderType.INTEGER,
                true,
                BlankNumberFormat.INSTANCE
        );
        Scoreboard scoreboard = placeHolderObjective.getScoreboard();
        scoreboard.getOrCreateScore(createHolder(Text.literal("Random text!")), placeHolderObjective).setScore(0);
        scoreboard.getOrCreateScore(createHolder(Text.literal("To fill in")), placeHolderObjective).setScore(-1);
        scoreboard.getOrCreateScore(createHolder(Text.literal("The place!")), placeHolderObjective).setScore(-2);
        scoreboard.getOrCreateScore(createHolder(Text.literal("...")), placeHolderObjective).setScore(-3);
        scoreboard.getOrCreateScore(createHolder(Text.literal("yea")), placeHolderObjective).setScore(-4);
        scoreboard.getOrCreateScore(createHolder(Text.literal("so how's your")), placeHolderObjective).setScore(-5);
        scoreboard.getOrCreateScore(createHolder(Text.literal("day? great that's")), placeHolderObjective).setScore(-6);
        scoreboard.getOrCreateScore(createHolder(Text.literal("nice to hear.")), placeHolderObjective).setScore(-7);
        scoreboard.getOrCreateScore(createHolder(Text.literal("this should be")), placeHolderObjective).setScore(-8);
        scoreboard.getOrCreateScore(createHolder(Text.literal("enough lines bye")), placeHolderObjective).setScore(-9);
        scoreboard.getOrCreateScore(createHolder(Text.literal("NEVER GONNA GIVE Y-")), placeHolderObjective).setScore(-10);
    }

    private ScoreHolder createHolder(Text name) {
        return new ScoreHolder() {
            @Override
            public String getNameForScoreboard() {
                return name.getString().replace(' ', '_');
            }

            @Nullable
            @Override
            public Text getDisplayName() {
                return name;
            }
        };
    }

    public void goToLayer(ScreenMaster.ScreenLayer layer) {
        if (layer == ScreenMaster.ScreenLayer.DEFAULT) layer = ScreenMaster.ScreenLayer.HUD;
        layerButtons[layer.ordinal()].onPress();
    }

    @Override
    public Text getTitle() {
        return Text.literal(dungeon ? "Dungeons Editing" : "Preview");
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        consumer.accept(previewWidget);
        for (ButtonWidget layerButton : layerButtons) {
            consumer.accept(layerButton);
        }
        consumer.accept(widgetOptions);
        consumer.accept(restorePositioning);
        if (dungeon) consumer.accept(textWidget);
    }

    @Override
    public void refreshGrid(ScreenRect tabArea) {
        float ratio = Math.min((tabArea.height() - 35) / (float) (parent.height), (tabArea.width() - RIGHT_SIDE_WIDTH - 5) / (float) (parent.width));
        previewWidget.setPosition(5, tabArea.getTop() + 5);
        previewWidget.setWidth((int) (parent.width * ratio));
        previewWidget.setHeight((int) (parent.height * ratio));
        previewWidget.ratio = ratio;
        updateWidgets();

        for (int i = 0; i < layerButtons.length; i++) {
            ButtonWidget layerButton = layerButtons[i];
            layerButton.setPosition(tabArea.width() - layerButton.getWidth() - 10, tabArea.getTop() + 10 + i * 15);
        }
        int optionsY = tabArea.getTop() + 10 + layerButtons.length * 15 + 5;
        widgetOptions.setPosition(tabArea.width() - widgetOptions.getWidth() - 5, optionsY);
        widgetOptions.setHeight(tabArea.height() - optionsY - 5);
        textWidget.setWidth(tabArea.width());
        textWidget.setPosition(0, tabArea.getBottom() - 9);
        restorePositioning.setPosition(10, tabArea.getBottom() - 25);

        forEachChild(clickableWidget -> clickableWidget.visible = parent.isPreviewVisible() || parent.noHandler);
    }

    private void updatePlayerListFromPreview() {
        if (dungeon) {
            PlayerListMgr.updateDungeons(DungeonsTabPlaceholder.get());
            return;
        }
        if (!parent.isPreviewVisible()) return;
        List<Text> lines = new ArrayList<>();

        // Preview doesn't include any players, so adding this as default
        lines.add(Text.literal("Players (6)"));
        lines.add(Text.literal("[PIG").formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal("+++").formatted(Formatting.AQUA))
                .append(Text.literal("] Technoblade").formatted(Formatting.LIGHT_PURPLE))
        );
        lines.add(Text.literal("Kevinthegreat1"));
        lines.add(Text.literal("AzureAaron"));
        lines.add(Text.literal("LifeIsAParadox"));
        lines.add(Text.literal("Rime"));
        lines.add(Text.literal("Vic is a Cat"));
        lines.add(Text.literal("that's right i "));
        lines.add(Text.literal("don't care about"));
        lines.add(Text.literal("spaces MWAHAHA"));
        lines.add(Text.literal("[MVP--] sixteencharacter"));

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

    void updateWidgets() {
        ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(getCurrentLocation());
        updatePlayerListFromPreview();
        float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100.f;
        screenBuilder.positionWidgets((int) (parent.width / scale), (int) (parent.height / scale));
    }

    void onHudWidgetSelected(@Nullable HudWidget hudWidget) {
        widgetOptions.clearWidgets();
        if (hudWidget == null) return;
        ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(getCurrentLocation());
        PositionRule positionRule = screenBuilder.getPositionRule(hudWidget.getInternalID());
        int width = widgetOptions.getWidth() - widgetOptions.getScrollerWidth();

        // Normal hud widgets don't have auto.
        if (positionRule == null && !(hudWidget instanceof TabHudWidget)) {
            screenBuilder.setPositionRule(hudWidget.getInternalID(), PositionRule.DEFAULT);
            positionRule = PositionRule.DEFAULT;
        }

        // TODO localization

        widgetOptions.addWidget(new TextWidget(width, 9, Text.literal(hudWidget.getNiceName()).formatted(Formatting.BOLD, Formatting.UNDERLINE), client.textRenderer));
        if (positionRule == null) {
            widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Positioning: Auto"), button -> {
                        PositionRule rule = new PositionRule(
                                "screen",
                                PositionRule.Point.DEFAULT,
                                PositionRule.Point.DEFAULT,
                                hudWidget.getX() - 5,
                                hudWidget.getY() - 5,
                                ScreenMaster.ScreenLayer.DEFAULT);
                        screenBuilder.setPositionRule(hudWidget.getInternalID(), rule);
                        updateWidgets();
                        onHudWidgetSelected(hudWidget);
                    })
                    .width(width)
                    .build());
        } else {
            // Normal hud widgets don't have auto.
            if (hudWidget instanceof TabHudWidget) {
                widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Positioning: Custom"), button -> {
                            screenBuilder.setPositionRule(hudWidget.getInternalID(), null);
                            updateWidgets();
                            onHudWidgetSelected(hudWidget);
                        })
                        .width(width)
                        .build());
            }

            String ye = "Layer: " + positionRule.screenLayer().toString();

            widgetOptions.addWidget(ButtonWidget.builder(Text.literal(ye), button -> {
                ScreenBuilder builder = ScreenMaster.getScreenBuilder(getCurrentLocation());
                PositionRule rule = builder.getPositionRuleOrDefault(hudWidget.getInternalID());
                ScreenMaster.ScreenLayer[] values = ScreenMaster.ScreenLayer.values();
                ScreenMaster.ScreenLayer newLayer = values[(rule.screenLayer().ordinal() + 1) % values.length];

                PositionRule newRule = new PositionRule(
                        rule.parent(),
                        rule.parentPoint(),
                        rule.thisPoint(),
                        rule.relativeX(),
                        rule.relativeY(),
                        newLayer
                );
                builder.setPositionRule(hudWidget.getInternalID(), newRule);
                button.setMessage(Text.literal("Layer: " + newRule.screenLayer().toString()));
                updateWidgets();
                if (newLayer != ScreenMaster.ScreenLayer.DEFAULT) {
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

            // apply to all locations
            if (dungeon) return;
            // padding thing
            widgetOptions.addWidget(new ClickableWidget(0, 0, width, 20, Text.empty()) {
                @Override
                protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                }

                @Override
                protected void appendClickableNarrations(NarrationMessageBuilder builder) {
                }
            });
            widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Apply everywhere"), button -> {
                        if (this.previewWidget.selectedWidget == null) return;
                        PositionRule toCopy = ScreenMaster.getScreenBuilder(getCurrentLocation()).getPositionRule(this.previewWidget.selectedWidget.getInternalID());
                        if (toCopy == null) return;
                        for (Location value : Location.values()) {
                            if (value == getCurrentLocation() || value == Location.DUNGEON) continue;
                            ScreenMaster.getScreenBuilder(value).setPositionRule(
                                    this.previewWidget.selectedWidget.getInternalID(),
                                    toCopy
                            );
                        }
                        button.setMessage(Text.literal("Applied!"));
                        Scheduler.INSTANCE.schedule(() -> button.setMessage(Text.literal("Apply everywhere")), 15);
                    }).width(width).tooltip(Tooltip.of(Text.literal("Apply positioning to all locations. This cannot be restored!"))).build()
            );

        }
    }

    private Location getCurrentLocation() {
        return dungeon ? Location.DUNGEON : parent.getCurrentLocation();
    }

    /**
     * The preview widget that captures clicks and displays the current state of the widgets.
     */
    public class PreviewWidget extends ClickableWidget {

        private float ratio = 1f;
        private float scaledRatio = 1f;
        private float scaledScreenWidth = parent.width;
        private float scaledScreenHeight = parent.height;
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
            float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100.f;
            scaledRatio = ratio * scale;
            scaledScreenWidth = parent.width / scale;
            scaledScreenHeight = parent.height / scale;

            ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(getCurrentLocation());
            context.drawBorder(getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, -1);
            context.enableScissor(getX(), getY(), getRight(), getBottom());
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(getX(), getY(), 0f);
            matrices.scale(scaledRatio, scaledRatio, 1f);

            screenBuilder.renderWidgets(context, PreviewTab.this.currentScreenLayer);

            float localMouseX = (mouseX - getX()) / scaledRatio;
            float localMouseY = (mouseY - getY()) / scaledRatio;

            if (selectedWidget != null && selectedWidget.isMouseOver(localMouseX, localMouseY)) {
                hoveredWidget = selectedWidget;
            } else for (HudWidget hudWidget : screenBuilder.getHudWidgets(PreviewTab.this.currentScreenLayer)) {
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

                    int translatedX = Math.min(thisAnchorX - rule.relativeX() - relativeX, (int) scaledScreenWidth - 2);
                    int translatedY = Math.min(thisAnchorY - rule.relativeY() - relativeY, (int) scaledScreenHeight - 2);

                    renderUnits(context, relativeX, rule, thisAnchorX, thisAnchorY, relativeY, translatedX, translatedY);

                    context.drawHorizontalLine(translatedX, thisAnchorX, thisAnchorY + 1, 0xAAAA0000);
                    context.drawVerticalLine(translatedX + 1, translatedY, thisAnchorY, 0xAAAA0000);


                    context.drawHorizontalLine(translatedX, thisAnchorX, thisAnchorY, Colors.RED);
                    context.drawVerticalLine(translatedX, translatedY, thisAnchorY, Colors.RED);
                }
            }

            matrices.pop();
            matrices.push();
            matrices.translate(getX(), getY(), 0.f);
            matrices.scale(ratio, ratio, 1.f);
            ((InGameHudInvoker) MinecraftClient.getInstance().inGameHud).skyblocker$renderSidebar(context, placeHolderObjective);
            matrices.pop();
            context.disableScissor();
        }

        private void renderUnits(DrawContext context, int relativeX, PositionRule rule, int thisAnchorX, int thisAnchorY, int relativeY, int translatedX, int translatedY) {
            boolean xUnitOnTop = rule.relativeY() > 0;
            if (xUnitOnTop && thisAnchorY < 10) xUnitOnTop = false;
            if (!xUnitOnTop && thisAnchorY > scaledScreenHeight - 10) xUnitOnTop = true;

            String yUnitText = String.valueOf(rule.relativeY() + relativeY);
            int yUnitTextWidth = client.textRenderer.getWidth(yUnitText);
            boolean yUnitOnRight = rule.relativeX() > 0;
            if (yUnitOnRight && translatedX + 2 + yUnitTextWidth >= scaledScreenWidth) yUnitOnRight = false;
            if (!yUnitOnRight && translatedX - 2 - yUnitTextWidth <= 0) yUnitOnRight = true;

            // X
            context.drawCenteredTextWithShadow(client.textRenderer, String.valueOf(relativeX + rule.relativeX()), thisAnchorX - (relativeX + rule.relativeX()) / 2, xUnitOnTop ? thisAnchorY - 9 : thisAnchorY + 2, Colors.LIGHT_RED);
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
            double localDeltaX = deltaX / scaledRatio + bufferedDeltaX;
            double localDeltaY = deltaY / scaledRatio + bufferedDeltaY;

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
                ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(getCurrentLocation());
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
            if (!(this.active && this.visible && isMouseOver(mouseX, mouseY))) return false;
            double localMouseX = (mouseX - getX()) / scaledRatio;
            double localMouseY = (mouseY - getY()) / scaledRatio;
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                List<HudWidget> hoveredThingies = new ArrayList<>();
                for (HudWidget hudWidget : ScreenMaster.getScreenBuilder(getCurrentLocation()).getHudWidgets(currentScreenLayer)) {
                    if (hudWidget.isMouseOver(localMouseX, localMouseY)) hoveredThingies.add(hudWidget);
                }
                if (hoveredThingies.size() == 1) selectedWidget = hoveredThingies.getFirst();
                else if (!hoveredThingies.isEmpty()) {
                    for (int i = 0; i < hoveredThingies.size(); i++) {
                        if (hoveredThingies.get(i).equals(hoveredWidget)) {
                            selectedWidget = hoveredThingies.get((i + 1) % hoveredThingies.size());
                        }
                    }
                }
                return true;
            }
            ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(getCurrentLocation());
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



            if (selectedWidget != null && selectedWidget.isMouseOver(localMouseX, localMouseY) &&
                    screenBuilder.getPositionRule(selectedWidget.getInternalID()) != null) {
                selectedOriginalPos = new ScreenPos(selectedWidget.getX(), selectedWidget.getY());
            }
            return true;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (hoveredWidget != null && hoveredWidget.equals(selectedWidget)) {
                int multiplier = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 ? 5 : 1;
                int x = 0, y = 0;
                switch (keyCode) {
                    case GLFW.GLFW_KEY_UP -> y = -multiplier;
                    case GLFW.GLFW_KEY_DOWN -> y = multiplier;
                    case GLFW.GLFW_KEY_LEFT -> x = -multiplier;
                    case GLFW.GLFW_KEY_RIGHT -> x = multiplier;
                }
                ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(getCurrentLocation());
                PositionRule oldRule = screenBuilder.getPositionRuleOrDefault(selectedWidget.getInternalID());

                screenBuilder.setPositionRule(selectedWidget.getInternalID(), new PositionRule(
                        oldRule.parent(),
                        oldRule.parentPoint(),
                        oldRule.thisPoint(),
                        oldRule.relativeX() + x,
                        oldRule.relativeY() + y,
                        oldRule.screenLayer()));
                updateWidgets();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
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
                        PositionRule positionRule = ScreenMaster.getScreenBuilder(getCurrentLocation()).getPositionRule(internalID);
                        if (positionRule != null) {
                            PositionRule.Point point = other ? positionRule.parentPoint() : positionRule.thisPoint();
                            selectedAnchor = point.horizontalPoint().ordinal() == i && point.verticalPoint().ordinal() == j;
                        }
                    }

                    boolean hoveredAnchor = mouseX >= getX() + i * getWidth() / 3 &&
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
            HudWidget affectedWidget = previewWidget.selectedWidget;
            if (hoveredPoint != null && affectedWidget != null) {
                ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(getCurrentLocation());
                String internalID = affectedWidget.getInternalID();
                PositionRule oldRule = screenBuilder.getPositionRuleOrDefault(internalID);
                // Get the x, y of the parent's point
                float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100.f;
                ScreenPos startPos = WidgetPositioner.getStartPosition(oldRule.parent(), (int) (parent.width / scale), (int) (parent.height / scale), other ? hoveredPoint : oldRule.parentPoint());
                if (startPos == null) startPos = new ScreenPos(0, 0);
                // Same but for the affected widget
                PositionRule.Point thisPoint = other ? oldRule.thisPoint() : hoveredPoint;
                ScreenPos endPos = new ScreenPos(
                        (int) (affectedWidget.getX() + thisPoint.horizontalPoint().getPercentage() * affectedWidget.getWidth()),
                        (int) (affectedWidget.getY() + thisPoint.verticalPoint().getPercentage() * affectedWidget.getHeight())
                );

                if (other) {
                    screenBuilder.setPositionRule(internalID, new PositionRule(
                            oldRule.parent(),
                            hoveredPoint,
                            oldRule.thisPoint(),
                            endPos.x() - startPos.x(),
                            endPos.y() - startPos.y(),
                            oldRule.screenLayer()));
                } else {
                    screenBuilder.setPositionRule(internalID, new PositionRule(
                            oldRule.parent(),
                            oldRule.parentPoint(),
                            hoveredPoint,
                            endPos.x() - startPos.x(),
                            endPos.y() - startPos.y(),
                            oldRule.screenLayer()));
                }
            }
            updateWidgets();
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        }
    }
}
