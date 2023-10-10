package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows your crimson isle faction quests

public class QuestWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Faction Quests").formatted(Formatting.AQUA,
            Formatting.BOLD);

    public QuestWidget() {
        super(TITLE, Formatting.AQUA.getColorValue());

    }

    @Override
    public void updateContent() {
        for (int i = 51; i < 56; i++) {
            Text q = PlayerListMgr.textAt(i);
            IcoTextComponent itc = new IcoTextComponent(Ico.BOOK, q);
            this.addComponent(itc);
        }

    }

}
