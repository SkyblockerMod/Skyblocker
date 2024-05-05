package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about your profile and bank

public class ProfileWidget extends HudWidget {

    private static final MutableText TITLE = Text.literal("Profile").formatted(Formatting.YELLOW, Formatting.BOLD);

    public ProfileWidget() {
        super(TITLE, Formatting.YELLOW.getColorValue());

    }

    @Override
    public void updateContent() {
        this.addSimpleIcoText(Ico.SIGN, "Profile:", Formatting.GREEN, 61);
        this.addSimpleIcoText(Ico.BONE, "Pet Sitter:", Formatting.AQUA, 62);
        this.addSimpleIcoText(Ico.EMERALD, "Balance:", Formatting.GOLD, 63);
        this.addSimpleIcoText(Ico.CLOCK, "Interest in:", Formatting.GOLD, 64);

    }

}
