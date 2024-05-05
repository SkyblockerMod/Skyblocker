package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows how meny pelts you have (farming island)

public class TrapperWidget extends HudWidget {
    private static final MutableText TITLE = Text.literal("Trapper").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public TrapperWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());

    }

    @Override
    public void updateContent() {
        this.addSimpleIcoText(Ico.LEATHER, "Pelts:", Formatting.AQUA, 46);
    }

}
