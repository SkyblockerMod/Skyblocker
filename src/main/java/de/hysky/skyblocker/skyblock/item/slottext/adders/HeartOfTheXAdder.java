package de.hysky.skyblocker.skyblock.item.slottext.adders;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class HeartOfTheXAdder extends SimpleSlotTextAdder {
	private static final Pattern LEVEL = Pattern.compile("Level (?<level>\\d+)/?(?<max>\\d+)?");

	protected HeartOfTheXAdder(@Language("RegExp") String titlePattern, @Nullable ConfigInformation configInformation) {
		super(titlePattern, configInformation);
	}

	protected abstract boolean isNonLeveledItem(ItemStack stack);

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (slotId < 0 || slotId > 44 || isNonLeveledItem(stack)) return List.of();

		List<String> lore = stack.skyblocker$getLoreStrings();
		if (lore.isEmpty()) return List.of();

		String levelLine = lore.getFirst();
		Matcher matcher = LEVEL.matcher(levelLine);
		if (!matcher.matches()) return List.of();

		String level = matcher.group("level");
		// The `/<max>` part is removed when the level is max, so the group being null means it's maxed
		boolean isMaxed = matcher.group("max") == null;

		return SlotText.bottomRightList(Component.literal(level).withColor(isMaxed ? SlotText.GOLD : SlotText.CREAM));
	}
}
