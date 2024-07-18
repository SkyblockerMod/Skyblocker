package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.Objects;
import java.util.function.Consumer;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;

public abstract class HudWidget implements Element, Widget, Drawable {
    private final String internalID;
    protected int w = 0, h = 0;
    protected int x = 0, y = 0;


    /**
     * Most often than not this should be instantiated only once.
     * @param internalID the internal ID, for config, positioning depending on other widgets, all that good stuff
     */
    public HudWidget(String internalID) {
        this.internalID = internalID;
        ScreenMaster.widgetInstances.put(internalID, this);
    }


    public abstract boolean shouldRender(Location location);

    public abstract void update();

    public void render(DrawContext context) {
        render(context, -1, -1, MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration());
    }



    public final int getX() {
        return this.x;
    }

    public final void setX(int x) {
        this.x = x;
    }

    public final int getY() {
        return this.y;
    }

    public final void setY(int y) {
        this.y = y;
    }

    public final int getWidth() {
        return this.w;
    }

    public void setWidth(int width) {
        this.w = width;
    }

    public final int getHeight() {
        return this.h;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {}

    public void setHeight(int height) {
        this.h = height;
    }

    public void setDimensions(int size) {
        setDimensions(size, size);
    }

    public void setDimensions(int width, int height) {
        this.w = width;
        this.h = height;
    }

    private boolean focused = false;

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + getHeight();
    }

    /**
     *
     * @param object the other HudWidget
     * @return true if they are the same instance or the internal id is the same.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        HudWidget widget = (HudWidget) object;
        return Objects.equals(getInternalID(), widget.getInternalID());
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return Element.super.getNavigationFocus();
    }

    public String getInternalID() {
        return internalID;
    }

    public String getNiceName() {
        return getInternalID();
    }

    private boolean positioned = false;


    public boolean isPositioned() {
        return positioned;
    }

    public void setPositioned(boolean positioned) {
        this.positioned = positioned;
    }
}
