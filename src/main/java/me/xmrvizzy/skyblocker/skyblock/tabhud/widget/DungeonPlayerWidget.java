package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about a player in the current dungeon group

public class DungeonPlayerWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Player").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    // match a player entry
    // group 1: name
    // group 2: class (or literal "EMPTY" pre dungeon start)
    // group 3: level (or nothing, if pre dungeon start)
    // as a side effect, this regex keeps the iron man icon in the name
    // not sure if that should be
    private static final Pattern PLAYER_PATTERN = Pattern.compile("\\[\\d*\\] (.*) \\((\\S*) ?([LXVI]*)\\)");

    private static final HashMap<String, ItemStack> ICOS = new HashMap<>();
    private static final ArrayList<String> MSGS = new ArrayList<>();
    static {
        ICOS.put("Tank", Ico.CHESTPLATE);
        ICOS.put("Mage", Ico.B_ROD);
        ICOS.put("Berserk", Ico.DIASWORD);
        ICOS.put("Archer", Ico.BOW);
        ICOS.put("Healer", Ico.POTION);

        MSGS.add("PRESS A TO JOIN");
        MSGS.add("Invite a friend!");
        MSGS.add("But nobody came.");
    }

    // title needs to be changeable here
    public DungeonPlayerWidget(List<PlayerListEntry> list, int player) {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());

        int start = 1 + (player - 1) * 4;

        if (list.get(start).getDisplayName().getString().length() < 2) {
            this.addComponent(
                    new IcoTextComponent(Ico.SIGN, Text.literal(MSGS.get((int)(Math.random()*MSGS.size()))).formatted(Formatting.GRAY)));
        } else {
            Matcher m = StrMan.regexAt(list, start, PLAYER_PATTERN);

            Text name = Text.literal("Name: ").append(Text.literal(m.group(1)).formatted(Formatting.YELLOW));
            this.addComponent(new IcoTextComponent(Ico.PLAYER, name));

            String cl = m.group(2);
            Formatting clf = Formatting.GRAY;
            ItemStack cli = Ico.BARRIER;
            if (!cl.equals("EMPTY")) {
                cli = ICOS.get(cl);
                clf = Formatting.LIGHT_PURPLE;
                cl += " " + m.group(3);
            }

            Text class_ = Text.literal("Class: ").append(Text.literal(cl).formatted(clf));
            IcoTextComponent itclass = new IcoTextComponent(cli, class_);
            this.addComponent(itclass);

            Text ult = StrMan.stdEntry(list, start + 1, "Ult Cooldown:", Formatting.GOLD);
            IcoTextComponent ul = new IcoTextComponent(Ico.CLOCK, ult);
            this.addComponent(ul);

            Text revive = StrMan.stdEntry(list, start + 2, "Revives:", Formatting.DARK_PURPLE);
            IcoTextComponent re = new IcoTextComponent(Ico.POTION, revive);
            this.addComponent(re);

        }
        this.pack();

    }
}
