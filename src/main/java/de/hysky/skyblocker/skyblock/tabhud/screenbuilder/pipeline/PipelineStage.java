package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;

import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;

public abstract class PipelineStage {

    protected ArrayList<Widget> primary = null;
    protected ArrayList<Widget> secondary = null;

    public abstract void run(int screenW, int screenH);

}
