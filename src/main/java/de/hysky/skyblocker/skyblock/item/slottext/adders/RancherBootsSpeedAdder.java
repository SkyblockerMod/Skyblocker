package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RancherBootsSpeedAdder extends SlotTextAdder {
	private static final Pattern SPEED_PATTERN = Pattern.compile("Current Speed Cap: (\\d+) ?(\\d+)?");

	public RancherBootsSpeedAdder() {
		super();
	}

	@Override
	public @NotNull List<SlotText> getText(@NotNull ItemStack itemStack, int slotId) {
		//                                                          V null-safe equals.
		if (!itemStack.isOf(Items.LEATHER_BOOTS) && !StringUtils.equals(itemStack.getSkyblockId(), "RANCHERS_BOOTS")) return List.of();
		Matcher matcher = ItemUtils.getLoreLineIfMatch(itemStack, SPEED_PATTERN);
		if (matcher == null) return List.of();
		String speed = matcher.group(2);
		if (speed == null) speed = matcher.group(1); //2nd group only matches when the speed cap is set to a number beyond the player's actual speed cap.
		return List.of(SlotText.bottomLeft(Text.literal(speed).withColor(0xFFDDC1)));
	}
}
