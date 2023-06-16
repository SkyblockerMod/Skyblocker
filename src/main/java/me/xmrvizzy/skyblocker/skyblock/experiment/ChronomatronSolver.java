package me.xmrvizzy.skyblocker.skyblock.experiment;

import com.google.common.collect.ImmutableMap;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.gui.ColorHighlight;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChronomatronSolver extends ExperimentSolver {
    public static final ImmutableMap<Item, Item> TERRACOTTA_TO_GLASS = ImmutableMap.ofEntries(
            new AbstractMap.SimpleImmutableEntry<>(Items.RED_TERRACOTTA, Items.RED_STAINED_GLASS),
            new AbstractMap.SimpleImmutableEntry<>(Items.ORANGE_TERRACOTTA, Items.ORANGE_STAINED_GLASS),
            new AbstractMap.SimpleImmutableEntry<>(Items.YELLOW_TERRACOTTA, Items.YELLOW_STAINED_GLASS),
            new AbstractMap.SimpleImmutableEntry<>(Items.LIME_TERRACOTTA, Items.LIME_STAINED_GLASS),
            new AbstractMap.SimpleImmutableEntry<>(Items.GREEN_TERRACOTTA, Items.GREEN_STAINED_GLASS),
            new AbstractMap.SimpleImmutableEntry<>(Items.CYAN_TERRACOTTA, Items.CYAN_STAINED_GLASS),
            new AbstractMap.SimpleImmutableEntry<>(Items.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_STAINED_GLASS),
            new AbstractMap.SimpleImmutableEntry<>(Items.BLUE_TERRACOTTA, Items.BLUE_STAINED_GLASS),
            new AbstractMap.SimpleImmutableEntry<>(Items.PURPLE_TERRACOTTA, Items.PURPLE_STAINED_GLASS),
            new AbstractMap.SimpleImmutableEntry<>(Items.PINK_TERRACOTTA, Items.PINK_STAINED_GLASS)
    );

    private final List<Item> chronomatronSlots = new ArrayList<>();
    private int chronomatronChainLengthCount;
    private int chronomatronCurrentSlot;
    private int chronomatronCurrentOrdinal;

    public ChronomatronSolver() {
        super("^Chronomatron \\(");
    }

    public List<Item> getChronomatronSlots() {
        return chronomatronSlots;
    }

    public int getChronomatronCurrentOrdinal() {
        return chronomatronCurrentOrdinal;
    }

    public int incrementChronomatronCurrentOrdinal() {
        return ++chronomatronCurrentOrdinal;
    }

    @Override
    protected boolean isEnabled(SkyblockerConfig.Experiments experimentsConfig) {
        return experimentsConfig.enableChronomatronSolver;
    }

    @Override
    protected void tick(Screen screen) {
        if (isEnabled() && screen instanceof GenericContainerScreen genericContainerScreen && genericContainerScreen.getTitle().getString().startsWith("Chronomatron (")) {
            switch (getState()) {
                case REMEMBER -> {
                    Inventory inventory = genericContainerScreen.getScreenHandler().getInventory();
                    if (chronomatronCurrentSlot == 0) {
                        for (int index = 10; index < 43; index++) {
                            if (inventory.getStack(index).hasEnchantments()) {
                                if (chronomatronSlots.size() <= chronomatronChainLengthCount) {
                                    chronomatronSlots.add(TERRACOTTA_TO_GLASS.get(inventory.getStack(index).getItem()));
                                    setState(State.WAIT);
                                } else {
                                    chronomatronChainLengthCount++;
                                }
                                chronomatronCurrentSlot = index;
                                return;
                            }
                        }
                    } else if (!inventory.getStack(chronomatronCurrentSlot).hasEnchantments()) {
                        chronomatronCurrentSlot = 0;
                    }
                }
                case WAIT -> {
                    if (genericContainerScreen.getScreenHandler().getInventory().getStack(49).getName().getString().startsWith("Timer: ")) {
                        setState(State.SHOW);
                    }
                }
                case END -> {
                    String name = genericContainerScreen.getScreenHandler().getInventory().getStack(49).getName().getString();
                    if (!name.startsWith("Timer: ")) {
                        if (name.equals("Remember the pattern!")) {
                            chronomatronChainLengthCount = 0;
                            chronomatronCurrentOrdinal = 0;
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
    protected List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> indexStack : slots.entrySet()) {
            int index = indexStack.getKey();
            ItemStack stack = indexStack.getValue();
            Item item = chronomatronSlots.get(chronomatronCurrentOrdinal);
            if (stack.isOf(item) || TERRACOTTA_TO_GLASS.get(stack.getItem()) == item) {
                highlights.add(ColorHighlight.green(index));
            }
        }
        return highlights;
    }

    @Override
    protected void reset() {
        super.reset();
        chronomatronSlots.clear();
        chronomatronChainLengthCount = 0;
        chronomatronCurrentSlot = 0;
        chronomatronCurrentOrdinal = 0;
    }
}
