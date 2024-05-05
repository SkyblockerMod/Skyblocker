package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Pattern;

// this widget shows info about the secrets of the dungeon

public class DungeonSecretWidget extends HudWidget {

    private static final MutableText TITLE = Text.literal("Discoveries").formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
    private static final Pattern DISCOVERIES = Pattern.compile("Discoveries: (\\d+)");

    public DungeonSecretWidget() {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
    }

    @Override
    public void updateContent() {
        if (!DungeonScore.isDungeonStarted()) {
            this.addSimpleIcoText(Ico.CHEST, "Secrets:", Formatting.YELLOW, 30);
            this.addSimpleIcoText(Ico.SKULL, "Crypts:", Formatting.YELLOW, 31);
        } else if (PlayerListMgr.regexAt(31, DISCOVERIES) != null) {
            this.addSimpleIcoText(Ico.CHEST, "Secrets:", Formatting.YELLOW, 32);
            this.addSimpleIcoText(Ico.SKULL, "Crypts:", Formatting.YELLOW, 33);
        } else {
            this.addSimpleIcoText(Ico.CHEST, "Secrets:", Formatting.YELLOW, 31);
            this.addSimpleIcoText(Ico.SKULL, "Crypts:", Formatting.YELLOW, 32);
        }
    }
}
