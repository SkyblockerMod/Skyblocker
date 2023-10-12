package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlayerComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Comparator;

// this widget shows a list of players with their skins.
// responsible for non-private-island areas

public class PlayerListWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Players").formatted(Formatting.GREEN,
            Formatting.BOLD);

    public PlayerListWidget() {
        super(TITLE, Formatting.GREEN.getColorValue());

    }

    @Override
    public void updateContent() {
        ArrayList<PlayerListEntry> list = new ArrayList<>();

        // hard cap to 4x20 entries.
        // 5x20 is too wide (and not possible in theory. in reality however...)
        int listlen = Math.min(PlayerListMgr.getSize(), 160);

        // list isn't fully loaded, so our hack won't work...
        if (listlen < 80) {
            this.addComponent(new PlainTextComponent(Text.literal("List loading...").formatted(Formatting.GRAY)));
            return;
        }

        // unintuitive int ceil division stolen from
        // https://stackoverflow.com/questions/7139382/java-rounding-up-to-an-int-using-math-ceil#21830188
        int tblW = ((listlen - 80) - 1) / 20 + 1;

        TableComponent tc = new TableComponent(tblW, Math.min(listlen - 80, 20), Formatting.GREEN.getColorValue());

        for (int i = 80; i < listlen; i++) {
            list.add(PlayerListMgr.getRaw(i));
        }

        if (SkyblockerConfigManager.get().general.tabHud.nameSorting == SkyblockerConfig.NameSorting.ALPHABETICAL) {
            list.sort(Comparator.comparing(o -> o.getProfile().getName().toLowerCase()));
        }

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
    }
}
