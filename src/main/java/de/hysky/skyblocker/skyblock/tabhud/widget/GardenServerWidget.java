package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about the garden server

public class GardenServerWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Server Info").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);
    //From the armor trim tooltip
    private static final int COPPER_COLOR = 0xB4684D;
    private static final int GREEN_COLOR = 0x55FF55;

    // match the next visitor in the garden
    // group 1: visitor name
    private static final Pattern VISITOR_PATTERN = Pattern.compile("Visitors: (?<vis>.*)");

    private static final Pattern GARDEN_LEVEL_PATTERN =
            Pattern.compile("Garden Level: (?<cur>\\d+) \\((?<prog>\\d+)% to (?<next>\\d+)\\)");
    // No progress on max level
    private static final Pattern MAX_GARDEN_LEVEL_PATTERN = Pattern.compile("Garden Level: (?<cur>\\d+)");

    public GardenServerWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    private static Text parseCopper() {
        Text copperText = Widget.simpleEntryText(44, "Copper:", Formatting.WHITE);
        ((MutableText) copperText.getSiblings().get(0)).withColor(COPPER_COLOR);
        return copperText;
    }

    private static IcoTextComponent parseVisitors(Matcher m) {
        if (m == null) {
            return new IcoTextComponent();
        }
        String vis = m.group("vis").replaceAll("[()]*", "");
        Formatting col;
        if (vis.equals("Not Unlocked!") || vis.equals("Queue Full!")) {
            col = Formatting.RED;
        } else {
            col = Formatting.GREEN;
        }
        Text visitor = Widget.simpleEntryText(vis, "Next Visitor: ", col);
        return new IcoTextComponent(Ico.PLAYER, visitor);
    }

    private static ProgressComponent parseGardenLevel() {
        Matcher gardenLvlMatcher = PlayerListMgr.regexAt(45, GARDEN_LEVEL_PATTERN);

        if (gardenLvlMatcher == null) {
            Matcher maxGardenLvlMatcher = PlayerListMgr.regexAt(45, MAX_GARDEN_LEVEL_PATTERN);
            if (maxGardenLvlMatcher == null) return new ProgressComponent();

            String currentLvl = maxGardenLvlMatcher.group("cur");
            Text lvlText = Text.literal("Garden Lvl: " + currentLvl);
            Text progressText = Text.literal(100 + "%");
            return new ProgressComponent(Ico.SUNFLOWER, lvlText, progressText, 100, GREEN_COLOR);
        }

        String currentLvl = gardenLvlMatcher.group("cur");
        int progress = Integer.parseInt(gardenLvlMatcher.group("prog"));
        String nextLvl = gardenLvlMatcher.group("next");

        Text lvlText = Text.literal("Garden Lvl: " + currentLvl + " -> " + nextLvl);
        Text progressText = Text.literal(progress + "%");

        return new ProgressComponent(Ico.SUNFLOWER, lvlText, progressText, progress, GREEN_COLOR);
    }

    @Override
    public void updateContent() {
        this.addSimpleIcoText(Ico.MAP, "Area:", Formatting.DARK_AQUA, 41);
        this.addSimpleIcoText(Ico.NTAG, "Server ID:", Formatting.GRAY, 42);
        this.addSimpleIcoText(Ico.EMERALD, "Gems:", Formatting.GREEN, 43);
        this.addComponent(new IcoTextComponent(Ico.COPPER, parseCopper()));
        this.addComponent(parseGardenLevel());

        boolean hasPesthunterBonus = PlayerListMgr.strAt(46) != null;

        if (hasPesthunterBonus) {
            this.addComponent(new IcoTextComponent(Ico.NETHERITE_UPGRADE_ST, PlayerListMgr.textAt(46)));
        }

        int offset = hasPesthunterBonus ? 1 : 0;
        this.addComponent(parseVisitors(PlayerListMgr.regexAt(53 + offset, VISITOR_PATTERN)));
    }

}
