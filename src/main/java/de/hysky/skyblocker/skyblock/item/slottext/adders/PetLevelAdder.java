package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetLevelAdder extends SimpleSlotTextAdder {
	private static final Pattern LEVEL_PATTERN = Pattern.compile("⭐? ?\\[Lvl (\\d+)].*");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"pet_level",
			"skyblocker.config.uiAndVisuals.slotText.petLevel");
	public PetLevelAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (!stack.isOf(Items.PLAYER_HEAD) || !ItemUtils.getItemId(stack).equals("PET")) return List.of();
		Matcher matcher = LEVEL_PATTERN.matcher(stack.getName().getString());
		if (!matcher.matches()) return List.of();
		String level = matcher.group(1);
		if (!NumberUtils.isDigits(level) || "100".equals(level) || "200".equals(level)) return List.of();
		return SlotText.topLeftList(Text.literal(level).withColor(0xFFDDC1));
	}
}
