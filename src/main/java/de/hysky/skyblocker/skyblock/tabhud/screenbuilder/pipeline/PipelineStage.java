package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;

import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;

public abstract class PipelineStage {

    protected ArrayList<HudWidget> primary = null;
    protected ArrayList<HudWidget> secondary = null;

    public abstract void run(int screenW, int screenH);

}
