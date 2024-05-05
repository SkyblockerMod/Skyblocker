package de.hysky.skyblocker.skyblock.tabhud.widget.rift;

import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RiftProfileWidget extends HudWidget {

	private static final MutableText TITLE = Text.literal("Profile").formatted(Formatting.DARK_AQUA, Formatting.BOLD);

	public RiftProfileWidget() {
		super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent() {
		this.addSimpleIcoText(Ico.SIGN, "Profile:", Formatting.GREEN, 61);
	}
}
