package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows your Crimson Isle suests

public class QuestWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Faction Quests").formatted(Formatting.AQUA,
            Formatting.BOLD);

    public QuestWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.AQUA.getColorValue());

        for (int i = 51; i<56; i++) {
            String q = StrMan.strAt(list, i).trim();
            IcoTextComponent itc = new IcoTextComponent(Ico.BOOK, Text.literal(q));
            this.addComponent(itc);
        }
        this.pack();

    }

}
