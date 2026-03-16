package de.hysky.skyblocker.skyblock.fancybars;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanMutablePair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.Window;
import de.hysky.skyblocker.skyblock.fancybars.BarPositioner.BarLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class StatusBarsConfigScreen extends Screen {
        private static final Identifier HOTBAR_TEXTURE = Identifier.withDefaultNamespace("hud/hotbar");
        private static final int HOTBAR_WIDTH = 182;
        private static final float RESIZE_THRESHOLD = 0.75f;
        private static final int BAR_MINIMUM_WIDTH = 30;
        private static final int DRAG_THRESHOLD = 5;

        private final Map<ScreenRectangle, Pair<StatusBar, BarLocation>> rectToBar = new HashMap<>();
        /**
         * Contains the hovered bar and a boolean that is true if hovering the right side or false otherwise.
         */
        private final ObjectBooleanPair<@Nullable StatusBar> resizeHover = new ObjectBooleanMutablePair<>(null, false);
        private final Pair<@Nullable StatusBar, @Nullable StatusBar> resizedBars = ObjectObjectMutablePair.of(null, null);

        private @Nullable StatusBar cursorBar = null;
        private @Nullable StatusBar selectedBar = null;
        private ScreenPosition cursorOffset = new ScreenPosition(0, 0);

        private boolean resizing = false;
        private boolean mouseButtonHeld = false;
        private int dragStartX = 0;
        private int dragStartY = 0;
        private EditBarWidget editBarWidget;

        public StatusBarsConfigScreen() {
                super(Component.nullToEmpty("Status Bars Config"));
        }


        private void startDrag(StatusBar statusBar) {
                cursorBar = statusBar;
                cursorBar.inMouse = true;
                cursorBar.enabled = true;
                // Capture rendered pixel width as normalized width so bar stays same size when free-floating
                if (statusBar.getWidth() > 0) {
                        statusBar.width = (float) statusBar.getWidth() / this.width;
                }
                if (statusBar.anchor != null)
                        FancyStatusBars.barPositioner.removeBar(statusBar.anchor, statusBar.gridY, statusBar);
                statusBar.anchor = null;
                FancyStatusBars.updatePositions(true);
                cursorBar.setX(width + 5);
                updateScreenRects();
                editBarWidget.visible = false;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
                super.render(context, mouseX, mouseY, delta);

                if (mouseButtonHeld && selectedBar != null && cursorBar == null && !resizing) {
                        double dx = mouseX - dragStartX;
                        double dy = mouseY - dragStartY;
                        if (dx * dx + dy * dy > (double) DRAG_THRESHOLD * DRAG_THRESHOLD) {
                                startDrag(selectedBar);
                        }
                }

                context.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE, width / 2 - HOTBAR_WIDTH / 2, height - 22, HOTBAR_WIDTH, 22);
                editBarWidget.render(context, mouseX, mouseY, delta);

                Window window = minecraft.getWindow();
                int scaleFactor = window.calculateScale(0, minecraft.isEnforceUnicode()) - window.getGuiScale() + 3;
                if ((scaleFactor & 2) == 0) scaleFactor++;

                ScreenRectangle mouseRect = new ScreenRectangle(new ScreenPosition(mouseX - scaleFactor / 2, mouseY - scaleFactor / 2), scaleFactor, scaleFactor);

                // Draw selection outline around selected bar (left-click)
                if (selectedBar != null && cursorBar == null && selectedBar.enabled) {
                        int sx = selectedBar.getX() - 1;
                        int sy = selectedBar.getY() - 1;
                        int sw = selectedBar.getWidth() + 2;
                        int sh = selectedBar.getHeight() + 2;
                        context.fill(sx, sy, sx + sw, sy + 1, 0xFFFFFF55);
                        context.fill(sx, sy + sh - 1, sx + sw, sy + sh, 0xFFFFFF55);
                        context.fill(sx, sy, sx + 1, sy + sh, 0xFFFFFF55);
                        context.fill(sx + sw - 1, sy, sx + sw, sy + sh, 0xFFFFFF55);
                }

                if (cursorBar != null) {
                        cursorBar.renderCursor(context, mouseX + cursorOffset.x(), mouseY + cursorOffset.y(), delta);
                } else { // Not dragging around a bar
                        if (resizing) { // actively resizing one or 2 bars
                                int middleX; // the point between the 2 bars

                                StatusBar rightBar = resizedBars.right();
                                StatusBar leftBar = resizedBars.left();
                                boolean hasRight = rightBar != null;
                                boolean hasLeft = leftBar != null;
                                BarPositioner.BarAnchor barAnchor;
                                if (!hasRight) {
                                        barAnchor = leftBar.anchor;
                                        middleX = leftBar.getX() + leftBar.getWidth();
                                } else {
                                        barAnchor = rightBar.anchor;
                                        middleX = rightBar.getX();
                                }

                                if (barAnchor != null) { // If is on an anchor
                                        BarPositioner.SizeRule sizeRule = barAnchor.getSizeRule();
                                        boolean doResize = true;

                                        float widthPerSize;
                                        if (sizeRule.isTargetSize())
                                                widthPerSize = (float) sizeRule.totalWidth() / sizeRule.targetSize();
                                        else
                                                widthPerSize = sizeRule.widthPerSize();

                                        // resize towards the left
                                        if (mouseX < middleX) {
                                                if (middleX - mouseX > widthPerSize / RESIZE_THRESHOLD) {
                                                        if (hasRight) {
                                                                if (rightBar.size + 1 > sizeRule.maxSize()) doResize = false;
                                                        }
                                                        if (hasLeft) {
                                                                if (leftBar.size - 1 < sizeRule.minSize()) doResize = false;
                                                        }

                                                        if (doResize) {
                                                                if (hasRight) rightBar.size++;
                                                                if (hasLeft) leftBar.size--;
                                                                FancyStatusBars.updatePositions(true);
                                                        }
                                                }
                                        } else { // towards the right
                                                if (mouseX - middleX > widthPerSize / RESIZE_THRESHOLD) {
                                                        if (hasRight) {
                                                                if (rightBar.size - 1 < sizeRule.minSize()) doResize = false;
                                                        }
                                                        if (hasLeft) {
                                                                if (leftBar.size + 1 > sizeRule.maxSize()) doResize = false;
                                                        }

                                                        if (doResize) {
                                                                if (hasRight) rightBar.size--;
                                                                if (hasLeft) leftBar.size++;
                                                                FancyStatusBars.updatePositions(true);
                                                        }
                                                }
                                        }
                                } else { // Freely moving around
                                        if (hasLeft) {
                                                leftBar.setWidth(Math.max(BAR_MINIMUM_WIDTH, mouseX - leftBar.getX()));
                                        } else if (hasRight) {
                                                int endX = rightBar.getX() + rightBar.getWidth();
                                                rightBar.setX(Math.min(endX - BAR_MINIMUM_WIDTH, mouseX));
                                                rightBar.setWidth(endX - rightBar.getX());
                                        }
                                }

                        } else { // hovering bars
                                rectLoop:
                                for (ScreenRectangle screenRect : rectToBar.keySet()) {
                                        for (ScreenDirection direction : new ScreenDirection[]{ScreenDirection.LEFT, ScreenDirection.RIGHT}) {
                                                boolean overlaps = screenRect.getBorder(direction).step(direction).overlaps(mouseRect);

                                                if (overlaps && !editBarWidget.isMouseOver(mouseX, mouseY)) {
                                                        Pair<StatusBar, BarLocation> barPair = rectToBar.get(screenRect);
                                                        BarLocation barLocation = barPair.right();
                                                        StatusBar bar = barPair.left();
                                                        if (!bar.enabled) break;
                                                        boolean right = direction.equals(ScreenDirection.RIGHT);
                                                        if (barLocation.barAnchor() != null) {
                                                                if (barLocation.barAnchor().getSizeRule().isTargetSize() && !FancyStatusBars.barPositioner.hasNeighbor(barLocation.barAnchor(), barLocation.y(), barLocation.x(), right)) {
                                                                        break;
                                                                }
                                                                if (!barLocation.barAnchor().getSizeRule().isTargetSize() && barLocation.x() == 0 && barLocation.barAnchor().isRight() != right)
                                                                        break;
                                                        }
                                                        resizeHover.first(bar);
                                                        resizeHover.right(right);
                                                        context.requestCursor(CursorTypes.RESIZE_EW);
                                                        break rectLoop;
                                                } else {
                                                        resizeHover.first(null);
                                                }
                                        }
                                }
                        }
                }
        }


        @Override
        protected void init() {
                super.init();
                FancyStatusBars.updatePositions(true);
                editBarWidget = new EditBarWidget(0, 0, this);
                editBarWidget.visible = false;
                addWidget(editBarWidget); // rendering separately to have it above hotbar
                Collection<StatusBar> values = FancyStatusBars.statusBars.values();
                values.forEach(this::setup);
                updateScreenRects();
                this.addRenderableWidget(Button.builder(Component.literal("?"),
                                                button -> minecraft.setScreen(new PopupScreen.Builder(this, Component.translatable("skyblocker.bars.config.explanationTitle"))
                                                                .addButton(Component.translatable("gui.ok"), PopupScreen::onClose)
                                                                .setMessage(Component.translatable("skyblocker.bars.config.explanation"))
                                                                .build()))
                                .bounds(width - 20, (height - 15) / 2, 15, 15)
                                .build());
                this.addRenderableWidget(Button.builder(Component.translatable("skyblocker.bars.config.resetToDefault"),
                                                button -> {
                                                        FancyStatusBars.resetToDefaults();
                                                        selectedBar = null;
                                                        editBarWidget.visible = false;
                                                        updateScreenRects();
                                                })
                                .bounds(5, 5, 110, 14)
                                .build());
        }

        private void setup(StatusBar statusBar) {
                this.addRenderableWidget(statusBar);
                statusBar.setOnClick(this::onBarClick);
        }

        @Override
        public void removed() {
                super.removed();
                FancyStatusBars.statusBars.values().forEach(statusBar -> statusBar.setOnClick(null));
                if (cursorBar != null) cursorBar.inMouse = false;
                FancyStatusBars.updatePositions(false);
                FancyStatusBars.saveBarConfig();
        }

        @Override
        public boolean isPauseScreen() {
                return false;
        }

        private void onBarClick(StatusBar statusBar, MouseButtonEvent click) {
                if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        // Left click: select bar (shows outline) and prepare for possible drag
                        selectedBar = statusBar;
                        mouseButtonHeld = true;
                        dragStartX = (int) click.x();
                        dragStartY = (int) click.y();
                        cursorOffset = new ScreenPosition((int) (statusBar.getX() - click.x()), (int) (statusBar.getY() - click.y()));
                        editBarWidget.visible = false;
                } else if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        // Right click: show the customization panel, don't interfere with dragging
                        selectedBar = statusBar;
                        int x = (int) Math.min(click.x() + 5, width - editBarWidget.getWidth());
                        int y = (int) Math.min(click.y() + 5, height - editBarWidget.getHeight());
                        // Prime the auto-hide tracker so the widget doesn't vanish immediately
                        editBarWidget.insideMouseX = (int) click.x();
                        editBarWidget.insideMouseY = (int) click.y();
                        editBarWidget.visible = true;
                        editBarWidget.setStatusBar(statusBar);
                        editBarWidget.setX(x);
                        editBarWidget.setY(y);
                }
        }

        private void updateScreenRects() {
                rectToBar.clear();
                FancyStatusBars.statusBars.values().forEach(statusBar1 -> {
                        if (!statusBar1.enabled) return;
                        rectToBar.put(
                                        new ScreenRectangle(new ScreenPosition(statusBar1.getX(), statusBar1.getY()), statusBar1.getWidth(), statusBar1.getHeight()),
                                        Pair.of(statusBar1, BarLocation.of(statusBar1)));
                });
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent click) {
                mouseButtonHeld = false;
                if (cursorBar != null) {
                        cursorBar.inMouse = false;
                        cursorBar.anchor = null;
                        cursorBar.x = (float) ((click.x() + cursorOffset.x()) / width);
                        cursorBar.y = (float) ((click.y() + cursorOffset.y()) / height);
                        cursorBar.width = Math.clamp(cursorBar.width, (float) BAR_MINIMUM_WIDTH / width, 1);
                        cursorBar = null;
                        FancyStatusBars.updatePositions(true);
                        updateScreenRects();
                        return true;
                } else if (resizing) {
                        resizing = false;

                        // update x and width if bar has no anchor
                        StatusBar bar = null;
                        if (resizedBars.left() != null) bar = resizedBars.left();
                        else if (resizedBars.right() != null) bar = resizedBars.right();
                        if (bar != null && bar.anchor == null) {
                                bar.x = (float) bar.getX() / width;
                                bar.width = (float) bar.getWidth() / width;
                        }
                        resizedBars.left(null);
                        resizedBars.right(null);
                        updateScreenRects();
                        return true;
                }
                return super.mouseReleased(click);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
                StatusBar first = resizeHover.first();
                // want the right click thing to have priority
                if (!editBarWidget.isMouseOver(click.x(), click.y()) && click.button() == 0 && first != null) {
                        BarPositioner.BarAnchor barAnchor = first.anchor;
                        if (barAnchor != null) {
                                if (resizeHover.rightBoolean()) {
                                        resizedBars.left(first);

                                        if (FancyStatusBars.barPositioner.hasNeighbor(barAnchor, first.gridY, first.gridX, true)) {
                                                resizedBars.right(FancyStatusBars.barPositioner.getBar(barAnchor, first.gridY, first.gridX + (barAnchor.isRight() ? 1 : -1)));
                                        } else resizedBars.right(null);
                                } else {
                                        resizedBars.right(first);

                                        if (FancyStatusBars.barPositioner.hasNeighbor(barAnchor, first.gridY, first.gridX, false)) {
                                                resizedBars.left(FancyStatusBars.barPositioner.getBar(barAnchor, first.gridY, first.gridX + (barAnchor.isRight() ? -1 : 1)));
                                        } else resizedBars.left(null);
                                }
                        } else { // if they have no anchor no need to do any checking
                                if (resizeHover.rightBoolean()) {
                                        resizedBars.left(first);
                                        resizedBars.right(null);
                                } else {
                                        resizedBars.right(first);
                                        resizedBars.left(null);
                                }
                        }
                        resizing = true;
                        return true;
                }
                boolean handled = super.mouseClicked(click, doubled);
                if (!handled && !editBarWidget.isMouseOver(click.x(), click.y())) {
                        selectedBar = null;
                        editBarWidget.visible = false;
                }
                return handled;
        }
}
