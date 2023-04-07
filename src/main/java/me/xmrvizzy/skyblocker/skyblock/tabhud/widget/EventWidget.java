package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about ongoing events (e.g. jacob's farming)

public class EventWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Event Info").formatted(Formatting.YELLOW, Formatting.BOLD);

    public EventWidget(List<PlayerListEntry> list, boolean isInGarden) {
        super(TITLE, Formatting.YELLOW.getColorValue());

        // hypixel devs carefully inserting the most random edge cases #317:
        // the event info is placed a bit differently when in the garden.
        int offset = (isInGarden) ? -1 : 0;

        Text eventName = StrMan.stdEntry(list, 73 + offset, "Name:", Formatting.YELLOW);
        IcoTextComponent event = new IcoTextComponent(Ico.NTAG, eventName);
        this.addComponent(event);

        // this could look better
        Text time = StrMan.plainEntry(list, 74 + offset);
        IcoTextComponent t = new IcoTextComponent(Ico.CLOCK, time);
        this.addComponent(t);
        this.pack();
    }

}
