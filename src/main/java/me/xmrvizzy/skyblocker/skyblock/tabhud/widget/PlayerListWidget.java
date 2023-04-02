package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlayerComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.TableComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows a list of players with their skins
// in most areas

public class PlayerListWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Players").formatted(Formatting.GREEN,
            Formatting.BOLD);

    private ArrayList<PlayerListEntry> list = new ArrayList<>();

    public PlayerListWidget(List<PlayerListEntry> l) {
        super(TITLE, Formatting.GREEN.getColorValue());

        // unintuitive int ceil division stolen from
        // https://stackoverflow.com/questions/7139382/java-rounding-up-to-an-int-using-math-ceil#21830188
        int tblW = ((l.size() - 80) - 1) / 20 + 1;

        TableComponent tc = new TableComponent(tblW, (l.size() - 80 >= 20) ? 20 : l.size() - 80,
                Formatting.GREEN.getColorValue());

        for (int i = 80; i < l.size(); i++) {
            list.add(l.get(i));
        }

        Collections.sort(list, new Comparator<PlayerListEntry>() {
            @Override
            public int compare(PlayerListEntry o1, PlayerListEntry o2) {
                return o1.getProfile().getName().toLowerCase().compareTo(o2.getProfile().getName().toLowerCase());
            }
        });

        int x = 0, y = 0;

        for (PlayerListEntry ple : list) {
            tc.addToCell(x, y, new PlayerComponent(ple));
            y++;
            if (y >= 20) {
                y = 0;
                x++;
            }
        }

        this.addComponent(tc);
        this.pack();
    }
}
