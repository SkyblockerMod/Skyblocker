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

import java.util.HashMap;
import java.util.Map;

public class StatusBarsConfigScreen extends Screen {

    private static final Identifier HOTBAR_TEXTURE = new Identifier("hud/hotbar");

    private final Map<ScreenRect, int[]> meaningFullName = new HashMap<>();

    private @Nullable StatusBar cursorBar = null;
    protected StatusBarsConfigScreen(Text title) {
        super(title);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawGuiTexture(HOTBAR_TEXTURE, width/2 - 91, height-22, 182, 22);
        if (cursorBar != null) {
            cursorBar.setX(mouseX);
            cursorBar.setY(mouseY);
            cursorBar.render(context, mouseX, mouseY, delta);

            for (ScreenRect screenRect : meaningFullName.keySet()) {
                for (NavigationDirection value : NavigationDirection.values()) {
                    boolean overlaps = screenRect.getBorder(value).overlaps(new ScreenRect(new ScreenPos(mouseX - 1, mouseY - 1), 3, 3));
                    if (overlaps) {
                        int[] ints = meaningFullName.get(screenRect);
                        if (ints[0] != cursorBar.gridX || ints[1] != cursorBar.gridY) {
                            FancyStatusBars.barGrid.remove(cursorBar.gridX, cursorBar.gridY);
                            if (value.getAxis().equals(NavigationAxis.VERTICAL)) {
                                if (value.isPositive()) {
                                    FancyStatusBars.barGrid.addRow(ints[1]+1, ints[0]>0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        FancyStatusBars.statusBars.values().forEach(this::setup);
    }

    private void setup(StatusBar statusBar) {
        this.addDrawableChild(statusBar);
        statusBar.setOnClick(this::onClick);
    }

    private void onClick(StatusBar statusBar) {
        cursorBar = statusBar;
        meaningFullName.clear();
        FancyStatusBars.barGrid.remove(statusBar.gridX, statusBar.gridY);
        FancyStatusBars.statusBars.values().forEach(statusBar1 -> {
            meaningFullName.put(
                    new ScreenRect(new ScreenPos(statusBar1.getX(), statusBar1.getY()), statusBar1.getWidth(), statusBar1.getHeight()),
                    new int[]{statusBar1.gridX, statusBar1.gridY});
        });
    }
}
