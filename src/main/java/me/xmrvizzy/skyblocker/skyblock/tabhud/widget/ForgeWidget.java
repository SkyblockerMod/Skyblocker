package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.Component;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoFatTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows what you're forging right now.
// for locked slots, the unlock requirement is shown

public class ForgeWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Forge Status").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public ForgeWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
         int forgestart = 54;
        // why is it forges and not fabrics >:(
        if (!StrMan.strAt(list, 53).startsWith("Forge")) {
            forgestart += 2;
        }


        for (int i = forgestart, slot = 0; i < forgestart + 5 && i < 60; i++,slot ++) {
            String fstr = StrMan.strAt(list, i).substring(4);
            Component c;
            Text l1, l2;

            switch (fstr) {
                case "LOCKED":
                    l1 = Text.literal("Locked").formatted(Formatting.RED);
                    l2 = switch (slot) {
                        case 3 -> Text.literal("Req. HotM 3").formatted(Formatting.GRAY);
                        case 4 -> Text.literal("Req. HotM 4").formatted(Formatting.GRAY);
                        case 5 -> Text.literal("Req. PotM 2").formatted(Formatting.GRAY);
                        default ->
                            Text.literal("This message should not appear").formatted(Formatting.RED, Formatting.BOLD);
                    };
                    c = new IcoFatTextComponent(Ico.BARRIER, l1, l2);
                    break;
                case "EMPTY":
                    l1 = Text.literal("Empty").formatted(Formatting.GRAY);
                    c = new IcoTextComponent(Ico.FURNACE, l1);
                    break;
                default:
                    String[] parts = fstr.split(": ");
                    l1 = Text.of(parts[0]);
                    l2 = Text.literal(parts[1]).formatted(Formatting.YELLOW);
                    c = new IcoFatTextComponent(Ico.FIRE, l1, l2);
                    break;
            }
            this.addComponent(c);
        }
        this.pack();
    }

}
