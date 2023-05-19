package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows your crimson isle faction quests

public class QuestWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Faction Quests").formatted(Formatting.AQUA,
            Formatting.BOLD);

    public QuestWidget() {
        super(TITLE, Formatting.AQUA.getColorValue());

        for (int i = 51; i < 56; i++) {
            Text q = Widget.plainEntryText(i);
            IcoTextComponent itc = new IcoTextComponent(Ico.BOOK, q);
            this.addComponent(itc);
        }
        this.pack();

    }

}
