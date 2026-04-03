package de.hysky.skyblocker.utils.purse;

import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.Minecraft;

public enum PurseChangeCause {
	// Gain
	MOB_KILL,
	TALISMAN_OF_COINS,
	DICE_SIX,
	TAKE_BANK,
	UNKNOWN_GAIN,
	// Loss
	SLAYER_QUEST,
	DICE_ROLL,
	DEPO_BANK,
	UNKNOWN_LOSS;

	public static PurseChangeCause getCause(double diff) {
		if (diff > 0) {
			if (diff == 5 || diff == 25) {
				return TALISMAN_OF_COINS;
			}

			if (diff == 15000000 || diff == 100000000) {
				return DICE_SIX;
			}

			if (Minecraft.getInstance().screen == null) {
				// UI closed
				// need to make this more specific, but atm might as well attrib to mob kill
				return MOB_KILL;
			} else if (Utils.getArea() == Area.Hub.BANK) {
				return TAKE_BANK;
			}
			return UNKNOWN_GAIN;
		} else {
			// TODO: implement slayer quest loss
			if (diff == -6666666 || diff == -666666) {
				return DICE_ROLL;
			}
			return UNKNOWN_LOSS;
		}
	}
}
