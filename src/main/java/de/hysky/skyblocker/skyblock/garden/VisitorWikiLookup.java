package de.hysky.skyblocker.skyblock.garden;

import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;

public class VisitorWikiLookup {
	public static boolean canSearch(String title, @NotNull Slot slot) {
		if (slot.id <= 9 || slot.id >= 44) return false;
		if (slot.getStack().isOf(Items.BLACK_STAINED_GLASS_PANE)) return false;
		return title.matches("^Visitor's Logbook$");
	}
}
