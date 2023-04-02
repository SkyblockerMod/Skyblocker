package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about... something?
// TODO: test this.

public class DungeonDownedWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Downed").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    public DungeonDownedWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
        Formatting format = Formatting.RED;
        if (StrMan.strAt(list, 21).endsWith("NONE")) {
            format = Formatting.GRAY;
        }
        Text downed = StrMan.stdEntry(list, 21, "Downed:", format);
        IcoTextComponent down = new IcoTextComponent(Ico.SKULL, downed);
        this.addComponent(down);

        Text time = StrMan.stdEntry(list, 22, "Time:", Formatting.GRAY);
        IcoTextComponent t = new IcoTextComponent(Ico.CLOCK, time);
        this.addComponent(t);

        Text revive = StrMan.stdEntry(list, 23, "Revive:", Formatting.GRAY);
        IcoTextComponent rev = new IcoTextComponent(Ico.POTION, revive);
        this.addComponent(rev);
        this.pack();
    }

}
