package me.xmrvizzy.skyblocker.skyblock.experiment;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.gui.ColorHighlight;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SuperpairsSolver extends ExperimentSolver {
    private int superpairsPrevClickedSlot;
    private ItemStack superpairsCurrentSlot;
    private final Set<Integer> superpairsDuplicatedSlots = new HashSet<>();

    public SuperpairsSolver() {
        super("^Superpairs \\(");
    }

    public void setSuperpairsPrevClickedSlot(int superpairsPrevClickedSlot) {
        this.superpairsPrevClickedSlot = superpairsPrevClickedSlot;
    }

    public void setSuperpairsCurrentSlot(ItemStack superpairsCurrentSlot) {
        this.superpairsCurrentSlot = superpairsCurrentSlot;
    }

    @Override
    protected boolean isEnabled(SkyblockerConfig.Experiments experimentsConfig) {
        return experimentsConfig.enableSuperpairsSolver;
    }

    @Override
    protected void tick(Screen screen) {

    }

    @Override
    protected List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots) {
        return null;
    }
}
