package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows various dungeon info
// deaths, healing, dmg taken, milestones

public class DungeonDeathWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Death").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    // match the deaths entry
    // group 1: amount of deaths
    private static final Pattern DEATH_PATTERN = Pattern.compile("Team Deaths: (?<deathnum>\\d+).*");

    public DungeonDeathWidget() {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
    }

    @Override
    public void updateContent() {
        Matcher m = PlayerListMgr.regexAt(25, DEATH_PATTERN);
        if (m == null) {
            this.addComponent(new IcoTextComponent());
        } else {
            Formatting f = (m.group("deathnum").equals("0")) ? Formatting.GREEN : Formatting.RED;
            Text d = Widget.simpleEntryText(m.group("deathnum"), "Deaths: ", f);
            IcoTextComponent deaths = new IcoTextComponent(Ico.SKULL, d);
            this.addComponent(deaths);
        }

        this.addSimpleIcoText(Ico.SWORD, "Damage Dealt:", Formatting.RED, 26);
        this.addSimpleIcoText(Ico.POTION, "Healing Done:", Formatting.RED, 27);
        this.addSimpleIcoText(Ico.NTAG, "Milestone:", Formatting.YELLOW, 28);

    }

}
