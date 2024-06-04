package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.PositionedText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RancherBootsSpeedAdder extends SlotTextAdder {
	public RancherBootsSpeedAdder() {
		super();
	}

	@SuppressWarnings("deprecation") //It's only deprecated to discourage usage as the nbt is supposed to be immutable, but we're not mutating it anyway, so it's fine
	@Override
	public @NotNull List<PositionedText> getText(Slot slot) {
		final ItemStack itemStack = slot.getStack();
		if (!itemStack.isOf(Items.LEATHER_BOOTS)) return List.of();
		final ComponentMap components = itemStack.getComponents();
		if (!components.contains(DataComponentTypes.CUSTOM_DATA)) return List.of();
		NbtComponent nbt = components.get(DataComponentTypes.CUSTOM_DATA);
		if (nbt == null || nbt.isEmpty()) return List.of();
		if (!nbt.contains("ranchers_speed")) return List.of();
		return List.of(PositionedText.BOTTOM_LEFT(Text.literal(String.valueOf(nbt.getNbt().getInt("ranchers_speed"))).formatted(Formatting.GREEN)));
	}
}
