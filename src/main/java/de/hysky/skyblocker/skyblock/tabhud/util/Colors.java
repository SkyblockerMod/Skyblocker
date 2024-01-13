package de.hysky.skyblocker.skyblock.tabhud.util;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class Colors {

    /**
     * @param pcnt Percentage between 0% and 100%, NOT 0-1!
     * @return an int representing a color, where 100% = green and 0% = red
     */
    public static int pcntToCol(float pcnt) {
        return MathHelper.hsvToRgb(pcnt / 300f, 0.9f, 0.9f);
    }

    public static Formatting hypixelProgressColor(float pcnt) {
        if (pcnt < 25) {
            return Formatting.RED;
        } else if (pcnt < 50) {
            return Formatting.GOLD;
        } else if (pcnt < 75) {
            return Formatting.YELLOW;
        } else {
            return Formatting.GREEN;
        }
    }
}
