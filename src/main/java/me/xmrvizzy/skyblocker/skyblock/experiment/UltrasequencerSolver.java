package me.xmrvizzy.skyblocker.skyblock.experiment;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.gui.ColorHighlight;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
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
        if (isEnabled() && screen instanceof GenericContainerScreen genericContainerScreen && genericContainerScreen.getTitle().getString().startsWith("Ultrasequencer (")) {
            switch (state) {
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
                                slots.put(index, itemStack);
                            }
                        }
                        state = State.WAIT;
                    }
                }
                case WAIT -> {
                    if (genericContainerScreen.getScreenHandler().getInventory().getStack(49).getName().getString().startsWith("Timer: ")) {
                        state = State.SHOW;
                    }
                }
                case END -> {
                    String name = genericContainerScreen.getScreenHandler().getInventory().getStack(49).getName().getString();
                    if (!name.startsWith("Timer: ")) {
                        if (name.equals("Remember the pattern!")) {
                            slots.clear();
                            state = State.REMEMBER;
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
    protected List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> indexStack : slots.entrySet()) {
            int index = indexStack.getKey();
            if (index == ultrasequencerNextSlot) {
                highlights.add(ColorHighlight.green(index));
            }
        }
        return highlights;
    }
}
