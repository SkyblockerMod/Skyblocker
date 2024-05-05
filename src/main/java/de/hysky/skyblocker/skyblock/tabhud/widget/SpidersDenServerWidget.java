package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

/**
 * This widget shows info about the Spider's Den server
 */
public class SpidersDenServerWidget extends HudWidget {

    private static final MutableText TITLE =
            Text.literal("Server Info").formatted(Formatting.DARK_AQUA, Formatting.BOLD);

    /**
     * Broodmother Mini-Boss tab states
     */
    private enum BroodmotherState {
        SOON("Soon", Formatting.GOLD),
        AWAKENING("Awakening", Formatting.GOLD),
        IMMINENT("Imminent", Formatting.DARK_RED),
        ALIVE("Alive!", Formatting.DARK_RED),
        SLAIN("Slain", Formatting.YELLOW),
        DORMANT("Dormant", Formatting.YELLOW),
        UNKNOWN("Unknown", Formatting.GRAY);

        private final String text;
        private final Formatting formatting;

        BroodmotherState(String text, Formatting formatting) {
            this.text = text;
            this.formatting = formatting;
        }

        public String text() {
            return this.text;
        }

        public Formatting formatting() {
            return this.formatting;
        }

        /**
         * Returns a state object by text
         *
         * @param text text state from tab
         * @return Broodmother State object
         */
        public static BroodmotherState from(String text) {
            return Arrays.stream(BroodmotherState.values())
                    .filter(broodmotherState -> text.equals(broodmotherState.text())).findFirst().orElse(UNKNOWN);
        }
    }

    public SpidersDenServerWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    /**
     * Parses the Broodmother string from tab and returns a state object.
     *
     * @return Broodmother State object
     */
    private static BroodmotherState parseTab() {
        String state = PlayerListMgr.strAt(45);
        if (state == null || !state.contains(": ")) return BroodmotherState.UNKNOWN;

        return BroodmotherState.from(state.split(": ")[1]);
    }

    /**
     * Updates the information in the widget.
     */
    @Override
    public void updateContent() {
        this.addSimpleIcoText(Ico.MAP, "Area:", Formatting.DARK_AQUA, 41);
        this.addSimpleIcoText(Ico.NTAG, "Server ID:", Formatting.GRAY, 42);
        this.addSimpleIcoText(Ico.EMERALD, "Gems:", Formatting.GREEN, 43);

        BroodmotherState broodmotherState = parseTab();
        this.addSimpleIcoText(Ico.SPIDER_EYE, "Broodmother: ", broodmotherState.formatting(), broodmotherState.text());
    }
}
