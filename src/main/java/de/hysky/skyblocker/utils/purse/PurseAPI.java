package de.hysky.skyblocker.utils.purse;

import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Utils;

public class PurseAPI {
    public static double currPurse = -1;

    public static void update() {
        if (currPurse == -1) {
            currPurse = Utils.getPurse();
            return;
        }
        double newPurse = Utils.getPurse();
        double diff = newPurse - currPurse;
        if (diff == 0) return;
        currPurse = newPurse;
        SkyblockEvents.PURSE_CHANGE.invoker().onPurseChange(diff, PurseChangeCause.getCause(diff));
    }
}
