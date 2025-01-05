package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RancherBootsSpeedAdder extends SimpleSlotTextAdder {
	private static final Pattern SPEED_PATTERN = Pattern.compile("Current Speed Cap: (\\d+) ?(\\d+)?");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"rancher_boots",
			"skyblocker.config.uiAndVisuals.slotText.rancherBoots");

	public RancherBootsSpeedAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (!stack.isOf(Items.LEATHER_BOOTS) && !stack.getSkyblockId().equals("RANCHERS_BOOTS")) return List.of();
		Matcher matcher = ItemUtils.getLoreLineIfMatch(stack, SPEED_PATTERN);
		if (matcher == null) return List.of();
		String speed = matcher.group(2);
		if (speed == null) speed = matcher.group(1); //2nd group only matches when the speed cap is set to a number beyond the player's actual speed cap.
		return SlotText.bottomLeftList(Text.literal(speed).withColor(0xFFDDC1));
	}
}
