package de.hysky.skyblocker.skyblock.tabhud.config.list.entries.slot;

import de.hysky.skyblocker.skyblock.tabhud.config.list.WidgetsListScreen;
import de.hysky.skyblocker.skyblock.tabhud.config.list.entries.WidgetsListEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/**
 * An entry with common fields for all entries that clicks slots
 */
public abstract class WidgetsListSlotEntry extends WidgetsListEntry {
	protected final int slotId;
	protected final WidgetsListScreen parent;
	protected final ItemStack icon;

	public WidgetsListSlotEntry(WidgetsListScreen parent, int slotId, ItemStack icon) {
		this.parent = parent;
		this.slotId = slotId;
		this.icon = icon;
	}

	protected void extractIconAndText(GuiGraphicsExtractor graphics, int y, int x, int entryHeight) {
		super.extractIconAndText(graphics, this.icon, y, x, entryHeight);
	}
}
