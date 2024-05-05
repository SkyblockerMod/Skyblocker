package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoFatTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widgte shows, how many active effects you have.
// it also shows one of those in detail.
// the parsing is super suspect and should be replaced by some regexes sometime later

public class EffectWidget extends HudWidget {

    private static final MutableText TITLE = Text.literal("Effect Info").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    public EffectWidget() {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
    }

    @Override
    public void updateContent() {

        String footertext = PlayerListMgr.getFooter();

        if (footertext == null || !footertext.contains("Active Effects")) {
            this.addComponent(new IcoTextComponent());
            return;

        }

        String[] lines = footertext.split("Active Effects")[1].split("\n");
        if (lines.length < 2) {
            this.addComponent(new IcoTextComponent());
            return;
        }

        if (lines[1].startsWith("No")) {
            Text txt = Text.literal("No effects active").formatted(Formatting.GRAY);
            this.addComponent(new IcoTextComponent(Ico.POTION, txt));
        } else if (lines[1].contains("God")) {
            String timeleft = lines[1].split("! ")[1];
            Text godpot = Text.literal("God potion!").formatted(Formatting.RED);
            Text txttleft = Text.literal(timeleft).formatted(Formatting.LIGHT_PURPLE);
            IcoFatTextComponent iftc = new IcoFatTextComponent(Ico.POTION, godpot, txttleft);
            this.addComponent(iftc);
        } else {
            String number = lines[1].substring("You have ".length());
            int idx = number.indexOf(' ');
            if (idx == -1 || lines.length < 4) {
                this.addComponent(new IcoFatTextComponent());
                return;
            }
            number = number.substring(0, idx);
            Text active = Text.literal("Active Effects: ")
                    .append(Text.literal(number).formatted(Formatting.YELLOW));

            IcoFatTextComponent iftc = new IcoFatTextComponent(Ico.POTION, active,
                    Text.literal(lines[2]).formatted(Formatting.AQUA));
            this.addComponent(iftc);
        }
    }

}
