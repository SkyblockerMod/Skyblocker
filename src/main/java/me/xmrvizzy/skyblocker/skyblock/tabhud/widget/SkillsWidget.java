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

// this widget shows info about a skill and some stats

public class SkillsWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Skill Info").formatted(Formatting.YELLOW,
            Formatting.BOLD);

    // match the skill entry
    // group 1: skill name and level
    // group 2: progress to next level (without "%")
    private static final Pattern SKILL_PATTERN = Pattern.compile("\\S*: ([A-Za-z]* [0-9]*): (\\S*)%");

    public SkillsWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.YELLOW.getColorValue());

        Matcher m = StrMan.regexAt(list, 66, SKILL_PATTERN);

        float pcnt = Float.parseFloat(m.group(2));
        String skill = m.group(1);

        ProgressComponent pc = new ProgressComponent(Ico.LANTERN, Text.of(skill), pcnt, Formatting.GOLD.getColorValue());

        this.addComponent(pc);

        Text speed = StrMan.stdEntry(list, 67, "SPD", Formatting.WHITE);
        IcoTextComponent spd = new IcoTextComponent(Ico.SUGAR, speed);
        Text strength = StrMan.stdEntry(list, 68, "STR", Formatting.RED);
        IcoTextComponent str = new IcoTextComponent(Ico.SWORD, strength);
        Text critDmg = StrMan.stdEntry(list, 69, "CDG", Formatting.BLUE);
        IcoTextComponent cdg = new IcoTextComponent(Ico.SWORD, critDmg);
        Text critCh = StrMan.stdEntry(list, 70, "CCH", Formatting.BLUE);
        IcoTextComponent cch = new IcoTextComponent(Ico.SWORD, critCh);
        Text aSpeed = StrMan.stdEntry(list, 71, "ASP", Formatting.YELLOW);
        IcoTextComponent asp = new IcoTextComponent(Ico.HOE, aSpeed);

        TableComponent tc = new TableComponent(2, 3, Formatting.YELLOW.getColorValue());
        tc.addToCell(0, 0, spd);
        tc.addToCell(0, 1, str);
        tc.addToCell(0, 2, asp);
        tc.addToCell(1, 0, cdg);
        tc.addToCell(1, 1, cch);
        this.addComponent(tc);
        this.pack();
    }

}
