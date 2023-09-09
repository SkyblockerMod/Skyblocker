package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.rift;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ShenWidget extends Widget {

	private static final MutableText TITLE = Text.literal("Shen's Countdown").formatted(Formatting.DARK_AQUA, Formatting.BOLD);

	public ShenWidget() {
		super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent() {
		this.addComponent(new PlainTextComponent(Text.literal(PlayerListMgr.strAt(70))));
	}
}
