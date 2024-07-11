package de.hysky.skyblocker.skyblock.chocolatefactory;

import java.util.List;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class ChocolateFactoryAdder extends SlotTextAdder {
	public ChocolateFactoryAdder() {
		super("Chocolate Factory");
	}

	@Override
	public @NotNull List<SlotText> getText(Slot slot) {
		for (ChocolateFactorySolver.Rabbit rabbit : ChocolateFactorySolver.getRabbits()) {
			if (slot.id == rabbit.slot()) {
				// Use SlotText#topLeft for positioning and add color to the text.
				Text levelText = Text.literal(String.valueOf(rabbit.level())).formatted(Formatting.GOLD);
				return List.of(SlotText.topLeft(levelText));
			}
		}
		return List.of(); // Return an empty list if the slot does not correspond to a rabbit slot.
	}
}