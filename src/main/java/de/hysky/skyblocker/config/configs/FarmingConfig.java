package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class FarmingConfig {

    @SerialEntry
    public Garden garden = new Garden();

    public static class Garden {
        @SerialEntry
        public FarmingHud farmingHud = new FarmingHud();

        @SerialEntry
        public boolean dicerTitlePrevent = true;

        @SerialEntry
        public boolean visitorHelper = true;

        @SerialEntry
        public boolean lockMouseTool = false;

        @SerialEntry
        public boolean lockMouseGroundOnly = false;

        @SerialEntry
        public boolean gardenPlotsWidget = true;

        @SerialEntry
        public boolean closeScreenOnPlotClick = false;
    }

    public static class FarmingHud {
        @SerialEntry
        public boolean enableHud = true;

        @SerialEntry
        public int x;

        @SerialEntry
        public int y;
    }
}
