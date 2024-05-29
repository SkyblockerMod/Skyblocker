package de.hysky.skyblocker.utils.tooltip;

import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TooltipManager {
	private static final TooltipAdder[] adders = new TooltipAdder[]{
			new LineSmoothener(),
			new DungeonQualityTooltip(0),
			new NpcPriceTooltip(1),
			new ObtainedTooltip(3),
			new MuseumTooltip(4),
			new ColorTooltip(5),
			new AccessoryTooltip(6),
			new MotesTooltip(0),
	};
	private static final ArrayList<TooltipAdder> currentScreenAdders = new ArrayList<>();

	private TooltipManager() {
	}

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			onScreenChange(screen);
			ScreenEvents.remove(screen).register(ignored -> currentScreenAdders.clear());
		});
	}

	private static void onScreenChange(Screen screen) {
		final String title = screen.getTitle().getString();
		for (TooltipAdder adder : adders) {
			if (adder.titlePattern == null || adder.titlePattern.matcher(title).matches()) {
				currentScreenAdders.add(adder);
			}
		}
		currentScreenAdders.sort(Comparator.comparingInt(adder -> adder.priority));
	}

	/**
	 * <p>Adds additional text from all adders that are applicable to the current screen.
	 * This method is run on each tooltip render, so don't do any heavy calculations here.</p>
	 *
	 * <p>If you want to add info to the tooltips of multiple items, consider using a switch statement with {@code focusedSlot.getIndex()}</p>
	 *
	 * @param lore        The lore of the focused item.
	 * @param focusedSlot The slot that is currently focused by the cursor.
	 * @return The lore itself after all adders have added their text.
	 * @deprecated This method is public only for the sake of the mixin. Don't call directly, not that there is any point to it.
	 */
	@Deprecated
	public static List<Text> addToTooltip(List<Text> lore, Slot focusedSlot) {
		if (!Utils.isOnSkyblock()) return lore;
		for (TooltipAdder adder : currentScreenAdders) {
			adder.addToTooltip(lore, focusedSlot);
		}
		return lore;
	}
}
