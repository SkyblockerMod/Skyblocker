package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.Component;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoFatTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows what you're forging right now.
// for locked slots, the unlock requirement is shown

public class ForgeWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Forge Status").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public ForgeWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent() {
        int forgestart = 54;
        // why is it forges and not looms >:(
        String pos = PlayerListMgr.strAt(53);
        if (pos == null) {
            this.addComponent(new IcoTextComponent());
            return;
        }

        if (!pos.startsWith("Forges")) {
            forgestart += 2;
        }

        for (int i = forgestart, slot = 1; i < forgestart + 5 && i < 60; i++, slot++) {
            String fstr = PlayerListMgr.strAt(i);
            if (fstr == null || fstr.length() < 3) {
                if (i == forgestart) {
                    this.addComponent(new IcoTextComponent());
                }
                break;
            }
            Component c;
            Text l1, l2;

            switch (fstr.substring(3)) {
                case "LOCKED":
                    l1 = Text.literal("Locked").formatted(Formatting.RED);
                    l2 = switch (slot) {
                        case 3 -> Text.literal("Needs HotM 3").formatted(Formatting.GRAY);
                        case 4 -> Text.literal("Needs HotM 4").formatted(Formatting.GRAY);
                        case 5 -> Text.literal("Needs PotM 2").formatted(Formatting.GRAY);
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
                    if (parts.length != 2) {
                        c = new IcoFatTextComponent();
                    } else {
                        l1 = Text.literal(parts[0].substring(3)).formatted(Formatting.YELLOW);
                        l2 = Text.literal("Done in: ").formatted(Formatting.GRAY).append(Text.literal(parts[1]).formatted(Formatting.WHITE));
                        c = new IcoFatTextComponent(Ico.FIRE, l1, l2);
                    }
                    break;
            }
            this.addComponent(c);
        }
    }

}
