package de.hysky.skyblocker.skyblock.fancybars;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanMutablePair;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import de.hysky.skyblocker.skyblock.fancybars.BarPositioner.BarLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StatusBarsConfigScreen extends Screen {
    private static final Identifier HOTBAR_TEXTURE = new Identifier("hud/hotbar");

    public static final long RESIZE_CURSOR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR);

    private final Map<ScreenRect, BarLocation> rectToBarLocation = new HashMap<>();
    private static final int HOTBAR_WIDTH = 182;

    private @Nullable StatusBar cursorBar = null;

    public StatusBarsConfigScreen() {
        super(Text.of("Status Bars Config"));
        FancyStatusBars.updatePositions();
    }

    private BarLocation currentInsertLocation = new BarLocation(null, 0, 0);

    private final Pair<BarLocation, Boolean> resizeHover = new ObjectBooleanMutablePair<>(BarLocation.NULL, false);

    private final Pair<BarLocation, BarLocation> resizedBars = ObjectObjectMutablePair.of(BarLocation.NULL, BarLocation.NULL);
    private boolean resizing = false;

    private EditBarWidget editBarWidget;

    // prioritize left and right cuz they are much smaller space than up and down
    private static final NavigationDirection[] DIRECTION_CHECK_ORDER = new NavigationDirection[]{NavigationDirection.LEFT, NavigationDirection.RIGHT, NavigationDirection.UP, NavigationDirection.DOWN};

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        /*for (ScreenRect screenRect : meaningFullName.keySet()) {
            context.fillGradient(screenRect.position().x(), screenRect.position().y(), screenRect.position().x() + screenRect.width(), screenRect.position().y() + screenRect.height(), 0xFFFF0000, 0xFF0000FF);
        }*/
        super.render(context, mouseX, mouseY, delta);
        context.drawGuiTexture(HOTBAR_TEXTURE, width / 2 - HOTBAR_WIDTH / 2, height - 22, HOTBAR_WIDTH, 22);
        editBarWidget.render(context, mouseX, mouseY, delta);

        ScreenRect mouseRect = new ScreenRect(new ScreenPos(mouseX - 1, mouseY - 1), 3, 3);
        assert client != null;
        Window window = client.getWindow();

        if (cursorBar != null) {
            cursorBar.renderCursor(context, mouseX, mouseY, delta);
            boolean inserted = false;
            rectLoop:
            for (ScreenRect screenRect : rectToBarLocation.keySet()) {
                for (NavigationDirection direction : DIRECTION_CHECK_ORDER) {
                    boolean overlaps = screenRect.getBorder(direction).add(direction).overlaps(mouseRect);

                    if (overlaps) {
                        BarLocation barSnap = rectToBarLocation.get(screenRect);
                        if (barSnap.barAnchor() == null) break;
                        if (direction.getAxis().equals(NavigationAxis.VERTICAL)) {
                            int neighborInsertY = getNeighborInsertY(barSnap, !direction.isPositive());
                            if (!currentInsertLocation.equals(barSnap.barAnchor(), barSnap.x(), neighborInsertY)) {
                                if (cursorBar.anchor != null)
                                    FancyStatusBars.barPositioner.removeBar(cursorBar.anchor, cursorBar.gridY, cursorBar);
                                FancyStatusBars.barPositioner.addRow(barSnap.barAnchor(), neighborInsertY);
                                FancyStatusBars.barPositioner.addBar(barSnap.barAnchor(), neighborInsertY, cursorBar);
                                currentInsertLocation = BarLocation.of(cursorBar);
                                inserted = true;
                            }
                        } else {
                            int neighborInsertX = getNeighborInsertX(barSnap, direction.isPositive());
                            if (!currentInsertLocation.equals(barSnap.barAnchor(), neighborInsertX, barSnap.y())) {
                                if (cursorBar.anchor != null)
                                    FancyStatusBars.barPositioner.removeBar(cursorBar.anchor, cursorBar.gridY, cursorBar);
                                FancyStatusBars.barPositioner.addBar(barSnap.barAnchor(), barSnap.y(), neighborInsertX, cursorBar);
                                currentInsertLocation = BarLocation.of(cursorBar);
                                inserted = true;
                            }
                        }
                        break rectLoop;
                    }
                }
            }
            if (inserted) {
                FancyStatusBars.updatePositions();
                return;
            }
            // check for hovering empty anchors
            for (BarPositioner.BarAnchor barAnchor : BarPositioner.BarAnchor.allAnchors()) {
                if (FancyStatusBars.barPositioner.getRowCount(barAnchor) != 0) continue;
                ScreenRect anchorHitbox = barAnchor.getAnchorHitbox(barAnchor.getAnchorPosition(width, height));
                context.fill(anchorHitbox.getLeft(), anchorHitbox.getTop(), anchorHitbox.getRight(), anchorHitbox.getBottom(), 0x99FFFFFF);
                if (anchorHitbox.overlaps(mouseRect) && currentInsertLocation.barAnchor() != barAnchor) {
                    if (cursorBar.anchor != null)
                        FancyStatusBars.barPositioner.removeBar(cursorBar.anchor, cursorBar.gridY, cursorBar);
                    FancyStatusBars.barPositioner.addRow(barAnchor);
                    FancyStatusBars.barPositioner.addBar(barAnchor, 0, cursorBar);
                    currentInsertLocation = BarLocation.of(cursorBar);
                    FancyStatusBars.updatePositions();
                }
            }
        } else {
            if (resizing) { // actively resizing one or 2 bars
                int middleX;

                BarLocation left = resizedBars.left();
                BarLocation right = resizedBars.right();
                boolean hasRight = right.barAnchor() != null;
                boolean hasLeft = left.barAnchor() != null;
                BarPositioner.BarAnchor barAnchor;
                if (!hasRight) {
                    barAnchor = left.barAnchor();
                    StatusBar bar = FancyStatusBars.barPositioner.getBar(barAnchor, left.y(), left.x());
                    middleX = bar.getX() + bar.getWidth();
                } else {
                    barAnchor = right.barAnchor();
                    middleX = FancyStatusBars.barPositioner.getBar(barAnchor, right.y(), right.x()).getX();
                }

                boolean doResize = true;
                StatusBar rightBar = null;
                StatusBar leftBar = null;

                BarPositioner.SizeRule sizeRule = barAnchor.getSizeRule();

                float widthPerSize;
                if (sizeRule.isTargetSize())
                    widthPerSize = (float) sizeRule.totalWidth() / sizeRule.targetSize();
                else
                    widthPerSize = sizeRule.widthPerSize();

                // resize towards the left
                if (mouseX < middleX) {
                    if (middleX - mouseX > widthPerSize / .75f) {
                        if (hasRight) {
                            rightBar = FancyStatusBars.barPositioner.getBar(barAnchor, right.y(), right.x());
                            if (rightBar.size + 1 > sizeRule.maxSize()) doResize = false;
                        }
                        if (hasLeft) {
                            leftBar = FancyStatusBars.barPositioner.getBar(barAnchor, left.y(), left.x());
                            if (leftBar.size - 1 < sizeRule.minSize()) doResize = false;
                        }

                        if (doResize) {
                            if (hasRight) rightBar.size++;
                            if (hasLeft) leftBar.size--;
                            FancyStatusBars.updatePositions();
                        }
                    }
                } else { // towards the right
                    if (mouseX - middleX > widthPerSize / .75f) {
                        if (hasRight) {
                            rightBar = FancyStatusBars.barPositioner.getBar(barAnchor, right.y(), right.x());
                            if (rightBar.size - 1 < sizeRule.minSize()) doResize = false;
                        }
                        if (hasLeft) {
                            leftBar = FancyStatusBars.barPositioner.getBar(barAnchor, left.y(), left.x());
                            if (leftBar.size + 1 > sizeRule.maxSize()) doResize = false;
                        }

                        if (doResize) {
                            if (hasRight) rightBar.size--;
                            if (hasLeft) leftBar.size++;
                            FancyStatusBars.updatePositions();
                        }
                    }
                }

            } else { // hovering bars
                rectLoop:
                for (ScreenRect screenRect : rectToBarLocation.keySet()) {
                    for (NavigationDirection direction : new NavigationDirection[]{NavigationDirection.LEFT, NavigationDirection.RIGHT}) {
                        boolean overlaps = screenRect.getBorder(direction).add(direction).overlaps(mouseRect);

                        if (overlaps && !editBarWidget.isMouseOver(mouseX, mouseY)) {
                            BarLocation barLocation = rectToBarLocation.get(screenRect);
                            if (barLocation.barAnchor() == null) break;
                            boolean right = direction.equals(NavigationDirection.RIGHT);
                            // can't resize on the edge of a target size row!
                            if (barLocation.barAnchor().getSizeRule().isTargetSize() && !FancyStatusBars.barPositioner.hasNeighbor(barLocation.barAnchor(), barLocation.y(), barLocation.x(), right)) {
                                break;
                            }
                            resizeHover.first(barLocation);
                            resizeHover.right(right);
                            GLFW.glfwSetCursor(window.getHandle(), RESIZE_CURSOR);
                            break rectLoop;
                        } else {
                            resizeHover.first(BarLocation.NULL);
                            GLFW.glfwSetCursor(window.getHandle(), 0);
                        }
                    }
                }
            }
        }
    }

    private static int getNeighborInsertX(BarLocation barLocation, boolean right) {
        BarPositioner.BarAnchor barAnchor = barLocation.barAnchor();
        int gridX = barLocation.x();
        if (barAnchor == null) return 0;
        if (right) {
            return barAnchor.isRight() ? gridX + 1 : gridX;
        } else {
            return barAnchor.isRight() ? gridX : gridX + 1;
        }
    }

    private static int getNeighborInsertY(BarLocation barLocation, boolean up) {
        BarPositioner.BarAnchor barAnchor = barLocation.barAnchor();
        int gridY = barLocation.y();
        if (barAnchor == null) return 0;
        if (up) {
            return barAnchor.isUp() ? gridY + 1 : gridY;
        } else {
            return barAnchor.isUp() ? gridY : gridY + 1;
        }
    }

    @Override
    protected void init() {
        super.init();
        editBarWidget = new EditBarWidget(0, 0, this);
        editBarWidget.visible = false;
        addSelectableChild(editBarWidget); // rendering separately to have it above hotbar
        Collection<StatusBar> values = FancyStatusBars.statusBars.values();
        values.forEach(this::setup);
        checkNullAnchor(values);
        updateScreenRects();
        this.addDrawableChild(ButtonWidget.builder(Text.literal("?"),
                        button -> {
                            assert client != null;
                            client.setScreen(new PopupScreen.Builder(this, Text.translatable("skyblocker.bars.config.explanationTitle"))
                                    .button(Text.translatable("gui.ok"), PopupScreen::close)
                                    .message(Text.translatable("skyblocker.bars.config.explanation"))
                                    .build());
                        })
                .dimensions(width - 20, (height - 15) / 2, 15, 15)
                .build());
    }

    private void setup(StatusBar statusBar) {
        this.addDrawableChild(statusBar);
        statusBar.setOnClick(this::onBarClick);
    }

    private static void checkNullAnchor(Iterable<StatusBar> bars) {
        int offset = 0;
        for (StatusBar statusBar : bars) {
            if (statusBar.anchor == null) {
                statusBar.setX(5);
                statusBar.setY(50 + offset);
                statusBar.setWidth(30);
                offset += statusBar.getHeight();
            }
        }
    }

    @Override
    public void removed() {
        super.removed();
        FancyStatusBars.statusBars.values().forEach(statusBar -> statusBar.setOnClick(null));
        if (cursorBar != null) cursorBar.ghost = false;
        FancyStatusBars.updatePositions();
        assert client != null;
        GLFW.glfwSetCursor(client.getWindow().getHandle(), 0);
        FancyStatusBars.saveBarConfig();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void onBarClick(StatusBar statusBar, int button, int mouseX, int mouseY) {
        if (button == 0) {
            cursorBar = statusBar;
            cursorBar.ghost = true;
            if (statusBar.anchor != null)
                FancyStatusBars.barPositioner.removeBar(statusBar.anchor, statusBar.gridY, statusBar);
            FancyStatusBars.updatePositions();
            cursorBar.setX(width + 5); // send it to limbo lol
            updateScreenRects();
        } else if (button == 1) {
            int x = Math.min(mouseX - 1, width - editBarWidget.getWidth());
            int y = Math.min(mouseY - 1, height - editBarWidget.getHeight());
            editBarWidget.visible = true;
            editBarWidget.setStatusBar(statusBar);
            editBarWidget.setX(x);
            editBarWidget.setY(y);
        }
    }

    private void updateScreenRects() {
        rectToBarLocation.clear();
        FancyStatusBars.statusBars.values().forEach(statusBar1 -> {
            if (statusBar1.anchor == null) return;
            rectToBarLocation.put(
                    new ScreenRect(new ScreenPos(statusBar1.getX(), statusBar1.getY()), statusBar1.getWidth(), statusBar1.getHeight()),
                    BarLocation.of(statusBar1));
        });
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (cursorBar != null) {
            cursorBar.ghost = false;
            cursorBar = null;
            FancyStatusBars.updatePositions();
            checkNullAnchor(FancyStatusBars.statusBars.values());
            updateScreenRects();
            return true;
        } else if (resizing) {
            resizing = false;
            resizedBars.left(BarLocation.NULL);
            resizedBars.right(BarLocation.NULL);
            updateScreenRects();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        BarLocation first = resizeHover.first();
        // want the right click thing to have priority
        if (!editBarWidget.isMouseOver(mouseX, mouseY) && button == 0 && !first.equals(BarLocation.NULL)) {
            BarPositioner.BarAnchor barAnchor = first.barAnchor();
            assert barAnchor != null;
            if (resizeHover.right()) {
                resizedBars.left(first);

                if (FancyStatusBars.barPositioner.hasNeighbor(barAnchor, first.y(), first.x(), true)) {
                    resizedBars.right(new BarLocation(barAnchor, first.x() + (barAnchor.isRight() ? 1 : -1), first.y()));
                } else resizedBars.right(BarLocation.NULL);
            } else {
                resizedBars.right(first);

                if (FancyStatusBars.barPositioner.hasNeighbor(barAnchor, first.y(), first.x(), false)) {
                    resizedBars.left(new BarLocation(barAnchor, first.x() + (barAnchor.isRight() ? -1 : 1), first.y()));
                } else resizedBars.left(BarLocation.NULL);
            }
            resizing = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
