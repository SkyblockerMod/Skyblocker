package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.mixin.accessor.WindowAccessor;
import de.hysky.skyblocker.skyblock.FancyStatusBars;
import net.minecraft.client.MinecraftClient;
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
import java.util.Iterator;
import java.util.Map;

public class StatusBarsConfigScreen extends Screen {

    private static final Identifier HOTBAR_TEXTURE = new Identifier("hud/hotbar");

    private final long RESIZE_CURSOR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR);

    private final Map<ScreenRect, int[]> meaningFullName = new HashMap<>();

    private @Nullable StatusBar cursorBar = null;

    public StatusBarsConfigScreen() {
        super(Text.of("Status Bars Config"));
        FancyStatusBars.updatePositions();
    }

    private final int[] currentCoords = new int[]{0, 0};

    @SuppressWarnings("UnreachableCode") // IntelliJ big stupid
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (ScreenRect screenRect : meaningFullName.keySet()) {
            context.fillGradient(screenRect.position().x(), screenRect.position().y(), screenRect.position().x() + screenRect.width(), screenRect.position().y() + screenRect.height(), 0xFFFF0000, 0xFF0000FF);
        }
        super.render(context, mouseX, mouseY, delta);
        context.drawGuiTexture(HOTBAR_TEXTURE, width / 2 - 91, height - 22, 182, 22);
        ScreenRect mouseRect = new ScreenRect(new ScreenPos(mouseX - 1, mouseY - 1), 3, 3);
        assert client != null;
        WindowAccessor window = (WindowAccessor) (Object) client.getWindow();
        assert window != null;
        if (cursorBar != null) {
            cursorBar.renderCursor(context, mouseX, mouseY, delta);
            context.drawText(textRenderer, currentCoords[0] + " " + currentCoords[1], 100, 5, Colors.WHITE, true);


            if (FancyStatusBars.barGrid.getTopSize() == 0 && topBarZone.overlaps(mouseRect) && currentCoords[1] != 1) {
                FancyStatusBars.barGrid.remove(cursorBar.gridX, cursorBar.gridY);
                FancyStatusBars.barGrid.addRow(1, true);
                FancyStatusBars.barGrid.add(1, 1, cursorBar);
                currentCoords[1] = 1;
                FancyStatusBars.updatePositions();
            } else if (FancyStatusBars.barGrid.getBottomLeftSize() == 0 && bottomLeftBarZone.overlaps(mouseRect) && (currentCoords[0] != -1 || currentCoords[1] != -1)) {
                FancyStatusBars.barGrid.remove(cursorBar.gridX, cursorBar.gridY);
                FancyStatusBars.barGrid.addRow(-1, false);
                FancyStatusBars.barGrid.add(-1, -1, cursorBar);
                currentCoords[0] = -1;
                currentCoords[1] = -1;
                FancyStatusBars.updatePositions();
            } else if (FancyStatusBars.barGrid.getBottomRightSize() == 0 && bottomRightBarZone.overlaps(mouseRect) && (currentCoords[0] != 1 || currentCoords[1] != -1)) {
                FancyStatusBars.barGrid.remove(cursorBar.gridX, cursorBar.gridY);
                FancyStatusBars.barGrid.addRow(-1, true);
                FancyStatusBars.barGrid.add(1, -1, cursorBar);
                currentCoords[0] = 1;
                currentCoords[1] = -1;
                FancyStatusBars.updatePositions();
            } else rectLoop:for (ScreenRect screenRect : meaningFullName.keySet()) {
                for (NavigationDirection direction : NavigationDirection.values()) {
                    boolean overlaps  = screenRect.getBorder(direction).add(direction).overlaps(mouseRect);

                    if (overlaps) {
                        int[] ints = meaningFullName.get(screenRect);
                        int offsetX = 0;
                        int offsetY = 0;
                        final boolean vertical = direction.getAxis().equals(NavigationAxis.VERTICAL);
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
                        if (ints[0] + offsetX != currentCoords[0] || ints[1] + offsetY != currentCoords[1]) {
                            currentCoords[0] = ints[0] + offsetX;
                            currentCoords[1] = ints[1] + offsetY;
                            System.out.println("Moving " + cursorBar);
                            System.out.println(ints[0] + " " + ints[1]);
                            FancyStatusBars.barGrid.remove(cursorBar.gridX, cursorBar.gridY);


                            if (vertical) {

                                FancyStatusBars.barGrid.addRow(ints[1] + offsetY, ints[0] > 0);
                                FancyStatusBars.barGrid.add(ints[0] < 0 ? -1 : 1, ints[1] + offsetY, cursorBar);
                            } else {

                                FancyStatusBars.barGrid.add(ints[0] + offsetX, ints[1], cursorBar);
                            }
                            FancyStatusBars.updatePositions();
                            System.out.println("After " + cursorBar);
                        }
                        break rectLoop;
                    }
                }
            }
        } else {
            rectLoop:for (ScreenRect screenRect : meaningFullName.keySet()) {
                for (NavigationDirection direction : new NavigationDirection[]{NavigationDirection.LEFT, NavigationDirection.RIGHT}) {
                    boolean overlaps  = screenRect.getBorder(direction).add(direction).overlaps(mouseRect);

                    if (overlaps) {
                        GLFW.glfwSetCursor(window.getHandle(), RESIZE_CURSOR);
                        break rectLoop;
                    } else {
                        GLFW.glfwSetCursor(window.getHandle(), 0);
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
        Collection<StatusBar> values = FancyStatusBars.statusBars.values();
        values.forEach(this::setup);
        checkZeroCoordinates(values);
        updateScreenRects();
        topBarZone = new ScreenRect(width / 2 - 91, height - 22 - 15, 182, 15);
        bottomLeftBarZone = new ScreenRect(width / 2 - 91 - 20, height - 22, 20, 22);
        bottomRightBarZone = new ScreenRect(width / 2 + 91, height - 22, 20, 22);
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
        GLFW.glfwSetCursor(((WindowAccessor) (Object) client.getWindow()).getHandle(), 0);
        GLFW.glfwDestroyCursor(RESIZE_CURSOR); // Does it explode if I don't do that?? idk aaaaaaaaa
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void onClick(StatusBar statusBar) {
        cursorBar = statusBar;
        FancyStatusBars.barGrid.remove(statusBar.gridX, statusBar.gridY);
        FancyStatusBars.updatePositions();
        updateScreenRects();
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
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
