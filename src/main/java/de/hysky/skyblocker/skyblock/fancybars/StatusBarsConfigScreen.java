package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.skyblock.FancyStatusBars;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StatusBarsConfigScreen extends Screen {

    private static final Identifier HOTBAR_TEXTURE = new Identifier("hud/hotbar");

    private final Map<ScreenRect, int[]> meaningFullName = new HashMap<>();

    private @Nullable StatusBar cursorBar = null;

    public StatusBarsConfigScreen() {
        super(Text.of("Status Bars Config"));
        FancyStatusBars.updatePositions();
    }

    private final int[] currentCoords = new int[]{0, 0};

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (ScreenRect screenRect : meaningFullName.keySet()) {
            context.fillGradient(screenRect.position().x(), screenRect.position().y(), screenRect.position().x() + screenRect.width(), screenRect.position().y() + screenRect.height(), 0xFFFF0000, 0xFF0000FF);
        }
        super.render(context, mouseX, mouseY, delta);
        context.drawGuiTexture(HOTBAR_TEXTURE, width / 2 - 91, height - 22, 182, 22);
        if (cursorBar != null) {
            cursorBar.renderCursor(context, mouseX, mouseY, delta);
            ScreenRect mouseRect = new ScreenRect(new ScreenPos(mouseX - 1, mouseY - 1), 3, 3);

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
                for (NavigationDirection value : NavigationDirection.values()) {
                    boolean overlaps = screenRect.getBorder(value).add(value).overlaps(mouseRect);

                    if (overlaps) {
                        int[] ints = meaningFullName.get(screenRect);
                        if (ints[0] != currentCoords[0] || ints[1] != currentCoords[1]) {
                            currentCoords[0] = ints[0];
                            currentCoords[1] = ints[1];
                            System.out.println("Moving " + cursorBar);
                            System.out.println(ints[0] + " " + ints[1]);
                            FancyStatusBars.barGrid.remove(cursorBar.gridX, cursorBar.gridY);

                            int offset;
                            if (value.getAxis().equals(NavigationAxis.VERTICAL)) {
                                if (!value.isPositive()) {
                                    offset = ints[1] > 0 ? 1 : 0;
                                } else {
                                    offset = ints[1] > 0 ? 0 : -1;
                                }

                                FancyStatusBars.barGrid.addRow(ints[1] + offset, ints[0] > 0);
                                FancyStatusBars.barGrid.add(ints[0] < 0 ? -1 : 1, ints[1] + offset, cursorBar);
                            } else {
                                if (value.isPositive()) {
                                    offset = ints[0] > 0 ? 1 : 0;
                                } else {
                                    offset = ints[0] > 0 ? 0 : -1;
                                }

                                FancyStatusBars.barGrid.add(ints[0] + offset, ints[1], cursorBar);
                            }
                            FancyStatusBars.updatePositions();
                            System.out.println("After " + cursorBar);
                        }
                        break rectLoop;
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

    @Override
    public void removed() {
        super.removed();
        FancyStatusBars.statusBars.values().forEach(statusBar -> statusBar.setOnClick(null));
        FancyStatusBars.updatePositions();
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
