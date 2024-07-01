package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class SlayersConfig {
    @SerialEntry
    public EndermanSlayer endermanSlayer = new EndermanSlayer();

    @SerialEntry
    public VampireSlayer vampireSlayer = new VampireSlayer();

    @SerialEntry
    public BlazeSlayer blazeSlayer = new BlazeSlayer();

    public static class EndermanSlayer {
        @SerialEntry
        public boolean enableYangGlyphsNotification = true;

        @SerialEntry
        public boolean highlightBeacons = true;

        @SerialEntry
        public boolean highlightNukekubiHeads = true;
    }

    public static class VampireSlayer {
        @SerialEntry
        public boolean enableEffigyWaypoints = true;

        @SerialEntry
        public boolean compactEffigyWaypoints;

        @SerialEntry
        public int effigyUpdateFrequency = 5;

        @SerialEntry
        public boolean enableHolyIceIndicator = true;

        @SerialEntry
        public int holyIceIndicatorTickDelay = 10;

        @SerialEntry
        public int holyIceUpdateFrequency = 5;

        @SerialEntry
        public boolean enableHealingMelonIndicator = true;

        @SerialEntry
        public float healingMelonHealthThreshold = 4f;

        @SerialEntry
        public boolean enableSteakStakeIndicator = true;

        @SerialEntry
        public int steakStakeUpdateFrequency = 5;

        @SerialEntry
        public boolean enableManiaIndicator = true;

        @SerialEntry
        public int maniaUpdateFrequency = 5;
    }

    public static class BlazeSlayer {
        @SerialEntry
        public FirePillar FirePillarCountdown = FirePillar.SOUND_AND_VISUAL;

        public enum FirePillar {
            OFF("Off"),
            VISUAL("Visual Indicator"),
            SOUND_AND_VISUAL("Sound and Visual Indicator");

            private final String description;

            FirePillar(String description) {
                this.description = description;
            }

            @Override
            public String toString() {
                return description;
            }
        }

    }
}
