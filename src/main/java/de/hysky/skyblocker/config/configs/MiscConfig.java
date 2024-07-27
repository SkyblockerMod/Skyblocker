package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.resource.language.I18n;

public class MiscConfig {

    @SerialEntry
    public RichPresence richPresence = new RichPresence();

    @SerialEntry
    public DebugOptions debugOptions = new DebugOptions();

    public static class RichPresence {
        @SerialEntry
        public boolean enableRichPresence = false;

        @SerialEntry
        public Info info = Info.LOCATION;

        @SerialEntry
        public boolean cycleMode = false;

        @SerialEntry
        public String customMessage = "Playing Skyblock";
    }

    public static class DebugOptions {
        @SerialEntry
        public boolean enableDebugHitboxes = false;
    }

    public enum Info {
        PURSE, BITS, LOCATION;

        @Override
        public String toString() {
            return I18n.translate("skyblocker.config.misc.richPresence.info." + name());
        }
    }
}
