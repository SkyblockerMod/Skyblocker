package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.mixin.accessor.WindowAccessor;
import de.hysky.skyblocker.skyblock.FancyStatusBars;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanMutablePair;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StatusBarsConfigScreen extends Screen {

    private static final Identifier HOTBAR_TEXTURE = new Identifier("hud/hotbar");

    public static final long RESIZE_CURSOR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR);

    private final Map<ScreenRect, int[]> meaningFullName = new HashMap<>();
    private static final int HOTBAR_WIDTH = 182;

    private @Nullable StatusBar cursorBar = null;

    public StatusBarsConfigScreen() {
        super(Text.of("Status Bars Config"));
        FancyStatusBars.updatePositions();
    }

    private final int[] currentCursorCoords = new int[]{0, 0};

    private final Pair<int[], Boolean> resizeHover = new ObjectBooleanMutablePair<>(new int[]{0, 0}, false);

    private final Pair<int[], int[]> resizedBars = ObjectObjectMutablePair.of(new int[]{0, 0}, new int[]{0, 0});
    private boolean resizing = false;

    private EditBarWidget editBarWidget;

    @SuppressWarnings("UnreachableCode") // IntelliJ big stupid
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (ScreenRect screenRect : meaningFullName.keySet()) {
            context.fillGradient(screenRect.position().x(), screenRect.position().y(), screenRect.position().x() + screenRect.width(), screenRect.position().y() + screenRect.height(), 0xFFFF0000, 0xFF0000FF);
        }
        super.render(context, mouseX, mouseY, delta);
        context.drawGuiTexture(HOTBAR_TEXTURE, width / 2 - HOTBAR_WIDTH / 2, height - 22, HOTBAR_WIDTH, 22);
        editBarWidget.render(context, mouseX, mouseY, delta);

        ScreenRect mouseRect = new ScreenRect(new ScreenPos(mouseX - 1, mouseY - 1), 3, 3);
        assert client != null;
        WindowAccessor window = (WindowAccessor) (Object) client.getWindow();
        assert window != null;
        if (cursorBar != null) {
            cursorBar.renderCursor(context, mouseX, mouseY, delta);
            context.drawText(textRenderer, currentCursorCoords[0] + " " + currentCursorCoords[1], 100, 5, Colors.WHITE, true);


            if (FancyStatusBars.barGrid.getTopSize() == 0 && topBarZone.overlaps(mouseRect) && currentCursorCoords[1] != 1) {
                FancyStatusBars.barGrid.remove(cursorBar.gridX, cursorBar.gridY);
                FancyStatusBars.barGrid.addRow(1, true);
                FancyStatusBars.barGrid.add(1, 1, cursorBar);
                currentCursorCoords[1] = 1;
                FancyStatusBars.updatePositions();
            } else if (FancyStatusBars.barGrid.getBottomLeftSize() == 0 && bottomLeftBarZone.overlaps(mouseRect) && (currentCursorCoords[0] != -1 || currentCursorCoords[1] != -1)) {
                FancyStatusBars.barGrid.remove(cursorBar.gridX, cursorBar.gridY);
                FancyStatusBars.barGrid.addRow(-1, false);
                FancyStatusBars.barGrid.add(-1, -1, cursorBar);
                currentCursorCoords[0] = -1;
                currentCursorCoords[1] = -1;
                FancyStatusBars.updatePositions();
            } else if (FancyStatusBars.barGrid.getBottomRightSize() == 0 && bottomRightBarZone.overlaps(mouseRect) && (currentCursorCoords[0] != 1 || currentCursorCoords[1] != -1)) {
                FancyStatusBars.barGrid.remove(cursorBar.gridX, cursorBar.gridY);
                FancyStatusBars.barGrid.addRow(-1, true);
                FancyStatusBars.barGrid.add(1, -1, cursorBar);
                currentCursorCoords[0] = 1;
                currentCursorCoords[1] = -1;
                FancyStatusBars.updatePositions();
            } else rectLoop:for (ScreenRect screenRect : meaningFullName.keySet()) {
                for (NavigationDirection direction : NavigationDirection.values()) {
                    boolean overlaps = screenRect.getBorder(direction).add(direction).overlaps(mouseRect);

                    if (overlaps) {
                        int[] ints = meaningFullName.get(screenRect);
                        final boolean vertical = direction.getAxis().equals(NavigationAxis.VERTICAL);
                        int offsetX = 0;
                        int offsetY = 0;
                        if (vertical) {
                            if (!direction.isPositive()) {
                                offsetY = ints[1] > 0 ? 1 : 0;
                            } else {
                                offsetY = ints[1] > 0 ? 0 : -1;
                            }
                        } else {
                            if (direction.isPositive()) {
                                offsetX = ints[0] > 0 ? 1 : 0;
                            } else {
                                offsetX = ints[0] > 0 ? 0 : -1;
                            }
                        }
                        context.drawText(textRenderer, ints[0] + offsetX + " " + ints[1] + offsetY, 100, 15, Colors.WHITE, true);
                        if (ints[0] + offsetX != currentCursorCoords[0] || ints[1] + offsetY != currentCursorCoords[1]) {
                            currentCursorCoords[0] = ints[0] + offsetX;
                            currentCursorCoords[1] = ints[1] + offsetY;
                            FancyStatusBars.barGrid.remove(cursorBar.gridX, cursorBar.gridY);


                            if (vertical) {

                                FancyStatusBars.barGrid.addRow(ints[1] + offsetY, ints[0] > 0);
                                FancyStatusBars.barGrid.add(ints[0] < 0 ? -1 : 1, ints[1] + offsetY, cursorBar);
                            } else {

                                FancyStatusBars.barGrid.add(ints[0] + offsetX, ints[1], cursorBar);
                            }
                            FancyStatusBars.updatePositions();
                        }
                        break rectLoop;
                    }
                }
            }
        } else {
            // RESIZING STATE
            if (resizing) {
                int middleX;
                boolean bottom;

                int[] left = resizedBars.left();
                int[] right = resizedBars.right();
                boolean hasRight = right[0] != 0;
                boolean hasLeft = left[0] != 0;
                if (!hasRight) {
                    StatusBar bar = FancyStatusBars.barGrid.getBar(left[0], left[1]);
                    middleX = bar.getX() + bar.getWidth();
                    bottom = bar.gridY < 0;
                } else {
                    middleX = FancyStatusBars.barGrid.getBar(right[0], right[1]).getX();
                    bottom = right[1] < 0;
                }
                int i = bottom ? 20 : 10;
                boolean doResize = true;
                StatusBar rightBar = null;
                StatusBar leftBar = null;

                context.drawText(textRenderer, Integer.toString(mouseX - middleX), 100, 25, -1, true);

                if (mouseX < middleX) {
                    if (middleX - mouseX > i) {

                        if (hasRight) {
                            rightBar = FancyStatusBars.barGrid.getBar(right[0], right[1]);
                            if (rightBar.size + 1 > rightBar.getMaximumSize()) doResize = false;
                        }
                        if (hasLeft) {
                            leftBar = FancyStatusBars.barGrid.getBar(left[0], left[1]);
                            if (leftBar.size - 1 < leftBar.getMinimumSize()) doResize = false;
                        }

                        if (doResize) {
                            if (hasRight) rightBar.size++;
                            if (hasLeft) leftBar.size--;
                            FancyStatusBars.updatePositions();
                        }
                    }
                } else {
                    if (mouseX - middleX > i) {

                        if (hasRight) {
                            rightBar = FancyStatusBars.barGrid.getBar(right[0], right[1]);
                            if (rightBar.size - 1 < rightBar.getMinimumSize()) doResize = false;
                        }
                        if (hasLeft) {
                            leftBar = FancyStatusBars.barGrid.getBar(left[0], left[1]);
                            if (leftBar.size + 1 > leftBar.getMaximumSize()) doResize = false;
                        }
                        context.drawText(textRenderer, leftBar.size + " " + leftBar.getMaximumSize(), 100, 35, -1, true);

                        if (doResize) {
                            if (hasRight) rightBar.size--;
                            if (hasLeft) leftBar.size++;
                            FancyStatusBars.updatePositions();
                        }
                    }
                }
                GLFW.glfwSetCursor(window.getHandle(), RESIZE_CURSOR);
            }
            // NOT RESIZING STATE
            else {
                rectLoop:
                for (ScreenRect screenRect : meaningFullName.keySet()) {
                    for (NavigationDirection direction : new NavigationDirection[]{NavigationDirection.LEFT, NavigationDirection.RIGHT}) {
                        boolean overlaps = screenRect.getBorder(direction).add(direction).overlaps(mouseRect);

                        if (overlaps && !editBarWidget.isMouseOver(mouseX, mouseY)) {
                            int[] ints = meaningFullName.get(screenRect);
                            boolean left = direction.equals(NavigationDirection.LEFT);
                            if ((ints[0] == 1 && left) || (ints[0] == -1 && !left) || (!left && ints[1] > 0 && ints[0] == FancyStatusBars.barGrid.getRow(ints[1], true).size())) {
                                break;
                            }
                            resizeHover.first()[0] = ints[0];
                            resizeHover.first()[1] = ints[1];
                            resizeHover.right(!left);
                            GLFW.glfwSetCursor(window.getHandle(), RESIZE_CURSOR);
                            break rectLoop;
                        } else {
                            resizeHover.first()[0] = 0;
                            resizeHover.first()[1] = 0;
                            GLFW.glfwSetCursor(window.getHandle(), 0);
                        }
                    }
                }
            }
        }
    }

    private ScreenRect topBarZone = new ScreenRect(0, 0, 0, 0);
    private ScreenRect bottomLeftBarZone = new ScreenRect(0, 0, 0, 0);
    private ScreenRect bottomRightBarZone = new ScreenRect(0, 0, 0, 0);

    @Override
    protected void init() {
        super.init();
        editBarWidget = new EditBarWidget(0, 0, this);
        editBarWidget.visible = false;
        addSelectableChild(editBarWidget); // rendering separately to have it above hotbar
        Collection<StatusBar> values = FancyStatusBars.statusBars.values();
        values.forEach(this::setup);
        checkZeroCoordinates(values);
        updateScreenRects();
        topBarZone = new ScreenRect(width / 2 - HOTBAR_WIDTH / 2, height - 22 - 15, HOTBAR_WIDTH, 15);
        bottomLeftBarZone = new ScreenRect(width / 2 - HOTBAR_WIDTH / 2 - 20, height - 22, 20, 22);
        bottomRightBarZone = new ScreenRect(width / 2 + HOTBAR_WIDTH / 2, height - 22, 20, 22);
    }

    private void setup(StatusBar statusBar) {
        this.addDrawableChild(statusBar);
        statusBar.setOnClick(this::onClick);
    }

    private static void checkZeroCoordinates(Iterable<StatusBar> bars) {
        int offset = 0;
        for (StatusBar statusBar : bars) {
            if (statusBar.gridX == 0 || statusBar.gridY == 0) {
                statusBar.setX(5);
                statusBar.setY(5 + offset);
                statusBar.setWidth(30);
                offset += statusBar.getHeight();
            }
        }
    }

    @SuppressWarnings("UnreachableCode")
    @Override
    public void removed() {
        super.removed();
        FancyStatusBars.statusBars.values().forEach(statusBar -> statusBar.setOnClick(null));
        FancyStatusBars.updatePositions();
        assert client != null;
        GLFW.glfwSetCursor(((WindowAccessor) (Object) client.getWindow()).getHandle(), 0);
        FancyStatusBars.saveBarConfig();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void onClick(StatusBar statusBar, int button, int mouseX, int mouseY) {
        if (button == 0) {
            cursorBar = statusBar;
            FancyStatusBars.barGrid.remove(statusBar.gridX, statusBar.gridY);
            FancyStatusBars.updatePositions();
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
        meaningFullName.clear();
        FancyStatusBars.statusBars.values().forEach(statusBar1 -> meaningFullName.put(
                new ScreenRect(new ScreenPos(statusBar1.getX(), statusBar1.getY()), statusBar1.getWidth(), statusBar1.getHeight()),
                new int[]{statusBar1.gridX, statusBar1.gridY}));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (cursorBar != null) {
            cursorBar = null;
            FancyStatusBars.updatePositions();
            checkZeroCoordinates(FancyStatusBars.statusBars.values());
            updateScreenRects();
            return true;
        } else if (resizing) {
            resizing = false;
            resizedBars.left()[0] = 0;
            resizedBars.left()[1] = 0;
            resizedBars.right()[0] = 0;
            resizedBars.right()[1] = 0;
            updateScreenRects();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int[] first = resizeHover.first();
        // want the right click thing to have priority
        if (!editBarWidget.isMouseOver(mouseX, mouseY) && button == 0 && first[0] != 0 && first[1] != 0) {
            if (resizeHover.right()) {
                resizedBars.left()[0] = first[0];
                resizedBars.left()[1] = first[1];
                if (FancyStatusBars.barGrid.coordinatesExist(first[0] + 1, first[1])) {
                    resizedBars.right()[0] = first[0] + 1;
                    resizedBars.right()[1] = first[1];
                } else {
                    resizedBars.right()[0] = 0;
                    resizedBars.right()[1] = 0;
                }
            } else {
                resizedBars.right()[0] = first[0];
                resizedBars.right()[1] = first[1];
                if (FancyStatusBars.barGrid.coordinatesExist(first[0] - 1, first[1])) {
                    resizedBars.left()[0] = first[0] - 1;
                    resizedBars.left()[1] = first[1];
                } else {
                    resizedBars.left()[0] = 0;
                    resizedBars.left()[1] = 0;
                }
            }
            resizing = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
