package de.hysky.skyblocker.compatibility.rei;


import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import java.util.List;

import org.jetbrains.annotations.Nullable;

/**
 * Skyblock Crafting Recipe display class for REI
 */
public class SkyblockCraftingDisplay extends BasicDisplay implements SimpleGridMenuDisplay {
    private final String craftText;

    public SkyblockCraftingDisplay(List<EntryIngredient> input, List<EntryIngredient> output, String craftText) {
        super(input, output);
        this.craftText = craftText;
    }

    public String getCraftText() {
        return craftText;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return SkyblockerREIClientPlugin.SKYBLOCK;
    }

	@Override
	@Nullable
	public DisplaySerializer<? extends Display> getSerializer() {
		return null;
	}
}