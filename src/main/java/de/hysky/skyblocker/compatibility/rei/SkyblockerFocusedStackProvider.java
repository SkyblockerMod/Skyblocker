package de.hysky.skyblocker.compatibility.rei;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SkyblockerFocusedStackProvider implements FocusedStackProvider {
	private static @Nullable ItemStack focusedItem = null;

	public static void setFocusedItem(@Nullable ItemStack stack) {
		focusedItem = stack;
	}

	@Override
	public CompoundEventResult<EntryStack<?>> provide(Screen screen, Point point) {
		if (!(screen instanceof GenericContainerScreen)) {
			focusedItem = null;
			return CompoundEventResult.pass();
		}

		if (focusedItem == null) return CompoundEventResult.pass();
		return CompoundEventResult.interruptTrue(EntryStacks.of(focusedItem));
	}
}
