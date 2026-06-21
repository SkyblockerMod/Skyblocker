package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GardenUpgradesAdder extends SimpleSlotTextAdder {

	private static final Pattern TIER_PATTERN = Pattern.compile("Current Tier: (?<tier>\\d+)/(?<max>\\d+)");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"garden_upgrades",
			"skyblocker.config.uiAndVisuals.slotText.gardenUpgrades",
			"skyblocker.config.uiAndVisuals.slotText.gardenUpgrades.@Tooltip"
	);

	public GardenUpgradesAdder() {
		super("^(?:Crop|Greenhouse) Upgrades", CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (slotId > 53) return List.of();
		Matcher matcher = ItemUtils.getLoreLineIfMatch(stack, TIER_PATTERN);
		if (matcher == null) return List.of();

		String tier = matcher.group("tier");
		boolean maxed = tier.equals(matcher.group("max"));

		return SlotText.bottomRightList(
				Component.literal(String.valueOf(tier)).withColor(maxed ? SlotText.GOLD : SlotText.CREAM)
		);
	}
}
