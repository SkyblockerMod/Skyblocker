package de.hysky.skyblocker.skyblock.tabhud.widget;


import de.hysky.skyblocker.skyblock.tabhud.util.Ico;

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

// this widget shows how much mithril and gemstone powder you have
// (dwarven mines and crystal hollows)

public class PowderWidget extends Widget {
    /**
     * American number format instance
     */
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);
    /**
     * current value of Mithril Powder
     */
    private static int mithrilPowder = 0;
    /**
     * current value of Gemstone Powder
     */
    private static int gemstonePowder = 0;
    /**
     * the difference between the previous and current value of Mithril Powder
     */
    private static int mithrilPowderDiff = 0;
    /**
     * the difference between the previous and current value of Gemstone Powder
     */
    private static int gemstonePowderDiff = 0;
    /**
     * The initial value of the timer for the difference update delay countdown.
     */
    private static long startTime = System.currentTimeMillis();

    private static final MutableText TITLE = Text.literal("Powders").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public PowderWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    /**
     * Converts a string with a number and commas between digits to an integer value.
     *
     * @param str a string with a number and commas between digits
     * @return integer value
     */
    private static int parsePowder(String str) {
        if (str == null) return 0;
        try {
            return NUMBER_FORMAT.parse(str.split(": ")[1]).intValue();
        } catch (ParseException e) {
            return 0;
        }
    }

    /**
     * Converts Powder and difference values to a string and adds commas to the digits of the numbers.
     *
     * @param powder the value of Mithril or Gemstone Powder
     * @param diff   the difference between the previous and current value of Mithril or Gemstone Powder
     * @return formatted string
     */
    private static String formatPowderString(int powder, int diff) {
        if (diff == 0) return NUMBER_FORMAT.format(powder);
        return NUMBER_FORMAT.format(powder) + (diff > 0 ? " (+" : " (") + NUMBER_FORMAT.format(diff) + ")";
    }

    /**
     * Updates Powders and difference values when Powder values change or every 2 seconds.
     */
    private static void updatePowders() {
        long elapsedTime = System.currentTimeMillis() - startTime;

        int newMithrilPowder = parsePowder(PlayerListMgr.strAt(46));
        int newGemstonePowder = parsePowder(PlayerListMgr.strAt(47));

        if (newMithrilPowder != mithrilPowder || newGemstonePowder != gemstonePowder || elapsedTime > 2000) {
            startTime = System.currentTimeMillis();

            mithrilPowderDiff = newMithrilPowder - mithrilPowder;
            gemstonePowderDiff = newGemstonePowder - gemstonePowder;

            mithrilPowder = newMithrilPowder;
            gemstonePowder = newGemstonePowder;
        }
    }

    @Override
    public void updateContent() {
        updatePowders();
        String mithrilPowderString = formatPowderString(mithrilPowder, mithrilPowderDiff);
        String gemstonePowderString = formatPowderString(gemstonePowder, gemstonePowderDiff);

        this.addSimpleIcoText(Ico.MITHRIL, "Mithril: ", Formatting.AQUA, mithrilPowderString);
        this.addSimpleIcoText(Ico.AMETHYST_SHARD, "Gemstone: ", Formatting.DARK_PURPLE, gemstonePowderString);
    }

}
