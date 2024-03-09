package de.hysky.skyblocker.skyblock.tabhud.widget.hud;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HudFarmingWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Farming").formatted(Formatting.YELLOW, Formatting.BOLD);
    public static final HudFarmingWidget INSTANCE = new HudFarmingWidget();

    public HudFarmingWidget() {
        super(TITLE, Formatting.YELLOW.getColorValue());
        update();
    }

    @Override
    public void updateContent() {
        addSimpleIcoText(Ico.HOE, "Farming ", Formatting.RESET, "Farming");
    }
}
