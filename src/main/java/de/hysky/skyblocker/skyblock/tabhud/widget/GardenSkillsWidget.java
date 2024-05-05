package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about your skills while in the garden

public class GardenSkillsWidget extends HudWidget {

    private static final MutableText TITLE = Text.literal("Skill Info").formatted(Formatting.YELLOW,
            Formatting.BOLD);

    // match the skill entry
    // group 1: skill name and level
    // group 2: progress to next level (without "%")
    private static final Pattern SKILL_PATTERN = Pattern
            .compile("Skills: (?<skill>[A-Za-z]* [0-9]*): (?<progress>[0-9.MAX]*)%?");

    private static final Pattern GARDEN_LEVEL_PATTERN = Pattern.compile("Garden Level: (?<level>[IVX0-9]+)(?: \\((?<progress>[0-9.]+)% to [IVX0-9]+\\))?");

    // same, more or less
    private static final Pattern MS_PATTERN = Pattern
            .compile("Milestone: (?<milestone>[A-Za-z ]* [0-9]*): (?<progress>[0-9.]*)%");

    public GardenSkillsWidget() {
        super(TITLE, Formatting.YELLOW.getColorValue());
    }

    @Override
    public void updateContent() {
        ProgressComponent spc;
        Matcher skillMatcher = PlayerListMgr.regexAt(66, SKILL_PATTERN);
        if (skillMatcher == null) {
        	spc = new ProgressComponent();
        } else {

            String strpcnt = skillMatcher.group("progress");
            String skill = skillMatcher.group("skill");

            if (strpcnt.equals("MAX")) {
                spc = new ProgressComponent(Ico.LANTERN, Text.of(skill), Text.of("MAX"), 100f,
                        Formatting.RED.getColorValue());
            } else {
                float pcnt = Float.parseFloat(strpcnt);
                spc = new ProgressComponent(Ico.LANTERN, Text.of(skill), pcnt,
                        Formatting.GOLD.getColorValue());
            }
        }

        this.addComponent(spc);

        ProgressComponent glpc;
        Matcher glMatcher = PlayerListMgr.regexAt(45, GARDEN_LEVEL_PATTERN);

        if (glMatcher == null) {
            glpc = new ProgressComponent();
        } else {
            String level = glMatcher.group("level");

            if (level.equals("15") || level.equals("XV")) {
                glpc = new ProgressComponent(Ico.SEEDS, Text.literal("Garden Level " + level), 100f, Formatting.RED.getColorValue());
            } else {
            	String strpcnt = glMatcher.group("progress");
            	float pcnt = Float.parseFloat(strpcnt);

                glpc = new ProgressComponent(Ico.SEEDS, Text.literal("Garden Level " + level), pcnt, Formatting.DARK_GREEN.getColorValue());
            }
        }

        this.addComponent(glpc);

        Text speed = HudWidget.simpleEntryText(67, "SPD", Formatting.WHITE);
        IcoTextComponent spd = new IcoTextComponent(Ico.SUGAR, speed);
        Text farmfort = HudWidget.simpleEntryText(68, "FFO", Formatting.GOLD);
        IcoTextComponent ffo = new IcoTextComponent(Ico.IRON_HOE, farmfort);

        TableComponent tc = new TableComponent(2, 1, Formatting.YELLOW.getColorValue());
        tc.addToCell(0, 0, spd);
        tc.addToCell(1, 0, ffo);
        this.addComponent(tc);
        
        this.addComponent(new IcoTextComponent(Ico.IRON_HOE, PlayerListMgr.textAt(70)));

        ProgressComponent pc2;
        Matcher milestoneMatcher = PlayerListMgr.regexAt(69, MS_PATTERN);
        if (milestoneMatcher == null) {
            pc2 = new ProgressComponent();
        } else {
            String strpcnt = milestoneMatcher.group("progress");
            String milestone = milestoneMatcher.group("milestone");

            float pcnt = Float.parseFloat(strpcnt);
            pc2 = new ProgressComponent(Ico.MILESTONE, Text.of(milestone), pcnt,
                    Formatting.GREEN.getColorValue());
        }
        this.addComponent(pc2);
    }
}
