package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.ArrayList;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;

public abstract class PipelineStage {

    protected ArrayList<Widget> primary = null;
    protected ArrayList<Widget> secondary = null;

    public abstract void run(int screenW, int screenH);

}
