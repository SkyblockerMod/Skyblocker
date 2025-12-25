package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PetLevelAdder extends SimpleSlotTextAdder {
	private static final Pattern LEVEL_PATTERN = Pattern.compile("⭐? ?\\[Lvl (\\d+)].*");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"pet_level",
			"skyblocker.config.uiAndVisuals.slotText.petLevel");
	public PetLevelAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (!stack.is(Items.PLAYER_HEAD) || !stack.getSkyblockId().equals("PET")) return List.of();
		Matcher matcher = LEVEL_PATTERN.matcher(stack.getHoverName().getString());
		if (!matcher.matches()) return List.of();
		String level = matcher.group(1);
		if (!NumberUtils.isDigits(level) || "100".equals(level) || "200".equals(level)) return List.of();
		return SlotText.topLeftList(Component.literal(level).withColor(SlotText.CREAM));
	}
}
