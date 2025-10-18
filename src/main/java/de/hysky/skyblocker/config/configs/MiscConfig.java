package de.hysky.skyblocker.config.configs;

import net.azureaaron.dandelion.platform.ConfigType;
import net.minecraft.client.resource.language.I18n;

public class MiscConfig {

    public RichPresence richPresence = new RichPresence();

    public ConfigType configBackend = ConfigType.MOUL_CONFIG;

	public boolean cat = true;

    public static class RichPresence {
        public boolean enableRichPresence = false;

        public Info info = Info.LOCATION;

        public boolean cycleMode = false;

        public String customMessage = "Playing Skyblock";
    }

    public enum Info {
        PURSE, BITS, LOCATION;

        @Override
        public String toString() {
            return I18n.translate("skyblocker.config.misc.richPresence.info." + name());
        }
    }
}
