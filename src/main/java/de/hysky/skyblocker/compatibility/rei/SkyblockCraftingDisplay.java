package de.hysky.skyblocker.compatibility.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Skyblock Crafting Recipe display class for REI
 */
public class SkyblockCraftingDisplay extends BasicDisplay implements SimpleGridMenuDisplay {
    private final String craftText;
	private final String clickCommand;

    public SkyblockCraftingDisplay(List<EntryIngredient> input, List<EntryIngredient> output, String craftText, String clickCommand) {
        super(input, output);
        this.craftText = craftText;
		this.clickCommand = clickCommand;
    }

    public String getCraftText() {
        return craftText;
    }

	public String getClickCommand() {
		return clickCommand;
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
