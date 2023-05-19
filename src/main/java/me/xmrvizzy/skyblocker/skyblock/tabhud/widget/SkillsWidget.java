package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.Component;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoFatTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.TableComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about a skill and some stats,
// as seen in the rightmost column of the default HUD

public class SkillsWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Skill Info").formatted(Formatting.YELLOW,
            Formatting.BOLD);

    // match the skill entry
    // group 1: skill name and level
    // group 2: progress to next level (without "%")
    private static final Pattern SKILL_PATTERN = Pattern.compile("\\S*: ([A-Za-z]* [0-9]*): ([0-9.MAX]*)%?");

    public SkillsWidget() {
        super(TITLE, Formatting.YELLOW.getColorValue());

        Matcher m = PlayerListMgr.regexAt(66, SKILL_PATTERN);
        Component progress;
        if (m == null) {
            progress = new ProgressComponent();
        } else {

            String skill = m.group(1);
            String pcntStr = m.group(2);

            if (!pcntStr.equals("MAX")) {
                float pcnt = Float.parseFloat(pcntStr);
                progress = new ProgressComponent(Ico.LANTERN, Text.of(skill),
                        Text.of(pcntStr), pcnt, Formatting.GOLD.getColorValue());
            } else {
                progress = new IcoFatTextComponent(Ico.LANTERN, Text.of(skill),
                        Text.literal(pcntStr).formatted(Formatting.RED));
            }
        }

        this.addComponent(progress);

        Text speed = Widget.simpleEntryText(67, "SPD", Formatting.WHITE);
        IcoTextComponent spd = new IcoTextComponent(Ico.SUGAR, speed);
        Text strength = Widget.simpleEntryText(68, "STR", Formatting.RED);
        IcoTextComponent str = new IcoTextComponent(Ico.SWORD, strength);
        Text critDmg = Widget.simpleEntryText(69, "CCH", Formatting.BLUE);
        IcoTextComponent cdg = new IcoTextComponent(Ico.SWORD, critDmg);
        Text critCh = Widget.simpleEntryText(70, "CDG", Formatting.BLUE);
        IcoTextComponent cch = new IcoTextComponent(Ico.SWORD, critCh);
        Text aSpeed = Widget.simpleEntryText(71, "ASP", Formatting.YELLOW);
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
