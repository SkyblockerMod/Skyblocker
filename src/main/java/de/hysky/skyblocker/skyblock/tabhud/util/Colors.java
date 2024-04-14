package de.hysky.skyblocker.skyblock.tabhud.util;

import net.minecraft.util.math.MathHelper;

public class Colors {

    /**
     * @param pcnt Percentage between 0% and 100%, NOT 0-1!
     * @return an int representing a color, where 100% = green and 0% = red
     */
    public static int pcntToCol(float pcnt) {
        return MathHelper.hsvToRgb(pcnt / 300, 1, 1);
    }
}
