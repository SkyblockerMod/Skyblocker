package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows your dungeon essences (dungeon hub only)

public class EssenceWidget extends HudWidget {

    private Text undead, wither, diamond, gold, dragon, spider, ice, crimson;

    private static final MutableText TITLE = Text.literal("Essences").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public EssenceWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent() {
        wither = HudWidget.simpleEntryText(46, "Wither:", Formatting.DARK_PURPLE);
        spider = HudWidget.simpleEntryText(47, "Spider:", Formatting.DARK_PURPLE);
        undead = HudWidget.simpleEntryText(48, "Undead:", Formatting.DARK_PURPLE);
        dragon = HudWidget.simpleEntryText(49, "Dragon:", Formatting.DARK_PURPLE);
        gold = HudWidget.simpleEntryText(50, "Gold:", Formatting.DARK_PURPLE);
        diamond = HudWidget.simpleEntryText(51, "Diamond:", Formatting.DARK_PURPLE);
        ice = HudWidget.simpleEntryText(52, "Ice:", Formatting.DARK_PURPLE);
        crimson = HudWidget.simpleEntryText(53, "Crimson:", Formatting.DARK_PURPLE);

        TableComponent tc = new TableComponent(2, 4, Formatting.DARK_AQUA.getColorValue());

        tc.addToCell(0, 0, new IcoTextComponent(Ico.WITHER, wither));
        tc.addToCell(0, 1, new IcoTextComponent(Ico.STRING, spider));
        tc.addToCell(0, 2, new IcoTextComponent(Ico.FLESH, undead));
        tc.addToCell(0, 3, new IcoTextComponent(Ico.DRAGON, dragon));
        tc.addToCell(1, 0, new IcoTextComponent(Ico.GOLD, gold));
        tc.addToCell(1, 1, new IcoTextComponent(Ico.DIAMOND, diamond));
        tc.addToCell(1, 2, new IcoTextComponent(Ico.ICE, ice));
        tc.addToCell(1, 3, new IcoTextComponent(Ico.REDSTONE, crimson));
        this.addComponent(tc);
    }
}
