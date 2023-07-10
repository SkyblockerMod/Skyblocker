package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.rift;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GoodToKnowWidget extends Widget {

	private static final MutableText TITLE = Text.literal("Good To Know").formatted(Formatting.BLUE, Formatting.BOLD);

	public GoodToKnowWidget() {
		super(TITLE, Formatting.BLUE.getColorValue());

		// After you progress further the tab adds more info so we need to be careful of
		// that
		// In beginning it only shows montezuma, then timecharms and enigma souls are
		// added
		Text pos49 = PlayerListMgr.textAt(49); // Can be times visited rift
		Text pos51 = PlayerListMgr.textAt(51); // Can be lifetime motes or visited rift
		Text pos53 = PlayerListMgr.textAt(53); // Can be lifetime motes

		int visitedRiftPos = 0;
		int lifetimeMotesPos = 0;

		// Check each position to see what is or isn't there so we don't try adding
		// invalid components
		if (pos49.getString().contains("times"))
			visitedRiftPos = 49;
		if (pos51.getString().contains("Motes"))
			lifetimeMotesPos = 51;
		if (pos51.getString().contains("times"))
			visitedRiftPos = 51;
		if (pos53.getString().contains("Motes"))
			lifetimeMotesPos = 53;

		Text timesVisitedRift = (visitedRiftPos == 51) ? pos51 : (visitedRiftPos == 49) ? pos49 : null;
		Text lifetimeMotesEarned = (lifetimeMotesPos == 53) ? pos53 : (lifetimeMotesPos == 51) ? pos51 : null;

		if (visitedRiftPos != 0) {
			this.addComponent(new IcoTextComponent(Ico.EXPERIENCE_BOTTLE,
					Text.literal("Visited Rift: ").append(timesVisitedRift)));
		}

		if (lifetimeMotesPos != 0) {
			this.addComponent(
					new IcoTextComponent(Ico.PINK_DYE, Text.literal("Lifetime Earned: ").append(lifetimeMotesEarned)));
		}

		this.pack();
	}
}
