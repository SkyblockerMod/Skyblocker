package me.xmrvizzy.skyblocker.skyblock.experiment;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.gui.ColorHighlight;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

public class UltrasequencerSolver extends ExperimentSolver {
    private int ultrasequencerNextSlot;

    public UltrasequencerSolver() {
        super("^Ultrasequencer \\(");
    }

    public int getUltrasequencerNextSlot() {
        return ultrasequencerNextSlot;
    }

    public void setUltrasequencerNextSlot(int ultrasequencerNextSlot) {
        this.ultrasequencerNextSlot = ultrasequencerNextSlot;
    }

    @Override
    protected boolean isEnabled(SkyblockerConfig.Experiments experimentsConfig) {
        return experimentsConfig.enableUltrasequencerSolver;
    }

    @Override
    protected void tick(Screen screen) {

    }

    @Override
    protected List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots) {
        return null;
    }
}
