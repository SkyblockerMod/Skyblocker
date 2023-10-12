package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about the garden server

public class GardenServerWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Server Info").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    // match the next visitor in the garden
    // group 1: visitor name
    private static final Pattern VISITOR_PATTERN = Pattern.compile("Next Visitor: (?<vis>.*)");

    public GardenServerWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent() {
        this.addSimpleIcoText(Ico.MAP, "Area:", Formatting.DARK_AQUA, 41);
        this.addSimpleIcoText(Ico.NTAG, "Server ID:", Formatting.GRAY, 42);
        this.addSimpleIcoText(Ico.EMERALD, "Gems:", Formatting.GREEN, 43);
        this.addSimpleIcoText(Ico.COPPER, "Copper:", Formatting.GOLD, 44);

        Matcher m = PlayerListMgr.regexAt(45, VISITOR_PATTERN);
        if (m == null ) {
            this.addComponent(new IcoTextComponent());
            return;
        }

        String vis = m.group("vis");
        Formatting col;
        if (vis.equals("Not Unlocked!")) {
            col = Formatting.RED;
        } else {
            col = Formatting.GREEN;
        }
        Text visitor = Widget.simpleEntryText(vis, "Next Visitor: ", col);
        IcoTextComponent v = new IcoTextComponent(Ico.PLAYER, visitor);
        this.addComponent(v);
    }

}
