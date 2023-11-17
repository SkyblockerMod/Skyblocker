package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GardenVisitorsWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Visitors").formatted(Formatting.DARK_GREEN, Formatting.BOLD);

    public GardenVisitorsWidget() {
        super(TITLE, Formatting.DARK_GREEN.getColorValue());
    }

    @Override
    public void updateContent() {
    	int offset = (PlayerListMgr.strAt(46) != null) ? 1 : 0;

        if (PlayerListMgr.textAt(54 + offset) == null) {
            this.addComponent(new PlainTextComponent(Text.literal("No visitors!").formatted(Formatting.GRAY)));
            return;
        }

        for (int i = 54 + offset; i < 59 + offset; i++) {
            String text = PlayerListMgr.strAt(i);
            if (text != null)
                this.addComponent(new PlainTextComponent(Text.literal(text)));
        }
    }
}
