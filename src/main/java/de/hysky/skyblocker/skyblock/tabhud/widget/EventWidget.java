package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about ongoing events (e.g. election)

public class EventWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Event Info").formatted(Formatting.YELLOW, Formatting.BOLD);

    private final boolean isInGarden;

    public EventWidget(boolean isInGarden) {
        super(TITLE, Formatting.YELLOW.getColorValue());
        this.isInGarden = isInGarden;
    }

    @Override
    public void updateContent() {
        // hypixel devs carefully inserting the most random edge cases #317:
        // the event info is placed a bit differently when in the garden.
        int offset = (isInGarden) ? -1 : 0;

        this.addSimpleIcoText(Ico.NTAG, "Name:", Formatting.YELLOW, 73 + offset);

        // this could look better
        Text time = Widget.plainEntryText(74 + offset);
        IcoTextComponent t = new IcoTextComponent(Ico.CLOCK, time);
        this.addComponent(t);
    }

}
