package de.hysky.skyblocker.skyblock.experiment;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;

public class SuperpairsSolver extends ExperimentSolver {
    private int superpairsPrevClickedSlot;
    private ItemStack superpairsCurrentSlot;
    private final Set<Integer> superpairsDuplicatedSlots = new HashSet<>();

    public SuperpairsSolver() {
        super("^Superpairs \\(\\w+\\)$");
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
    protected void start(GenericContainerScreen screen) {
        super.start(screen);
        setState(State.SHOW);
    }

    @Override
    protected void tick(Screen screen) {
        if (isEnabled() && screen instanceof GenericContainerScreen genericContainerScreen && genericContainerScreen.getTitle().getString().startsWith("Superpairs (")) {
            if (getState() == State.SHOW) {
                if (genericContainerScreen.getScreenHandler().getInventory().getStack(4).isOf(Items.CAULDRON)) {
                    reset();
                } else if (getSlots().get(superpairsPrevClickedSlot) == null) {
                    ItemStack itemStack = genericContainerScreen.getScreenHandler().getInventory().getStack(superpairsPrevClickedSlot);
                    if (!(itemStack.isOf(Items.CYAN_STAINED_GLASS) || itemStack.isOf(Items.BLACK_STAINED_GLASS_PANE) || itemStack.isOf(Items.AIR))) {
                        getSlots().entrySet().stream().filter((entry -> ItemStack.areEqual(entry.getValue(), itemStack))).findAny().ifPresent(entry -> superpairsDuplicatedSlots.add(entry.getKey()));
                        getSlots().put(superpairsPrevClickedSlot, itemStack);
                        superpairsCurrentSlot = itemStack;
                    }
                }
            }
        } else {
            reset();
        }
    }

    @Override
    protected List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> displaySlots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        if (getState() == State.SHOW) {
            for (Map.Entry<Integer, ItemStack> indexStack : displaySlots.entrySet()) {
                int index = indexStack.getKey();
                ItemStack displayStack = indexStack.getValue();
                ItemStack stack = getSlots().get(index);
                if (stack != null && !ItemStack.areEqual(stack, displayStack)) {
                    if (ItemStack.areEqual(superpairsCurrentSlot, stack) && displayStack.getName().getString().equals("Click a second button!")) {
                        highlights.add(ColorHighlight.green(index));
                    } else if (superpairsDuplicatedSlots.contains(index)) {
                        highlights.add(ColorHighlight.yellow(index));
                    } else {
                        highlights.add(ColorHighlight.red(index));
                    }
                }
            }
        }
        return highlights;
    }
}
