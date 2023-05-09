package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows how meny pelts you have (farming island)

public class TrapperWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Trapper").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public TrapperWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());

        Text amtPelts = StrMan.stdEntry(list, 46, "Pelts:", Formatting.AQUA);
        IcoTextComponent pelts = new IcoTextComponent(Ico.LEATHER, amtPelts);
        this.addComponent(pelts);
        this.pack();
    }

}
