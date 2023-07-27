package me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import java.util.ArrayList;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;

public abstract class PipelineStage {

    // TODO for all subclasses: error checking, use enums instead of strings

    protected ArrayList<Widget> primary = null;
    protected ArrayList<Widget> secondary = null;

    public abstract void run(int screenW, int screenH);

}
