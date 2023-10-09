package de.hysky.skyblocker.compatibility.rei;

import com.google.common.collect.Lists;
import de.hysky.skyblocker.utils.ItemUtils;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Label;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Skyblock recipe category class for REI
 */
public class SkyblockCategory implements DisplayCategory<SkyblockCraftingDisplay> {
    @Override
    public CategoryIdentifier<SkyblockCraftingDisplay> getCategoryIdentifier() {
        return SkyblockerREIClientPlugin.SKYBLOCK;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("emi.category.skyblocker.skyblock");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(ItemUtils.getSkyblockerStack());
    }

    @Override
    public int getDisplayHeight() {
        return 73;
    }

    /**
     * Draws display for SkyblockCraftingDisplay
     *
     * @param display the display
     * @param bounds  the bounds of the display, configurable with overriding the width, height methods.
     */
    @Override
    public List<Widget> setupDisplay(SkyblockCraftingDisplay display, Rectangle bounds) {
        List<Widget> out = new ArrayList<>();
        out.add(Widgets.createRecipeBase(bounds));

        Point startPoint;
        if (!display.getCraftText().isEmpty() && display.getCraftText() != null) {
            startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 31);
        }
        else {
            startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 26);
        }
        Point resultPoint = new Point(startPoint.x + 95, startPoint.y + 19);
        out.add(Widgets.createArrow(new Point(startPoint.x + 60, startPoint.y + 18)));
        out.add(Widgets.createResultSlotBackground(resultPoint));

        // Generate Slots
        List<EntryIngredient> input = display.getInputEntries();
        List<Slot> slots = Lists.newArrayList();
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                slots.add(Widgets.createSlot(new Point(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18)).markInput());
        for (int i = 0; i < input.size(); i++) {
            slots.get(i).entries(input.get(i)).markInput();
        }
        out.addAll(slots);
        out.add(Widgets.createSlot(resultPoint).entries(display.getOutputEntries().get(0)).disableBackground().markOutput());

        // Add craftingText Label
        Label craftTextLabel = Widgets.createLabel(new Point(bounds.getCenterX(), startPoint.y + 55), Text.of(display.getCraftText()));
        out.add(craftTextLabel);
        return out;
    }
}
