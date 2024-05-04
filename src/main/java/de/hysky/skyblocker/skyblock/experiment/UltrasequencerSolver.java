package de.hysky.skyblocker.skyblock.experiment;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UltrasequencerSolver extends ExperimentSolver {
    public static final UltrasequencerSolver INSTANCE = new UltrasequencerSolver();
    private int ultrasequencerNextSlot;

    private UltrasequencerSolver() {
        super("^Ultrasequencer \\(\\w+\\)$");
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
        if (isEnabled() && screen instanceof GenericContainerScreen genericContainerScreen && genericContainerScreen.getTitle().getString().startsWith("Ultrasequencer (")) {
            switch (getState()) {
                case REMEMBER -> {
                    Inventory inventory = genericContainerScreen.getScreenHandler().getInventory();
                    if (inventory.getStack(49).getName().getString().equals("Remember the pattern!")) {
                        for (int index = 9; index < 45; index++) {
                            ItemStack itemStack = inventory.getStack(index);
                            String name = itemStack.getName().getString();
                            if (name.matches("\\d+")) {
                                if (name.equals("1")) {
                                    ultrasequencerNextSlot = index;
                                }
                                getSlots().put(index, itemStack);
                            }
                        }
                        setState(State.WAIT);
                    }
                }
                case WAIT -> {
                    if (genericContainerScreen.getScreenHandler().getInventory().getStack(49).getName().getString().startsWith("Timer: ")) {
                        setState(State.SHOW);
                        markHighlightsDirty();
                    }
                }
                case END -> {
                    String name = genericContainerScreen.getScreenHandler().getInventory().getStack(49).getName().getString();
                    if (!name.startsWith("Timer: ")) {
                        if (name.equals("Remember the pattern!")) {
                            getSlots().clear();
                            setState(State.REMEMBER);
                        } else {
                            reset();
                        }
                    }
                }
            }
        } else {
            reset();
        }
    }

    @Override
    protected List<ColorHighlight> getColors(String[] groups, Int2ObjectMap<ItemStack> slots) {
        return getState() == State.SHOW && ultrasequencerNextSlot != 0 ? List.of(ColorHighlight.green(ultrasequencerNextSlot)) : new ArrayList<>();
    }
}
