package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.TableComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about your skills while in the garden

public class GardenSkillsWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Skill Info").formatted(Formatting.YELLOW,
            Formatting.BOLD);

    // match the skill entry
    // group 1: skill name and level
    // group 2: progress to next level (without "%")
    private static final Pattern SKILL_PATTERN = Pattern.compile("\\S*: ([A-Za-z]* [0-9]*): (\\S*)%");
    // same, but with leading space
    private static final Pattern MS_PATTERN = Pattern.compile(" \\S*: ([A-Za-z]* [0-9]*): (\\S*)%");

    public GardenSkillsWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.YELLOW.getColorValue());

        Matcher m = StrMan.regexAt(list, 66, SKILL_PATTERN);

        float pcnt = Float.parseFloat(m.group(2));
        String skill = m.group(1);

        ProgressComponent pc = new ProgressComponent(Ico.LANTERN, Text.of(skill), pcnt, Formatting.GOLD.getColorValue());
        this.addComponent(pc);

        Text speed = StrMan.stdEntry(list, 67, "SPD", Formatting.WHITE);
        IcoTextComponent spd = new IcoTextComponent(Ico.SUGAR, speed);
        Text farmfort = StrMan.stdEntry(list, 68, "FFO", Formatting.GOLD);
        IcoTextComponent ffo = new IcoTextComponent(Ico.HOE, farmfort);

        TableComponent tc = new TableComponent(2, 1, Formatting.YELLOW.getColorValue());
        tc.addToCell(0, 0, spd);
        tc.addToCell(1, 0, ffo);
        this.addComponent(tc);

        m = StrMan.regexAt(list, 69, MS_PATTERN);
        pcnt = Float.parseFloat(m.group(2));
        skill = m.group(1);

        ProgressComponent pc2 = new ProgressComponent(Ico.MILESTONE, Text.of(skill), pcnt, Formatting.GREEN.getColorValue());
        this.addComponent(pc2);

        this.pack();
    }

}
