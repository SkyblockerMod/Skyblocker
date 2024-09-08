package de.hysky.skyblocker.skyblock.tabhud.widget;


import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;

import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

// this widget shows how much mithril and gemstone powder you have
// (dwarven mines and crystal hollows)
@RegisterWidget
public class PowderWidget extends TabHudWidget {

    private static final MutableText TITLE = Text.literal("Powders").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public PowderWidget() {
        super("Powders", TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent(List<Text> lines) {
        for (Text line : lines) {
            switch (line.getString().toLowerCase()) {
                case String s when s.contains("mithril") -> this.addComponent(new IcoTextComponent(Ico.MITHRIL, line));
                case String s when s.contains("gemstone") -> this.addComponent(new IcoTextComponent(Ico.AMETHYST_SHARD, line));
                case String s when s.contains("glacite") -> this.addComponent(new IcoTextComponent(Ico.BLUE_ICE, line));
                default -> this.addComponent(new PlainTextComponent(line));
            }
        }

    }

}
