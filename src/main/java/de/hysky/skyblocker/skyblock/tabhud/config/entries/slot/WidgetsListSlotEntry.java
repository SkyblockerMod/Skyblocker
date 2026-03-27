package de.hysky.skyblocker.skyblock.tabhud.config.entries.slot;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsListTab;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.WidgetsListEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/**
 * An entry with common fields for all entries that clicks slots
 */
public abstract class WidgetsListSlotEntry extends WidgetsListEntry {
	protected final int slotId;
	protected final WidgetsListTab parent;
	protected final ItemStack icon;

	public WidgetsListSlotEntry(WidgetsListTab parent, int slotId, ItemStack icon) {
		this.parent = parent;
		this.slotId = slotId;
		this.icon = icon;
	}

	protected void extractIconAndText(GuiGraphicsExtractor graphics, int y, int x, int entryHeight) {
		super.extractIconAndText(graphics, this.icon, y, x, entryHeight);
	}
}
