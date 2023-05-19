package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about... something?
// related to downed people in dungeons, not sure what this is supposed to show

public class DungeonDownedWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Downed").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    public DungeonDownedWidget() {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
        Formatting format = Formatting.RED;
        String down = PlayerListMgr.strAt(21);
        if (down != null && down.endsWith("NONE")) {
            format = Formatting.GRAY;
        }
        int idx = down.indexOf(": ");
        Text downed = (down == null || idx== -1) ? null : Widget.simpleEntryText(down.substring(idx + 2), "Downed:", format);
        IcoTextComponent d = new IcoTextComponent(Ico.SKULL, downed);
        this.addComponent(d);

        this.addSimpleIcoText(Ico.CLOCK, "Time:", Formatting.GRAY, 22);
        this.addSimpleIcoText(Ico.POTION, "Revive:", Formatting.GRAY, 23);
        this.pack();
    }

}
