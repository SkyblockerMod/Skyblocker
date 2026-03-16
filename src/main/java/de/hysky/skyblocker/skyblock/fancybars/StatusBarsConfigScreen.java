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
        private static final int EDGE_TOLERANCE = 5;

        /** Cyan: CUSTOM element handles */
        private static final int HANDLE_COLOR  = 0xFF55FFFF;
        /** Yellow: selected bar outline */
        private static final int BAR_SEL_COLOR = 0xFFFFFF55;

        private final Map<ScreenRectangle, Pair<StatusBar, BarLocation>> rectToBar = new HashMap<>();
        /** Hovered bar + boolean: true = right edge, false = left edge */
        private final ObjectBooleanPair<@Nullable StatusBar> resizeHover = new ObjectBooleanMutablePair<>(null, false);
        /** Hovered bar + boolean: true = top edge, false = bottom edge (height resize) */
        private final ObjectBooleanPair<@Nullable StatusBar> resizeHeightHover = new ObjectBooleanMutablePair<>(null, false);
        private final Pair<@Nullable StatusBar, @Nullable StatusBar> resizedBars = ObjectObjectMutablePair.of(null, null);

        private @Nullable StatusBar cursorBar = null;
        private @Nullable StatusBar selectedBar = null;
        private ScreenPosition cursorOffset = new ScreenPosition(0, 0);

        private boolean resizing = false;
        private boolean mouseButtonHeld = false;
        private int dragStartX = 0;
        private int dragStartY = 0;

        // ── Height resize (bars) ──
        private boolean resizingHeight = false;
        private boolean heightResizeFromTop = false;
        private @Nullable StatusBar heightResizeBar = null;
        private int heightResizeInitialY = 0;
        private int heightResizeInitialHeight = 0;

        // ── CUSTOM sub-element move drag ──
        private boolean draggingSubElement = false;
        private boolean draggingText = false;
        private int subDragStartMouseX = 0;
        private int subDragStartMouseY = 0;
        private int subDragStartOffX = 0;
        private int subDragStartOffY = 0;

        // ── CUSTOM sub-element resize ──
        private enum SubElementEdge { NONE, TEXT_RIGHT, ICON_RIGHT, ICON_BOTTOM }
        private SubElementEdge subElementEdgeHover = SubElementEdge.NONE;
        private @Nullable StatusBar subElementEdgeBar = null;

        private boolean resizingSubElement = false;
        private boolean resizeSubIsText = false;
        private boolean resizeSubIsHoriz = true;
        private @Nullable StatusBar resizeSubBar = null;
        private int resizeSubStartMouse = 0;
        private float resizeSubStartScale = 1.0f;
        private int resizeSubStartPx = 0;

        private EditBarWidget editBarWidget;

        public StatusBarsConfigScreen() {
                super(Component.nullToEmpty("Status Bars Config"));
        }

        private void startDrag(StatusBar statusBar) {
                cursorBar = statusBar;
                cursorBar.inMouse = true;
                cursorBar.enabled = true;
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

                // Sub-element MOVE drag
                if (draggingSubElement && selectedBar != null && mouseButtonHeld) {
                        int dx = mouseX - subDragStartMouseX;
                        int dy = mouseY - subDragStartMouseY;
                        if (draggingText) {
                                selectedBar.textCustomOffX = subDragStartOffX + dx;
                                selectedBar.textCustomOffY = subDragStartOffY + dy;
                        } else {
                                selectedBar.iconCustomOffX = subDragStartOffX + dx;
                                selectedBar.iconCustomOffY = subDragStartOffY + dy;
                        }
                }

                // Sub-element RESIZE drag
                if (resizingSubElement && resizeSubBar != null && mouseButtonHeld) {
                        if (resizeSubIsText) {
                                // Right edge → scale (proportional to mouse movement)
                                int delta_ = mouseX - resizeSubStartMouse;
                                float newScale = Math.max(0.5f, Math.min(4.0f, resizeSubStartScale + delta_ * 0.02f));
                                resizeSubBar.textCustomScale = newScale;
                        } else if (resizeSubIsHoriz) {
                                // Icon right edge → width
                                int delta_ = mouseX - resizeSubStartMouse;
                                resizeSubBar.iconCustomW = Math.max(4, Math.min(64, resizeSubStartPx + delta_));
                        } else {
                                // Icon bottom edge → height
                                int delta_ = mouseY - resizeSubStartMouse;
                                resizeSubBar.iconCustomH = Math.max(4, Math.min(64, resizeSubStartPx + delta_));
                        }
                }

                // Bar drag threshold check
                if (mouseButtonHeld && selectedBar != null && cursorBar == null && !resizing
                                && !resizingHeight && !draggingSubElement && !resizingSubElement) {
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

                // Draw selection outline + handles for selected bar
                if (selectedBar != null && cursorBar == null && selectedBar.enabled) {
                        // Bar outline with arrows (yellow)
                        drawBarOutlineWithArrows(context, selectedBar);

                        // CUSTOM text handles at EXACT text size (not padded)
                        if (selectedBar.getTextPosition() == StatusBar.TextPosition.CUSTOM) {
                                ScreenRectangle vis = selectedBar.getTextVisualArea(minecraft.font);
                                drawCustomElementHandles(context, vis, true);
                        }
                        // CUSTOM icon handles at EXACT icon size
                        if (selectedBar.getIconPosition() == StatusBar.IconPosition.CUSTOM) {
                                ScreenRectangle vis = selectedBar.getIconVisualArea();
                                drawCustomElementHandles(context, vis, false);
                        }
                }

                if (cursorBar != null) {
                        cursorBar.renderCursor(context, mouseX + cursorOffset.x(), mouseY + cursorOffset.y(), delta);
                } else {
                        if (resizing) {
                                int middleX;
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

                                if (barAnchor != null) {
                                        BarPositioner.SizeRule sizeRule = barAnchor.getSizeRule();
                                        boolean doResize = true;
                                        float widthPerSize;
                                        if (sizeRule.isTargetSize()) widthPerSize = (float) sizeRule.totalWidth() / sizeRule.targetSize();
                                        else widthPerSize = sizeRule.widthPerSize();

                                        if (mouseX < middleX) {
                                                if (middleX - mouseX > widthPerSize / RESIZE_THRESHOLD) {
                                                        if (hasRight && rightBar.size + 1 > sizeRule.maxSize()) doResize = false;
                                                        if (hasLeft && leftBar.size - 1 < sizeRule.minSize()) doResize = false;
                                                        if (doResize) {
                                                                if (hasRight) rightBar.size++;
                                                                if (hasLeft) leftBar.size--;
                                                                FancyStatusBars.updatePositions(true);
                                                        }
                                                }
                                        } else {
                                                if (mouseX - middleX > widthPerSize / RESIZE_THRESHOLD) {
                                                        if (hasRight && rightBar.size - 1 < sizeRule.minSize()) doResize = false;
                                                        if (hasLeft && leftBar.size + 1 > sizeRule.maxSize()) doResize = false;
                                                        if (doResize) {
                                                                if (hasRight) rightBar.size--;
                                                                if (hasLeft) leftBar.size++;
                                                                FancyStatusBars.updatePositions(true);
                                                        }
                                                }
                                        }
                                } else {
                                        if (hasLeft) {
                                                leftBar.setWidth(Math.max(BAR_MINIMUM_WIDTH, mouseX - leftBar.getX()));
                                        } else if (hasRight) {
                                                int endX = rightBar.getX() + rightBar.getWidth();
                                                rightBar.setX(Math.min(endX - BAR_MINIMUM_WIDTH, mouseX));
                                                rightBar.setWidth(endX - rightBar.getX());
                                        }
                                }

                        } else if (resizingHeight && heightResizeBar != null) {
                                if (heightResizeFromTop) {
                                        int bottom = heightResizeInitialY + heightResizeInitialHeight;
                                        int newTop = Math.min(bottom - StatusBar.MIN_BAR_HEIGHT, mouseY);
                                        heightResizeBar.setY(newTop);
                                        heightResizeBar.barHeight = bottom - newTop;
                                        heightResizeBar.y = (float) newTop / height;
                                } else {
                                        heightResizeBar.barHeight = Math.max(StatusBar.MIN_BAR_HEIGHT, mouseY - heightResizeBar.getY());
                                }

                        } else if (resizingSubElement) {
                                // cursor shown dynamically during drag
                                context.requestCursor(resizeSubIsHoriz ? CursorTypes.RESIZE_EW : CursorTypes.RESIZE_NS);

                        } else {
                                // Hover: sub-element resize edges (highest priority)
                                subElementEdgeHover = SubElementEdge.NONE;
                                subElementEdgeBar = null;
                                if (selectedBar != null && selectedBar.enabled) {
                                        if (selectedBar.getTextPosition() == StatusBar.TextPosition.CUSTOM) {
                                                ScreenRectangle vis = selectedBar.getTextVisualArea(minecraft.font);
                                                int rx = vis.position().x() + vis.width();
                                                int ly = vis.position().y();
                                                int by_ = ly + vis.height();
                                                boolean onRight = Math.abs(mouseX - rx) <= EDGE_TOLERANCE && mouseY >= ly && mouseY <= by_;
                                                if (onRight) {
                                                        subElementEdgeHover = SubElementEdge.TEXT_RIGHT;
                                                        subElementEdgeBar = selectedBar;
                                                        context.requestCursor(CursorTypes.RESIZE_EW);
                                                }
                                        }
                                        if (subElementEdgeHover == SubElementEdge.NONE && selectedBar.getIconPosition() == StatusBar.IconPosition.CUSTOM) {
                                                ScreenRectangle vis = selectedBar.getIconVisualArea();
                                                int rx = vis.position().x() + vis.width();
                                                int byx = vis.position().y() + vis.height();
                                                int lx = vis.position().x();
                                                int ly = vis.position().y();
                                                boolean onRight = Math.abs(mouseX - rx) <= EDGE_TOLERANCE && mouseY >= ly && mouseY <= byx;
                                                boolean onBottom = Math.abs(mouseY - byx) <= EDGE_TOLERANCE && mouseX >= lx && mouseX <= rx;
                                                if (onRight) {
                                                        subElementEdgeHover = SubElementEdge.ICON_RIGHT;
                                                        subElementEdgeBar = selectedBar;
                                                        context.requestCursor(CursorTypes.RESIZE_EW);
                                                } else if (onBottom) {
                                                        subElementEdgeHover = SubElementEdge.ICON_BOTTOM;
                                                        subElementEdgeBar = selectedBar;
                                                        context.requestCursor(CursorTypes.RESIZE_NS);
                                                }
                                        }
                                }

                                if (subElementEdgeHover == SubElementEdge.NONE) {
                                        // Bar horizontal resize
                                        boolean foundHorizontal = false;
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
                                                                        if (barLocation.barAnchor().getSizeRule().isTargetSize() && !FancyStatusBars.barPositioner.hasNeighbor(barLocation.barAnchor(), barLocation.y(), barLocation.x(), right))
                                                                                break;
                                                                        if (!barLocation.barAnchor().getSizeRule().isTargetSize() && barLocation.x() == 0 && barLocation.barAnchor().isRight() != right)
                                                                                break;
                                                                }
                                                                resizeHover.first(bar);
                                                                resizeHover.right(right);
                                                                resizeHeightHover.first(null);
                                                                foundHorizontal = true;
                                                                context.requestCursor(CursorTypes.RESIZE_EW);
                                                                break rectLoop;
                                                        } else {
                                                                resizeHover.first(null);
                                                        }
                                                }
                                        }

                                        if (!foundHorizontal) {
                                                resizeHover.first(null);
                                                heightLoop:
                                                for (ScreenRectangle screenRect : rectToBar.keySet()) {
                                                        Pair<StatusBar, BarLocation> barPair = rectToBar.get(screenRect);
                                                        StatusBar bar = barPair.left();
                                                        if (!bar.enabled || bar.anchor != null) continue;
                                                        for (ScreenDirection direction : new ScreenDirection[]{ScreenDirection.UP, ScreenDirection.DOWN}) {
                                                                boolean overlaps = screenRect.getBorder(direction).step(direction).overlaps(mouseRect);
                                                                if (overlaps && !editBarWidget.isMouseOver(mouseX, mouseY)) {
                                                                        boolean isTop = direction.equals(ScreenDirection.UP);
                                                                        resizeHeightHover.first(bar);
                                                                        resizeHeightHover.right(isTop);
                                                                        context.requestCursor(CursorTypes.RESIZE_NS);
                                                                        break heightLoop;
                                                                } else {
                                                                        resizeHeightHover.first(null);
                                                                }
                                                        }
                                                }
                                        } else {
                                                resizeHeightHover.first(null);
                                        }
                                }
                        }
                }
        }

        // ─────────────────────── Outline / arrow drawing ───────────────────────

        /** Draws the yellow selection outline + directional move arrows around a bar. */
        private void drawBarOutlineWithArrows(GuiGraphics ctx, StatusBar bar) {
                int x = bar.getX() - 1;
                int y = bar.getY() - 1;
                int w = bar.getWidth() + 2;
                int h = bar.getHeight() + 2;
                // 2px thick yellow border
                ctx.fill(x, y, x + w, y + 2, BAR_SEL_COLOR);
                ctx.fill(x, y + h - 2, x + w, y + h, BAR_SEL_COLOR);
                ctx.fill(x, y, x + 2, y + h, BAR_SEL_COLOR);
                ctx.fill(x + w - 2, y, x + w, y + h, BAR_SEL_COLOR);
                // Arrows pointing outward at each edge midpoint
                int mx = x + w / 2;
                int my = y + h / 2;
                fillLeftArrow(ctx, x - 7, my, BAR_SEL_COLOR);
                fillRightArrow(ctx, x + w + 6, my, BAR_SEL_COLOR);
                fillUpArrow(ctx, mx, y - 7, BAR_SEL_COLOR);
                fillDownArrow(ctx, mx, y + h + 6, BAR_SEL_COLOR);
        }

        /**
         * Draws a 2px border + outward arrows for a CUSTOM text or icon element.
         * Also draws resize handles (brighter squares) at the right and, for icons, bottom edge.
         * @param isText true = text element (only right-edge resize), false = icon (right + bottom resize)
         */
        private void drawCustomElementHandles(GuiGraphics ctx, ScreenRectangle area, boolean isText) {
                int x = area.position().x();
                int y = area.position().y();
                int w = area.width();
                int h = area.height();

                // 2px thick cyan border
                ctx.fill(x, y, x + w, y + 2, HANDLE_COLOR);
                ctx.fill(x, y + h - 2, x + w, y + h, HANDLE_COLOR);
                ctx.fill(x, y, x + 2, y + h, HANDLE_COLOR);
                ctx.fill(x + w - 2, y, x + w, y + h, HANDLE_COLOR);

                // Move arrows pointing outward
                int mx = x + w / 2;
                int my = y + h / 2;
                fillLeftArrow(ctx, x - 7, my, HANDLE_COLOR);
                fillRightArrow(ctx, x + w + 6, my, HANDLE_COLOR);
                fillUpArrow(ctx, mx, y - 7, HANDLE_COLOR);
                fillDownArrow(ctx, mx, y + h + 6, HANDLE_COLOR);

                // Resize handle: right edge midpoint (white square = resize W/scale)
                int rhx = x + w + 2, rhy = my - 3;
                ctx.fill(rhx, rhy, rhx + 5, rhy + 6, 0xFFFFFFFF);

                // Resize handle: bottom edge midpoint (white square = resize H, icon only)
                if (!isText) {
                        int rbx = mx - 3, rby = y + h + 2;
                        ctx.fill(rbx, rby, rbx + 6, rby + 5, 0xFFFFFFFF);
                }
        }

        private static void fillLeftArrow(GuiGraphics ctx, int tipX, int midY, int color) {
                ctx.fill(tipX,     midY,     tipX + 1, midY + 1, color);
                ctx.fill(tipX + 1, midY - 1, tipX + 2, midY + 2, color);
                ctx.fill(tipX + 2, midY - 2, tipX + 5, midY + 3, color);
        }

        private static void fillRightArrow(GuiGraphics ctx, int tipX, int midY, int color) {
                ctx.fill(tipX,     midY,     tipX + 1, midY + 1, color);
                ctx.fill(tipX - 1, midY - 1, tipX,     midY + 2, color);
                ctx.fill(tipX - 4, midY - 2, tipX - 1, midY + 3, color);
        }

        private static void fillUpArrow(GuiGraphics ctx, int midX, int tipY, int color) {
                ctx.fill(midX,     tipY,     midX + 1, tipY + 1, color);
                ctx.fill(midX - 1, tipY + 1, midX + 2, tipY + 2, color);
                ctx.fill(midX - 2, tipY + 2, midX + 3, tipY + 5, color);
        }

        private static void fillDownArrow(GuiGraphics ctx, int midX, int tipY, int color) {
                ctx.fill(midX,     tipY,     midX + 1, tipY + 1, color);
                ctx.fill(midX - 1, tipY - 1, midX + 2, tipY,     color);
                ctx.fill(midX - 2, tipY - 4, midX + 3, tipY - 1, color);
        }

        // ─────────────────────── Screen lifecycle ───────────────────────

        @Override
        protected void init() {
                super.init();
                FancyStatusBars.updatePositions(true);
                editBarWidget = new EditBarWidget(0, 0, this);
                editBarWidget.visible = false;
                addWidget(editBarWidget);
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
                        int cx = (int) click.x();
                        int cy = (int) click.y();

                        // If already selected, try sub-element edge or move
                        if (selectedBar == statusBar && statusBar.enabled) {
                                if (tryStartSubElementResize(statusBar, cx, cy)) return;
                                if (tryStartSubElementDrag(statusBar, cx, cy)) return;
                        }

                        selectedBar = statusBar;
                        mouseButtonHeld = true;
                        dragStartX = cx;
                        dragStartY = cy;
                        cursorOffset = new ScreenPosition((int) (statusBar.getX() - click.x()), (int) (statusBar.getY() - click.y()));
                        editBarWidget.visible = false;
                } else if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        selectedBar = statusBar;
                        int x = (int) Math.min(click.x() + 5, width - editBarWidget.getWidth());
                        int y = (int) Math.min(click.y() + 5, height - editBarWidget.getHeight());
                        editBarWidget.insideMouseX = (int) click.x();
                        editBarWidget.insideMouseY = (int) click.y();
                        editBarWidget.visible = true;
                        editBarWidget.setStatusBar(statusBar);
                        editBarWidget.setX(x);
                        editBarWidget.setY(y);
                }
        }

        /** Tries to start a sub-element RESIZE at (cx, cy). Returns true if started. */
        private boolean tryStartSubElementResize(StatusBar bar, int cx, int cy) {
                if (subElementEdgeHover != SubElementEdge.NONE && subElementEdgeBar == bar) {
                        resizingSubElement = true;
                        resizeSubBar = bar;
                        mouseButtonHeld = true;
                        editBarWidget.visible = false;
                        switch (subElementEdgeHover) {
                                case TEXT_RIGHT -> {
                                        resizeSubIsText = true;
                                        resizeSubIsHoriz = true;
                                        resizeSubStartMouse = cx;
                                        resizeSubStartScale = bar.textCustomScale;
                                }
                                case ICON_RIGHT -> {
                                        resizeSubIsText = false;
                                        resizeSubIsHoriz = true;
                                        resizeSubStartMouse = cx;
                                        resizeSubStartPx = bar.iconCustomW;
                                }
                                case ICON_BOTTOM -> {
                                        resizeSubIsText = false;
                                        resizeSubIsHoriz = false;
                                        resizeSubStartMouse = cy;
                                        resizeSubStartPx = bar.iconCustomH;
                                }
                                default -> {
                                        resizingSubElement = false;
                                        return false;
                                }
                        }
                        return true;
                }
                return false;
        }

        /** Tries to start a sub-element MOVE drag at (cx, cy). Returns true if started. */
        private boolean tryStartSubElementDrag(StatusBar bar, int cx, int cy) {
                ScreenRectangle clickRect = new ScreenRectangle(cx, cy, 1, 1);
                if (bar.getTextPosition() == StatusBar.TextPosition.CUSTOM) {
                        if (bar.getTextHitArea(minecraft.font).overlaps(clickRect)) {
                                draggingSubElement = true;
                                draggingText = true;
                                mouseButtonHeld = true;
                                subDragStartMouseX = cx;
                                subDragStartMouseY = cy;
                                subDragStartOffX = bar.textCustomOffX;
                                subDragStartOffY = bar.textCustomOffY;
                                editBarWidget.visible = false;
                                return true;
                        }
                }
                if (bar.getIconPosition() == StatusBar.IconPosition.CUSTOM) {
                        if (bar.getIconHitArea().overlaps(clickRect)) {
                                draggingSubElement = true;
                                draggingText = false;
                                mouseButtonHeld = true;
                                subDragStartMouseX = cx;
                                subDragStartMouseY = cy;
                                subDragStartOffX = bar.iconCustomOffX;
                                subDragStartOffY = bar.iconCustomOffY;
                                editBarWidget.visible = false;
                                return true;
                        }
                }
                return false;
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

                if (resizingSubElement) {
                        resizingSubElement = false;
                        resizeSubBar = null;
                        return true;
                }
                if (draggingSubElement) {
                        draggingSubElement = false;
                        return true;
                }

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
                } else if (resizingHeight) {
                        resizingHeight = false;
                        if (heightResizeBar != null) {
                                heightResizeBar.y = (float) heightResizeBar.getY() / height;
                                heightResizeBar = null;
                        }
                        updateScreenRects();
                        return true;
                }
                return super.mouseReleased(click);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
                // Sub-element edge resize (highest priority)
                if (click.button() == 0 && subElementEdgeHover != SubElementEdge.NONE && subElementEdgeBar != null) {
                        if (tryStartSubElementResize(subElementEdgeBar, (int) click.x(), (int) click.y())) return true;
                }

                // Height resize click
                StatusBar heightBar = resizeHeightHover.first();
                if (!editBarWidget.isMouseOver(click.x(), click.y()) && click.button() == 0 && heightBar != null) {
                        resizingHeight = true;
                        heightResizeFromTop = resizeHeightHover.rightBoolean();
                        heightResizeBar = heightBar;
                        heightResizeInitialY = heightBar.getY();
                        heightResizeInitialHeight = heightBar.barHeight;
                        mouseButtonHeld = false;
                        return true;
                }

                // Horizontal (width) resize click
                StatusBar first = resizeHover.first();
                if (!editBarWidget.isMouseOver(click.x(), click.y()) && click.button() == 0 && first != null) {
                        BarPositioner.BarAnchor barAnchor = first.anchor;
                        if (barAnchor != null) {
                                if (resizeHover.rightBoolean()) {
                                        resizedBars.left(first);
                                        if (FancyStatusBars.barPositioner.hasNeighbor(barAnchor, first.gridY, first.gridX, true))
                                                resizedBars.right(FancyStatusBars.barPositioner.getBar(barAnchor, first.gridY, first.gridX + (barAnchor.isRight() ? 1 : -1)));
                                        else resizedBars.right(null);
                                } else {
                                        resizedBars.right(first);
                                        if (FancyStatusBars.barPositioner.hasNeighbor(barAnchor, first.gridY, first.gridX, false))
                                                resizedBars.left(FancyStatusBars.barPositioner.getBar(barAnchor, first.gridY, first.gridX + (barAnchor.isRight() ? -1 : 1)));
                                        else resizedBars.left(null);
                                }
                        } else {
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

                // Global CUSTOM element hit detection (works even outside bar bounds)
                if (click.button() == 0 && !editBarWidget.isMouseOver(click.x(), click.y())) {
                        int cx = (int) click.x();
                        int cy = (int) click.y();
                        ScreenRectangle clickRect = new ScreenRectangle(cx, cy, 1, 1);
                        for (StatusBar bar : FancyStatusBars.statusBars.values()) {
                                if (!bar.enabled) continue;
                                if (bar.getTextPosition() == StatusBar.TextPosition.CUSTOM) {
                                        if (bar.getTextHitArea(minecraft.font).overlaps(clickRect)) {
                                                selectedBar = bar;
                                                if (tryStartSubElementResize(bar, cx, cy)) return true;
                                                if (tryStartSubElementDrag(bar, cx, cy)) return true;
                                        }
                                }
                                if (bar.getIconPosition() == StatusBar.IconPosition.CUSTOM) {
                                        if (bar.getIconHitArea().overlaps(clickRect)) {
                                                selectedBar = bar;
                                                if (tryStartSubElementResize(bar, cx, cy)) return true;
                                                if (tryStartSubElementDrag(bar, cx, cy)) return true;
                                        }
                                }
                        }
                }

                boolean handled = super.mouseClicked(click, doubled);
                if (!handled && !editBarWidget.isMouseOver(click.x(), click.y())) {
                        selectedBar = null;
                        editBarWidget.visible = false;
                }
                return handled;
        }
}
