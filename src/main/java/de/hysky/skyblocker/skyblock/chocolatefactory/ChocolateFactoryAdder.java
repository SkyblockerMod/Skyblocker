package de.hysky.skyblocker.skyblock.chocolatefactory;

import java.util.List;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.TextPosition;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ChocolateFactoryAdder extends SlotTextAdder {
	public ChocolateFactoryAdder() {
		super("Chocolate Factory");
	}

	@Override
	public @NotNull List<SlotText> getText(Slot slot) {
		for (ChocolateFactorySolver.Rabbit rabbit : ChocolateFactorySolver.getRabbits()) {
			if (slot.id == rabbit.slot()) {
				// Assuming the level is to be displayed directly on the slot.
				// Adjust x and y offsets as needed.
				return List.of(new SlotText(Text.literal(String.valueOf(rabbit.level())), TextPosition.TOP_LEFT));
			}
		}
		return List.of(); // Return an empty list if the slot does not correspond to a rabbit slot.
	}
}