package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class TabHudWidget extends HudWidget {
    private final String hypixelWidgetName;
    private final List<Component> cachedComponents = new ArrayList<>();


    public TabHudWidget(String hypixelWidgetName, MutableText title, Integer colorValue) {
        super(title, colorValue);
        this.hypixelWidgetName = hypixelWidgetName;
    }

    public String getHypixelWidgetName() {
        return hypixelWidgetName;
    }

    @Override
    public void updateContent() {
        cachedComponents.forEach(super::addComponent);
    }

    public void updateFromTab(List<Text> lines) {
        cachedComponents.clear();
        updateContent(lines);
    }

    /**
     * Update the content from the hypixel widget's lines
     *
     * @param lines the lines, they are formatted, no blank lines will be present
     */
    protected abstract void updateContent(List<Text> lines);

    @Override
    public final void addComponent(Component c) {
        cachedComponents.add(c);
    }
}
