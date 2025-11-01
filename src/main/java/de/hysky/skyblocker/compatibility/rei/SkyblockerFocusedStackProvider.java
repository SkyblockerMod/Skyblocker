package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.utils.hoveredItem.HoveredItemStackUtils;
import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

public class SkyblockerFocusedStackProvider implements FocusedStackProvider {
	@Override
	public CompoundEventResult<EntryStack<?>> provide(Screen screen, Point point) {
		ItemStack focusedItem = HoveredItemStackUtils.getHoveredItemStack(screen);
		if (focusedItem == null) return CompoundEventResult.pass();
		return CompoundEventResult.interruptTrue(EntryStacks.of(focusedItem));
	}
}
