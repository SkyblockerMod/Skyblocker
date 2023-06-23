package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.rift;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RiftProfileWidget extends Widget {
	
	private static final MutableText TITLE = Text.literal("Profile").formatted(Formatting.DARK_AQUA, Formatting.BOLD);
	
	public RiftProfileWidget() {
		super(TITLE, Formatting.DARK_AQUA.getColorValue());
		
		this.addSimpleIcoText(Ico.SIGN, "Profile:", Formatting.GREEN, 61);
		this.pack();
	}
}
