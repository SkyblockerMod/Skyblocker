package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class RancherBootsSpeedAdder extends SimpleSlotTextAdder {
	private static final Pattern SPEED_PATTERN = Pattern.compile("Current Speed Cap: (\\d+) ?(\\d+)?");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"rancher_boots",
			"skyblocker.config.uiAndVisuals.slotText.rancherBoots");

	public RancherBootsSpeedAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (!stack.is(Items.LEATHER_BOOTS) && !stack.getSkyblockId().equals("RANCHERS_BOOTS")) return List.of();
		Matcher matcher = ItemUtils.getLoreLineIfMatch(stack, SPEED_PATTERN);
		if (matcher == null) return List.of();
		String speed = matcher.group(2);
		if (speed == null) speed = matcher.group(1); //2nd group only matches when the speed cap is set to a number beyond the player's actual speed cap.
		return SlotText.bottomLeftList(Component.literal(speed).withColor(SlotText.CREAM));
	}
}
