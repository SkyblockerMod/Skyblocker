package de.hysky.skyblocker.skyblock.tabhud.widget.rift;

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AdvertisementWidget extends Widget {

	private static final MutableText TITLE = Text.literal("Advertisement").formatted(Formatting.DARK_AQUA,
			Formatting.BOLD);

	public AdvertisementWidget() {
		super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent() {
        boolean added = false;
		for (int i = 73; i < 80; i++) {
			Text text = PlayerListMgr.textAt(i);
			if (text != null) {
				this.addComponent(new PlainTextComponent(text));
                added = true;
            }
		}

        if (!added) {
            this.addComponent(new PlainTextComponent(Text.literal("No Advertisements").formatted(Formatting.GRAY)));
        }
	}

}
