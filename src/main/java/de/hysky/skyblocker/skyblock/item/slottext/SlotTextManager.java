package de.hysky.skyblocker.skyblock.item.slottext;

import de.hysky.skyblocker.skyblock.item.slottext.adders.EnchantmentLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.MinionLevelAdder;
import de.hysky.skyblocker.skyblock.item.slottext.adders.PetLevelAdder;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class SlotTextManager {
	private static final SlotTextAdder[] adders = new SlotTextAdder[]{
			new EnchantmentLevelAdder(),
			new MinionLevelAdder(),
			new PetLevelAdder()
	};
	private static final ArrayList<SlotTextAdder> currentScreenAdders = new ArrayList<>();

	private SlotTextManager() {
	}

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (screen instanceof HandledScreen<?>) {
				onScreenChange(screen);
				ScreenEvents.remove(screen).register(ignored -> currentScreenAdders.clear());
			}
		});
	}

	private static void onScreenChange(Screen screen) {
		final String title = screen.getTitle().getString();
		for (SlotTextAdder adder : adders) {
			if (adder.titlePattern == null || adder.titlePattern.matcher(title).matches()) {
				currentScreenAdders.add(adder);
			}
		}
	}

	/**
	 * The returned text is rendered on top of the slot. The text will be scaled if it doesn't fit in the slot,
	 * but 3 characters should be seen as the maximum to keep it readable and in place as it tends to move around when scaled.
	 *
	 * @implNote Only the first adder that returns a non-null text will be used.
	 * The order of the adders remains the same as they were added to the {@link SlotTextManager#adders} array.
	 */
	@Nullable
	public static Text getText(Slot slot) {
		if (currentScreenAdders.isEmpty()) return null;
		for (SlotTextAdder adder : currentScreenAdders) {
			Text text = adder.getText(slot);
			if (text != null) return text;
		}
		return null;
	}
}
