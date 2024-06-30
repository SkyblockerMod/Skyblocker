package de.hysky.skyblocker.stp.predicates;

import java.util.List;

import com.mojang.serialization.Codec;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

/**
 * Allows for checking whether an {@link ItemStack} is inside of a screen or not. Useful for re-texturing screen exclusive items.
 */
public record InsideScreenPredicate(boolean inScreen) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "inside_screen");
	public static final Codec<InsideScreenPredicate> CODEC = Codec.BOOL.xmap(InsideScreenPredicate::new, InsideScreenPredicate::inScreen);

	@Override
	public boolean test(ItemStack stack) {
		if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> handledScreen) {
			List<Slot> slots = handledScreen.getScreenHandler().slots;

			for (Slot slot : slots) {
				ItemStack slotStack = slot.getStack();

				if (slotStack == stack) return true;
			}
		}

		return false;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.INSIDE_SCREEN;
	}
}
