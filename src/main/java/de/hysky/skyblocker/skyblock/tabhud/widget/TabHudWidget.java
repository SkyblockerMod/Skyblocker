package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class TabHudWidget extends ComponentBasedWidget {
    private final String hypixelWidgetName;
    private final List<Component> cachedComponents = new ArrayList<>();


    public TabHudWidget(String hypixelWidgetName, MutableText title, Integer colorValue) {
        super(title, colorValue, hypixelWidgetName.toLowerCase().replace(' ', '_').replace("'", ""));
        this.hypixelWidgetName = hypixelWidgetName;
    }

    public String getHypixelWidgetName() {
        return hypixelWidgetName;
    }

    @Override
    public String getNiceName() {
        return getHypixelWidgetName();
    }

    @Override
    public void updateContent() {
        cachedComponents.forEach(super::addComponent);
    }

    public void updateFromTab(List<Text> lines) {
        cachedComponents.clear();
        updateContent(lines);
    }

    @Override
    public boolean shouldRender(Location location) {
        return false;
    }

    /**
     * Update the content from the hypixel widget's lines
     *
     * @param lines the lines, they are formatted and trimmed, no blank lines will be present.
     *              If the vanilla tab widget has text right after the : they will be put on the first line.
     */
    protected abstract void updateContent(List<Text> lines);

    @Override
    public final void addComponent(Component c) {
        cachedComponents.add(c);
    }

}
