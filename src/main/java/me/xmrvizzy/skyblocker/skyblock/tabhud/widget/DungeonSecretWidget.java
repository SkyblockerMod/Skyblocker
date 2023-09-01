package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about the secrets of the dungeon

public class DungeonSecretWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Discoveries").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    public DungeonSecretWidget() {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
    }

    @Override
    public void updateContent() {
        this.addSimpleIcoText(Ico.CHEST, "Secrets:", Formatting.YELLOW, 31);
        this.addSimpleIcoText(Ico.SKULL, "Crypts:", Formatting.YELLOW, 32);

    }

}
