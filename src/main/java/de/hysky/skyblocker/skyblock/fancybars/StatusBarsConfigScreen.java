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
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.input.KeyEvent;
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
        /** How far outside bar bounds the arrows extend (used for "extended zone" hit test). */
        private static final int ARROW_EXT = 14;

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
        /** The currently "active" bar (for outline / right-click menu). */
        private @Nullable StatusBar selectedBar = null;
        /**
         * Which sub-element within the selected bar is selected.
         * null = the bar itself; "text" = custom text element; "icon" = custom icon element.
         * When non-null the yellow bar outline is hidden and only cyan handles for that element are shown.
         */
        private @Nullable String selectedSubElement = null;

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

        // ─────────────────────── Helpers ───────────────────────

        /** True if (x, y) is inside the bar body + the outer arrow zone. */
        private static boolean isInBarExtendedZone(StatusBar bar, int x, int y) {
                return x >= bar.getX() - ARROW_EXT && x <= bar.getX() + bar.getWidth()  + ARROW_EXT
                    && y >= bar.getY() - ARROW_EXT && y <= bar.getY() + bar.getHeight() + ARROW_EXT;
        }

        /** True if (x, y) is inside the bar body only (not in the arrow gutter). */
        private static boolean isInBarBody(StatusBar bar, int x, int y) {
                return x >= bar.getX() && x <= bar.getX() + bar.getWidth()
                    && y >= bar.getY() && y <= bar.getY() + bar.getHeight();
        }

        private void clearSelection() {
                selectedBar = null;
                selectedSubElement = null;
                editBarWidget.visible = false;
        }

        // ─────────────────────── Drag helpers ───────────────────────

        private void startBarDrag(StatusBar statusBar) {
                cursorBar = statusBar;
                cursorBar.inMouse = true;
                cursorBar.enabled = true;
                if (statusBar.getWidth() > 0) statusBar.width = (float) statusBar.getWidth() / this.width;
                if (statusBar.anchor != null)
                        FancyStatusBars.barPositioner.removeBar(statusBar.anchor, statusBar.gridY, statusBar);
                statusBar.anchor = null;
                FancyStatusBars.updatePositions(true);
                cursorBar.setX(width + 5);
                updateScreenRects();
                editBarWidget.visible = false;
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
                                default -> { resizingSubElement = false; return false; }
                        }
                        return true;
                }
                return false;
        }

        // ─────────────────────── Render ───────────────────────

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
                                int d = mouseX - resizeSubStartMouse;
                                resizeSubBar.textCustomScale = Math.max(0.5f, Math.min(4.0f, resizeSubStartScale + d * 0.02f));
                        } else if (resizeSubIsHoriz) {
                                int d = mouseX - resizeSubStartMouse;
                                resizeSubBar.iconCustomW = Math.max(4, Math.min(64, resizeSubStartPx + d));
                        } else {
                                int d = mouseY - resizeSubStartMouse;
                                resizeSubBar.iconCustomH = Math.max(4, Math.min(64, resizeSubStartPx + d));
                        }
                }

                // Bar drag threshold check (only when bar itself is selected, not a sub-element)
                if (mouseButtonHeld && selectedBar != null && selectedSubElement == null
                                && cursorBar == null && !resizing && !resizingHeight
                                && !draggingSubElement && !resizingSubElement) {
                        double dx = mouseX - dragStartX;
                        double dy = mouseY - dragStartY;
                        if (dx * dx + dy * dy > (double) DRAG_THRESHOLD * DRAG_THRESHOLD) {
                                startBarDrag(selectedBar);
                        }
                }

                context.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE, width / 2 - HOTBAR_WIDTH / 2, height - 22, HOTBAR_WIDTH, 22);
                editBarWidget.render(context, mouseX, mouseY, delta);

                Window window = minecraft.getWindow();
                int scaleFactor = window.calculateScale(0, minecraft.isEnforceUnicode()) - window.getGuiScale() + 3;
                if ((scaleFactor & 2) == 0) scaleFactor++;
                ScreenRectangle mouseRect = new ScreenRectangle(new ScreenPosition(mouseX - scaleFactor / 2, mouseY - scaleFactor / 2), scaleFactor, scaleFactor);

                // Draw selection visuals
                if (selectedBar != null && cursorBar == null && selectedBar.enabled) {
                        if (selectedSubElement == null) {
                                // Bar selected: yellow outline with arrows
                                drawBarOutlineWithArrows(context, selectedBar);
                        } else if ("text".equals(selectedSubElement)
                                        && selectedBar.getTextPosition() == StatusBar.TextPosition.CUSTOM) {
                                // Text sub-element selected: cyan handles only
                                drawCustomElementHandles(context, selectedBar.getTextVisualArea(minecraft.font), true);
                        } else if ("icon".equals(selectedSubElement)
                                        && selectedBar.getIconPosition() == StatusBar.IconPosition.CUSTOM) {
                                // Icon sub-element selected: cyan handles only
                                drawCustomElementHandles(context, selectedBar.getIconVisualArea(), false);
                        }
                }

                if (cursorBar != null) {
                        int bx = mouseX + cursorOffset.x();
                        int by = mouseY + cursorOffset.y();
                        cursorBar.renderCursor(context, bx, by, delta);

                        // Position overlay: X from left, Y from bottom-left (bottom of bar = Y 0)
                        int posX = bx;
                        int posY = this.height - by - cursorBar.getHeight();
                        String posText = "X: " + posX + "  Y: " + posY;
                        int labelX = bx + cursorBar.getWidth() / 2 - minecraft.font.width(posText) / 2;
                        int labelY = by - 14;
                        // Dark background for readability
                        context.fill(labelX - 2, labelY - 1, labelX + minecraft.font.width(posText) + 2, labelY + 10, 0xAA000000);
                        context.drawString(minecraft.font, posText, labelX, labelY, BAR_SEL_COLOR, false);

                } else {
                        if (resizing) {
                                int middleX;
                                StatusBar rightBar = resizedBars.right();
                                StatusBar leftBar = resizedBars.left();
                                boolean hasRight = rightBar != null;
                                boolean hasLeft = leftBar != null;
                                BarPositioner.BarAnchor barAnchor;
                                if (!hasRight) { barAnchor = leftBar.anchor; middleX = leftBar.getX() + leftBar.getWidth(); }
                                else           { barAnchor = rightBar.anchor; middleX = rightBar.getX(); }

                                if (barAnchor != null) {
                                        BarPositioner.SizeRule sizeRule = barAnchor.getSizeRule();
                                        boolean doResize = true;
                                        float widthPerSize = sizeRule.isTargetSize()
                                                ? (float) sizeRule.totalWidth() / sizeRule.targetSize()
                                                : sizeRule.widthPerSize();
                                        if (mouseX < middleX) {
                                                if (middleX - mouseX > widthPerSize / RESIZE_THRESHOLD) {
                                                        if (hasRight && rightBar.size + 1 > sizeRule.maxSize()) doResize = false;
                                                        if (hasLeft && leftBar.size - 1 < sizeRule.minSize()) doResize = false;
                                                        if (doResize) { if (hasRight) rightBar.size++; if (hasLeft) leftBar.size--; FancyStatusBars.updatePositions(true); }
                                                }
                                        } else {
                                                if (mouseX - middleX > widthPerSize / RESIZE_THRESHOLD) {
                                                        if (hasRight && rightBar.size - 1 < sizeRule.minSize()) doResize = false;
                                                        if (hasLeft && leftBar.size + 1 > sizeRule.maxSize()) doResize = false;
                                                        if (doResize) { if (hasRight) rightBar.size--; if (hasLeft) leftBar.size++; FancyStatusBars.updatePositions(true); }
                                                }
                                        }
                                } else {
                                        if (hasLeft)       leftBar.setWidth(Math.max(BAR_MINIMUM_WIDTH, mouseX - leftBar.getX()));
                                        else if (hasRight) { int endX = rightBar.getX() + rightBar.getWidth(); rightBar.setX(Math.min(endX - BAR_MINIMUM_WIDTH, mouseX)); rightBar.setWidth(endX - rightBar.getX()); }
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
                                context.requestCursor(resizeSubIsHoriz ? CursorTypes.RESIZE_EW : CursorTypes.RESIZE_NS);

                        } else {
                                // Hover detection: sub-element resize edges (highest priority when sub-element selected)
                                subElementEdgeHover = SubElementEdge.NONE;
                                subElementEdgeBar = null;
                                if (selectedBar != null && selectedBar.enabled && selectedSubElement != null) {
                                        if ("text".equals(selectedSubElement) && selectedBar.getTextPosition() == StatusBar.TextPosition.CUSTOM) {
                                                ScreenRectangle vis = selectedBar.getTextVisualArea(minecraft.font);
                                                int rx = vis.position().x() + vis.width();
                                                int ly = vis.position().y(), by_ = ly + vis.height();
                                                if (Math.abs(mouseX - rx) <= EDGE_TOLERANCE && mouseY >= ly && mouseY <= by_) {
                                                        subElementEdgeHover = SubElementEdge.TEXT_RIGHT;
                                                        subElementEdgeBar = selectedBar;
                                                        context.requestCursor(CursorTypes.RESIZE_EW);
                                                }
                                        }
                                        if (subElementEdgeHover == SubElementEdge.NONE && "icon".equals(selectedSubElement)
                                                        && selectedBar.getIconPosition() == StatusBar.IconPosition.CUSTOM) {
                                                ScreenRectangle vis = selectedBar.getIconVisualArea();
                                                int rx = vis.position().x() + vis.width();
                                                int byx = vis.position().y() + vis.height();
                                                int lx = vis.position().x(), ly = vis.position().y();
                                                if (Math.abs(mouseX - rx) <= EDGE_TOLERANCE && mouseY >= ly && mouseY <= byx) {
                                                        subElementEdgeHover = SubElementEdge.ICON_RIGHT; subElementEdgeBar = selectedBar;
                                                        context.requestCursor(CursorTypes.RESIZE_EW);
                                                } else if (Math.abs(mouseY - byx) <= EDGE_TOLERANCE && mouseX >= lx && mouseX <= rx) {
                                                        subElementEdgeHover = SubElementEdge.ICON_BOTTOM; subElementEdgeBar = selectedBar;
                                                        context.requestCursor(CursorTypes.RESIZE_NS);
                                                }
                                        }
                                }

                                if (subElementEdgeHover == SubElementEdge.NONE) {
                                        // Bar horizontal resize edges
                                        boolean foundHorizontal = false;
                                        rectLoop:
                                        for (ScreenRectangle screenRect : rectToBar.keySet()) {
                                                for (ScreenDirection direction : new ScreenDirection[]{ScreenDirection.LEFT, ScreenDirection.RIGHT}) {
                                                        if (screenRect.getBorder(direction).step(direction).overlaps(mouseRect) && !editBarWidget.isMouseOver(mouseX, mouseY)) {
                                                                Pair<StatusBar, BarLocation> barPair = rectToBar.get(screenRect);
                                                                BarLocation barLocation = barPair.right();
                                                                StatusBar bar = barPair.left();
                                                                if (!bar.enabled) break;
                                                                boolean right = direction.equals(ScreenDirection.RIGHT);
                                                                if (barLocation.barAnchor() != null) {
                                                                        if (barLocation.barAnchor().getSizeRule().isTargetSize() && !FancyStatusBars.barPositioner.hasNeighbor(barLocation.barAnchor(), barLocation.y(), barLocation.x(), right)) break;
                                                                        if (!barLocation.barAnchor().getSizeRule().isTargetSize() && barLocation.x() == 0 && barLocation.barAnchor().isRight() != right) break;
                                                                }
                                                                resizeHover.first(bar); resizeHover.right(right);
                                                                resizeHeightHover.first(null);
                                                                foundHorizontal = true;
                                                                context.requestCursor(CursorTypes.RESIZE_EW);
                                                                break rectLoop;
                                                        } else { resizeHover.first(null); }
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
                                                                if (screenRect.getBorder(direction).step(direction).overlaps(mouseRect) && !editBarWidget.isMouseOver(mouseX, mouseY)) {
                                                                        resizeHeightHover.first(bar);
                                                                        resizeHeightHover.right(direction.equals(ScreenDirection.UP));
                                                                        context.requestCursor(CursorTypes.RESIZE_NS);
                                                                        break heightLoop;
                                                                } else { resizeHeightHover.first(null); }
                                                        }
                                                }
                                        } else { resizeHeightHover.first(null); }
                                }
                        }
                }
        }

        // ─────────────────────── Outline / arrow drawing ───────────────────────

        /** Yellow selection outline + directional move arrows around the bar. */
        private void drawBarOutlineWithArrows(GuiGraphics ctx, StatusBar bar) {
                int x = bar.getX() - 1, y = bar.getY() - 1;
                int w = bar.getWidth() + 2, h = bar.getHeight() + 2;
                // 2px border
                ctx.fill(x, y, x + w, y + 2, BAR_SEL_COLOR);
                ctx.fill(x, y + h - 2, x + w, y + h, BAR_SEL_COLOR);
                ctx.fill(x, y, x + 2, y + h, BAR_SEL_COLOR);
                ctx.fill(x + w - 2, y, x + w, y + h, BAR_SEL_COLOR);
                // Arrows at midpoints of each edge
                int mx = x + w / 2, my = y + h / 2;
                fillLeftArrow(ctx,  x - 7,      my, BAR_SEL_COLOR);
                fillRightArrow(ctx, x + w + 6,  my, BAR_SEL_COLOR);
                fillUpArrow(ctx,    mx,      y - 7,  BAR_SEL_COLOR);
                fillDownArrow(ctx,  mx,  y + h + 6,  BAR_SEL_COLOR);
        }

        /**
         * Cyan border + outward move arrows for a CUSTOM text or icon sub-element.
         * Also shows white resize handle squares at resize edges.
         * @param isText true=text (right-edge resize only), false=icon (right + bottom resize)
         */
        private void drawCustomElementHandles(GuiGraphics ctx, ScreenRectangle area, boolean isText) {
                int x = area.position().x(), y = area.position().y();
                int w = area.width(), h = area.height();
                // 2px border
                ctx.fill(x, y, x + w, y + 2, HANDLE_COLOR);
                ctx.fill(x, y + h - 2, x + w, y + h, HANDLE_COLOR);
                ctx.fill(x, y, x + 2, y + h, HANDLE_COLOR);
                ctx.fill(x + w - 2, y, x + w, y + h, HANDLE_COLOR);
                // Move arrows
                int mx = x + w / 2, my = y + h / 2;
                fillLeftArrow(ctx,  x - 7,      my, HANDLE_COLOR);
                fillRightArrow(ctx, x + w + 6,  my, HANDLE_COLOR);
                fillUpArrow(ctx,    mx,      y - 7,  HANDLE_COLOR);
                fillDownArrow(ctx,  mx,  y + h + 6,  HANDLE_COLOR);
                // Right-edge resize handle (white square)
                ctx.fill(x + w + 2, my - 3, x + w + 7, my + 3, 0xFFFFFFFF);
                // Bottom-edge resize handle (icon only)
                if (!isText) ctx.fill(mx - 3, y + h + 2, mx + 3, y + h + 7, 0xFFFFFFFF);
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
                                button -> minecraft.setScreen(new TipsScreen(this)))
                                .bounds(width - 20, (height - 15) / 2, 15, 15)
                                .build());
                this.addRenderableWidget(Button.builder(Component.translatable("skyblocker.bars.config.resetToDefault"),
                                button -> {
                                        FancyStatusBars.resetToDefaults();
                                        clearSelection();
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
                FancyStatusBars.statusBars.values().forEach(sb -> sb.setOnClick(null));
                if (cursorBar != null) cursorBar.inMouse = false;
                FancyStatusBars.updatePositions(false);
                FancyStatusBars.saveBarConfig();
        }

        @Override
        public boolean isPauseScreen() { return false; }

        // ─────────────────────── Click handlers ───────────────────────

        private void onBarClick(StatusBar statusBar, MouseButtonEvent click) {
                if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        int cx = (int) click.x(), cy = (int) click.y();
                        ScreenRectangle clickRect = new ScreenRectangle(cx, cy, 1, 1);

                        // ── Check for sub-element resize edge (only when that sub-element is already selected) ──
                        if (selectedBar == statusBar && selectedSubElement != null) {
                                if (tryStartSubElementResize(statusBar, cx, cy)) return;
                        }

                        // ── Check if click lands on a CUSTOM text element ──
                        if (statusBar.getTextPosition() == StatusBar.TextPosition.CUSTOM
                                        && statusBar.getTextHitArea(minecraft.font).overlaps(clickRect)) {
                                selectedBar = statusBar;
                                selectedSubElement = "text";
                                mouseButtonHeld = true;
                                subDragStartMouseX = cx; subDragStartMouseY = cy;
                                subDragStartOffX = statusBar.textCustomOffX; subDragStartOffY = statusBar.textCustomOffY;
                                draggingSubElement = true; draggingText = true;
                                editBarWidget.visible = false;
                                return;
                        }

                        // ── Check if click lands on a CUSTOM icon element ──
                        if (statusBar.getIconPosition() == StatusBar.IconPosition.CUSTOM
                                        && statusBar.getIconHitArea().overlaps(clickRect)) {
                                selectedBar = statusBar;
                                selectedSubElement = "icon";
                                mouseButtonHeld = true;
                                subDragStartMouseX = cx; subDragStartMouseY = cy;
                                subDragStartOffX = statusBar.iconCustomOffX; subDragStartOffY = statusBar.iconCustomOffY;
                                draggingSubElement = true; draggingText = false;
                                editBarWidget.visible = false;
                                return;
                        }

                        // ── Click on bar body → select bar itself ──
                        selectedBar = statusBar;
                        selectedSubElement = null;
                        mouseButtonHeld = true;
                        dragStartX = cx; dragStartY = cy;
                        cursorOffset = new ScreenPosition(statusBar.getX() - cx, statusBar.getY() - cy);
                        editBarWidget.visible = false;

                } else if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        selectedBar = statusBar;
                        selectedSubElement = null;
                        int x = (int) Math.min(click.x() + 5, width  - editBarWidget.getWidth());
                        int y = (int) Math.min(click.y() + 5, height - editBarWidget.getHeight());
                        editBarWidget.insideMouseX = (int) click.x();
                        editBarWidget.insideMouseY = (int) click.y();
                        editBarWidget.visible = true;
                        editBarWidget.setStatusBar(statusBar);
                        editBarWidget.setX(x); editBarWidget.setY(y);
                }
        }

        private void updateScreenRects() {
                rectToBar.clear();
                FancyStatusBars.statusBars.values().forEach(sb -> {
                        if (!sb.enabled) return;
                        rectToBar.put(new ScreenRectangle(new ScreenPosition(sb.getX(), sb.getY()), sb.getWidth(), sb.getHeight()),
                                        Pair.of(sb, BarLocation.of(sb)));
                });
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent click) {
                mouseButtonHeld = false;

                if (resizingSubElement) {
                        resizingSubElement = false; resizeSubBar = null;
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
                        StatusBar bar = resizedBars.left() != null ? resizedBars.left() : resizedBars.right();
                        if (bar != null && bar.anchor == null) { bar.x = (float) bar.getX() / width; bar.width = (float) bar.getWidth() / width; }
                        resizedBars.left(null); resizedBars.right(null);
                        updateScreenRects();
                        return true;
                } else if (resizingHeight) {
                        resizingHeight = false;
                        if (heightResizeBar != null) { heightResizeBar.y = (float) heightResizeBar.getY() / height; heightResizeBar = null; }
                        updateScreenRects();
                        return true;
                }
                return super.mouseReleased(click);
        }

        // ─────────────────────── Keyboard nudge ───────────────────────

        @Override
        public boolean keyPressed(KeyEvent keyEvent) {
                int key = keyEvent.key();
                boolean arrow = key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT
                                || key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN;

                int mods = keyEvent.modifiers();
                boolean shiftHeld = (mods & GLFW.GLFW_MOD_SHIFT) != 0;
                boolean altHeld   = (mods & GLFW.GLFW_MOD_ALT)   != 0;

                if (arrow && selectedBar != null && (shiftHeld || altHeld)) {
                        boolean shift = shiftHeld;
                        boolean alt   = altHeld;
                        boolean left  = key == GLFW.GLFW_KEY_LEFT;
                        boolean right = key == GLFW.GLFW_KEY_RIGHT;
                        boolean up    = key == GLFW.GLFW_KEY_UP;
                        boolean down  = key == GLFW.GLFW_KEY_DOWN;

                        if (selectedSubElement == null) {
                                // ── Bar ──
                                if (shift && selectedBar.anchor == null) {
                                        // Nudge position (floating bars only)
                                        if (left)  selectedBar.x -= 1.0f / width;
                                        if (right) selectedBar.x += 1.0f / width;
                                        if (up)    selectedBar.y -= 1.0f / height;
                                        if (down)  selectedBar.y += 1.0f / height;
                                }
                                if (alt) {
                                        // Resize bar
                                        if ((left || right) && selectedBar.anchor == null) {
                                                int newW = selectedBar.getWidth() + (right ? 1 : -1);
                                                selectedBar.setWidth(Math.max(BAR_MINIMUM_WIDTH, newW));
                                                selectedBar.width = (float) selectedBar.getWidth() / width;
                                        }
                                        if (up || down) {
                                                selectedBar.barHeight = Math.max(StatusBar.MIN_BAR_HEIGHT,
                                                                selectedBar.barHeight + (down ? 1 : -1));
                                        }
                                }
                        } else if ("text".equals(selectedSubElement)) {
                                // ── Custom text ──
                                if (shift) {
                                        if (left)  selectedBar.textCustomOffX--;
                                        if (right) selectedBar.textCustomOffX++;
                                        if (up)    selectedBar.textCustomOffY--;
                                        if (down)  selectedBar.textCustomOffY++;
                                }
                                if (alt) {
                                        // LEFT/RIGHT scales text; UP/DOWN currently unused for text
                                        float step = 0.05f;
                                        if (left)  selectedBar.textCustomScale = Math.max(0.5f, selectedBar.textCustomScale - step);
                                        if (right) selectedBar.textCustomScale = Math.min(4.0f, selectedBar.textCustomScale + step);
                                }
                        } else if ("icon".equals(selectedSubElement)) {
                                // ── Custom icon ──
                                if (shift) {
                                        if (left)  selectedBar.iconCustomOffX--;
                                        if (right) selectedBar.iconCustomOffX++;
                                        if (up)    selectedBar.iconCustomOffY--;
                                        if (down)  selectedBar.iconCustomOffY++;
                                }
                                if (alt) {
                                        if (left)  selectedBar.iconCustomW = Math.max(4, selectedBar.iconCustomW - 1);
                                        if (right) selectedBar.iconCustomW = Math.min(64, selectedBar.iconCustomW + 1);
                                        if (up)    selectedBar.iconCustomH = Math.max(4, selectedBar.iconCustomH - 1);
                                        if (down)  selectedBar.iconCustomH = Math.min(64, selectedBar.iconCustomH + 1);
                                }
                        }

                        FancyStatusBars.updatePositions(true);
                        updateScreenRects();
                        return true;
                }

                return super.keyPressed(keyEvent);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
                int cx = (int) click.x(), cy = (int) click.y();

                // Sub-element edge resize click (highest priority, only for selected sub-element)
                if (click.button() == 0 && subElementEdgeHover != SubElementEdge.NONE && subElementEdgeBar != null) {
                        if (tryStartSubElementResize(subElementEdgeBar, cx, cy)) return true;
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

                // Width resize click
                StatusBar first = resizeHover.first();
                if (!editBarWidget.isMouseOver(click.x(), click.y()) && click.button() == 0 && first != null) {
                        BarPositioner.BarAnchor barAnchor = first.anchor;
                        if (barAnchor != null) {
                                if (resizeHover.rightBoolean()) {
                                        resizedBars.left(first);
                                        resizedBars.right(FancyStatusBars.barPositioner.hasNeighbor(barAnchor, first.gridY, first.gridX, true)
                                                ? FancyStatusBars.barPositioner.getBar(barAnchor, first.gridY, first.gridX + (barAnchor.isRight() ? 1 : -1)) : null);
                                } else {
                                        resizedBars.right(first);
                                        resizedBars.left(FancyStatusBars.barPositioner.hasNeighbor(barAnchor, first.gridY, first.gridX, false)
                                                ? FancyStatusBars.barPositioner.getBar(barAnchor, first.gridY, first.gridX + (barAnchor.isRight() ? -1 : 1)) : null);
                                }
                        } else {
                                if (resizeHover.rightBoolean()) { resizedBars.left(first); resizedBars.right(null); }
                                else                            { resizedBars.right(first); resizedBars.left(null); }
                        }
                        resizing = true;
                        return true;
                }

                // Global CUSTOM element hit detection (text/icon may be outside bar bounds)
                if (click.button() == 0 && !editBarWidget.isMouseOver(click.x(), click.y())) {
                        ScreenRectangle clickRect = new ScreenRectangle(cx, cy, 1, 1);
                        for (StatusBar bar : FancyStatusBars.statusBars.values()) {
                                if (!bar.enabled) continue;
                                if (bar.getTextPosition() == StatusBar.TextPosition.CUSTOM
                                                && bar.getTextHitArea(minecraft.font).overlaps(clickRect)
                                                && !isInBarBody(bar, cx, cy)) {
                                        // Clicked custom text element that is OUTSIDE the bar body
                                        if (selectedBar == bar && "text".equals(selectedSubElement) && tryStartSubElementResize(bar, cx, cy)) return true;
                                        selectedBar = bar; selectedSubElement = "text";
                                        mouseButtonHeld = true;
                                        subDragStartMouseX = cx; subDragStartMouseY = cy;
                                        subDragStartOffX = bar.textCustomOffX; subDragStartOffY = bar.textCustomOffY;
                                        draggingSubElement = true; draggingText = true;
                                        editBarWidget.visible = false;
                                        return true;
                                }
                                if (bar.getIconPosition() == StatusBar.IconPosition.CUSTOM
                                                && bar.getIconHitArea().overlaps(clickRect)
                                                && !isInBarBody(bar, cx, cy)) {
                                        if (selectedBar == bar && "icon".equals(selectedSubElement) && tryStartSubElementResize(bar, cx, cy)) return true;
                                        selectedBar = bar; selectedSubElement = "icon";
                                        mouseButtonHeld = true;
                                        subDragStartMouseX = cx; subDragStartMouseY = cy;
                                        subDragStartOffX = bar.iconCustomOffX; subDragStartOffY = bar.iconCustomOffY;
                                        draggingSubElement = true; draggingText = false;
                                        editBarWidget.visible = false;
                                        return true;
                                }
                        }
                }

                boolean handled = super.mouseClicked(click, doubled);

                // If nothing handled the click, check if we should keep or clear selection
                if (!handled && !editBarWidget.isMouseOver(click.x(), click.y()) && click.button() == 0) {
                        if (selectedBar != null && isInBarExtendedZone(selectedBar, cx, cy)) {
                                // Click is in the arrow zone of the selected bar — keep selection and start drag
                                if (selectedSubElement == null) {
                                        mouseButtonHeld = true;
                                        dragStartX = cx; dragStartY = cy;
                                        cursorOffset = new ScreenPosition(selectedBar.getX() - cx, selectedBar.getY() - cy);
                                }
                                // (sub-element already selected — do nothing special, just don't deselect)
                        } else {
                                clearSelection();
                        }
                }

                return handled;
        }
}
