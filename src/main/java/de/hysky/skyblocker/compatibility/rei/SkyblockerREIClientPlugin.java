package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.*;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.List;

/**
 * REI integration
 */
public class SkyblockerREIClientPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<SkyblockCraftingDisplay> SKYBLOCK = CategoryIdentifier.of(
        SkyblockerMod.NAMESPACE,
        "skyblock"
    );

    @Override
    public void registerCategories(CategoryRegistry categoryRegistry) {
        categoryRegistry.addWorkstations(SKYBLOCK, EntryStacks.of(Items.CRAFTING_TABLE));
        categoryRegistry.add(new SkyblockCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry displayRegistry) {
        displayRegistry.registerDisplayGenerator(SKYBLOCK, new SkyblockCraftingDisplayGenerator());
    }

    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        List<EntryStack<ItemStack>> stacks = ItemRepository.getItemsStream()
            .map(item -> {
                EntryStack<ItemStack> entry = EntryStacks.of(item);
                ClientEntryStacks.setTooltipProcessor(
                    entry,
                    (_entry, tooltip) -> modifyTooltip(
                        entry.getValue(),
                        tooltip.entries()
                            .stream()
                            .map(Tooltip.Entry::getAsText)
                            .toList()
                    )
                );
                return entry;
            })
            .toList();


        entryRegistry.addEntries(stacks);
    }

    private final TooltipAdder[] adders = new TooltipAdder[]{
        new LineSmoothener(), // Applies before anything else
        new TrueHexDisplay(),
        new NpcPriceTooltip(0),
        new BazaarPriceTooltip(1),
        new LBinTooltip(2),
        new AvgBinTooltip(3),
        new CraftPriceTooltip(4),
        new DungeonQualityTooltip(5),
        new MotesTooltip(6),
        new MuseumTooltip(7),
        new ColorTooltip(8),
        new AccessoryTooltip(9),
    };
    private final Slot EMPTY_SLOT = new Slot(null, 0, 0, 0);

    public Tooltip modifyTooltip(
        ItemStack stack,
        List<Text> lines
    ) {
        for (TooltipAdder adder : adders) {
            adder.addToTooltip(EMPTY_SLOT, stack, lines);
        }

        return Tooltip.create(lines);
    }
}
