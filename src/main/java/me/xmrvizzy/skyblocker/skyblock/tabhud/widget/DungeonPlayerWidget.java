package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
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
    private static final Pattern PLAYER_PATTERN = Pattern
            .compile("\\[\\d*\\] (?<name>.*) \\((?<class>\\S*) ?(?<level>[LXVI]*)\\)");

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
        MSGS.add("More is better!");
    }

    // title needs to be changeable here
    public DungeonPlayerWidget(int player) {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());

        int start = 1 + (player - 1) * 4;

        if (PlayerListMgr.strAt(start) == null) {
            int idx = player - 2;
            IcoTextComponent noplayer = new IcoTextComponent(Ico.SIGN,
                    Text.literal(MSGS.get(idx)).formatted(Formatting.GRAY));
            this.addComponent(noplayer);
            this.pack();
            return;
        }
        Matcher m = PlayerListMgr.regexAt(start, PLAYER_PATTERN);
        if (m == null) {
            this.addComponent(new IcoTextComponent());
            this.addComponent(new IcoTextComponent());
        } else {

            Text name = Text.literal("Name: ").append(Text.literal(m.group("name")).formatted(Formatting.YELLOW));
            this.addComponent(new IcoTextComponent(Ico.PLAYER, name));

            String cl = m.group("class");
            Formatting clf = Formatting.GRAY;
            ItemStack cli = Ico.BARRIER;
            if (!cl.equals("EMPTY")) {
                cli = ICOS.get(cl);
                clf = Formatting.LIGHT_PURPLE;
                cl += " " + m.group("level");
            }

            Text clazz = Text.literal("Class: ").append(Text.literal(cl).formatted(clf));
            IcoTextComponent itclass = new IcoTextComponent(cli, clazz);
            this.addComponent(itclass);
        }

        this.addSimpleIcoText(Ico.CLOCK, "Ult Cooldown:", Formatting.GOLD, start + 1);
        this.addSimpleIcoText(Ico.POTION, "Revives:", Formatting.DARK_PURPLE, start + 2);

        this.pack();

    }
}
