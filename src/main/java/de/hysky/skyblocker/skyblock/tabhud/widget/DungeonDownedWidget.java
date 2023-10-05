package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
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
    }

    @Override
    public void updateContent() {
        String down = PlayerListMgr.strAt(21);
        if (down == null) {
            this.addComponent(new IcoTextComponent());
        } else {

            Formatting format = Formatting.RED;
            if (down.endsWith("NONE")) {
                format = Formatting.GRAY;
            }
            int idx = down.indexOf(": ");
            Text downed = (idx == -1) ? null
                    : Widget.simpleEntryText(down.substring(idx + 2), "Downed: ", format);
            IcoTextComponent d = new IcoTextComponent(Ico.SKULL, downed);
            this.addComponent(d);
        }

        this.addSimpleIcoText(Ico.CLOCK, "Time:", Formatting.GRAY, 22);
        this.addSimpleIcoText(Ico.POTION, "Revive:", Formatting.GRAY, 23);
    }

}
