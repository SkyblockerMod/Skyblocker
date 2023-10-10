package de.hysky.skyblocker.skyblock.tabhud.widget.rift;

import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GoodToKnowWidget extends Widget {

	private static final MutableText TITLE = Text.literal("Good To Know").formatted(Formatting.BLUE, Formatting.BOLD);

	public GoodToKnowWidget() {
		super(TITLE, Formatting.BLUE.getColorValue());
    }

    @Override
    public void updateContent() {
		// After you progress further the tab adds more info so we need to be careful of
		// that
		// In beginning it only shows montezuma, then timecharms and enigma souls are
		// added

        int headerPos = 0;
        // this seems suboptimal, but I'm not sure if there's a way to do it better.
        // search for the GTK header and offset the rest accordingly.
        for (int i = 45; i <= 49; i++) {
            String str = PlayerListMgr.strAt(i);
            if (str != null && str.startsWith("Good to")) {
                headerPos = i;
                break;
            }
        }

		Text posA = PlayerListMgr.textAt(headerPos + 2); // Can be times visited rift
		Text posB = PlayerListMgr.textAt(headerPos + 4); // Can be lifetime motes or visited rift
		Text posC = PlayerListMgr.textAt(headerPos + 6); // Can be lifetime motes

		int visitedRiftPos = 0;
		int lifetimeMotesPos = 0;

		// Check each position to see what is or isn't there so we don't try adding
		// invalid components
		if (posA != null && posA.getString().contains("times"))
			visitedRiftPos = headerPos + 2;
		if (posB != null && posB.getString().contains("Motes"))
			lifetimeMotesPos = headerPos + 4;
		if (posB != null && posB.getString().contains("times"))
			visitedRiftPos = headerPos + 4;
		if (posC != null && posC.getString().contains("Motes"))
			lifetimeMotesPos = headerPos + 6;

		Text timesVisitedRift = (visitedRiftPos == headerPos + 4) ? posB : (visitedRiftPos == headerPos + 2) ? posA : Text.literal("No Data").formatted(Formatting.GRAY);
		Text lifetimeMotesEarned = (lifetimeMotesPos == headerPos + 6) ? posC : (lifetimeMotesPos == headerPos + 4) ? posB : Text.literal("No Data").formatted(Formatting.GRAY);

		if (visitedRiftPos != 0) {
			this.addComponent(new IcoTextComponent(Ico.EXPERIENCE_BOTTLE,
					Text.literal("Visited Rift: ").append(timesVisitedRift)));
		}

		if (lifetimeMotesPos != 0) {
			this.addComponent(
					new IcoTextComponent(Ico.PINK_DYE, Text.literal("Lifetime Earned: ").append(lifetimeMotesEarned)));
		}

	}
}
