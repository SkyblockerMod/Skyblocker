package de.hysky.skyblocker.skyblock.dungeon.terminal;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.container.ContainerSolver;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public final class ColorTerminal extends SimpleContainerSolver implements TerminalSolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColorTerminal.class.getName());
    private static final Map<String, DyeColor> colorFromName;
    private DyeColor targetColor;
    private static final Map<Item, DyeColor> itemColor;

    public ColorTerminal() {
        super("^Select all the ([A-Z ]+) items!$");
    }

    @Override
    public boolean isEnabled() {
        targetColor = null;
        return SkyblockerConfigManager.get().dungeons.terminals.solveColor;
    }

    @Override
    public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
        ContainerSolver.trimEdges(slots, 6);
        List<ColorHighlight> highlights = new ArrayList<>();
        String colorString = groups[0];
        if (targetColor == null) {
            targetColor = colorFromName.get(colorString);
            if (targetColor == null) {
                LOGGER.error("[Skyblocker] Couldn't find dye color corresponding to \"{}\"", colorString);
                return Collections.emptyList();
            }
        }
        for (Int2ObjectMap.Entry<ItemStack> slot : slots.int2ObjectEntrySet()) {
            ItemStack itemStack = slot.getValue();
            if (!itemStack.hasGlint() && targetColor.equals(itemColor.get(itemStack.getItem()))) {
                highlights.add(ColorHighlight.green(slot.getIntKey()));
            }
        }
        return highlights;
    }

    @Override
    public boolean onClickSlot(int slot, ItemStack stack, int screenId) {
        if (stack.hasGlint() || !targetColor.equals(itemColor.get(stack.getItem()))) {
            return shouldBlockIncorrectClicks();
        }

        return false;
    }

    static {
        colorFromName = new HashMap<>();
        for (DyeColor color : DyeColor.values())
            colorFromName.put(color.getId().toUpperCase(Locale.ENGLISH), color);
        colorFromName.put("SILVER", DyeColor.LIGHT_GRAY);
        colorFromName.put("LIGHT BLUE", DyeColor.LIGHT_BLUE);

        itemColor = new HashMap<>();
        for (DyeColor color : DyeColor.values())
            for (String item : new String[]{"dye", "wool", "stained_glass", "terracotta"})
                itemColor.put(Registries.ITEM.get(Identifier.ofVanilla(color.getId() + '_' + item)), color);
        itemColor.put(Items.BONE_MEAL, DyeColor.WHITE);
        itemColor.put(Items.LAPIS_LAZULI, DyeColor.BLUE);
        itemColor.put(Items.COCOA_BEANS, DyeColor.BROWN);
        itemColor.put(Items.INK_SAC, DyeColor.BLACK);
    }
}
