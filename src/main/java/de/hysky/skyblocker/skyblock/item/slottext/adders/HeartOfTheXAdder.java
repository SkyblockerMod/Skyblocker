package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class HeartOfTheXAdder extends SimpleSlotTextAdder {
	private static final Pattern LEVEL = Pattern.compile("Level (?<level>\\d+)/?(?<max>\\d+)?");

	protected HeartOfTheXAdder(@Language("RegExp") String titlePattern, @Nullable ConfigInformation configInformation) {
		super(titlePattern, configInformation);
	}

	protected abstract Item getNonLeveledItem();

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (slotId < 0 || slotId > 44 || stack.isOf(getNonLeveledItem())) return List.of();

		List<String> lore = stack.skyblocker$getLoreStrings();
		if (lore.isEmpty()) return List.of();

		String levelLine = lore.getFirst();
		Matcher matcher = LEVEL.matcher(levelLine);
		if (!matcher.matches()) return List.of();

		String level = matcher.group("level");
		// The `/<max>` part is removed when the level is max, so the group being null means it's maxed
		boolean isMaxed = matcher.group("max") == null;

		return SlotText.bottomRightList(Text.literal(level).withColor(isMaxed ? SlotText.GOLD : SlotText.CREAM));
	}
}
