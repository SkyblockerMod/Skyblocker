package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;

/**
 * Changes the color of HEX color codes on dye items inside of Vincent.
 */
public class TrueHexDyeScreenDisplay extends SimpleTooltipAdder {
	private static final Pattern DYE_SCREEN_NAMES = Pattern.compile("Dye Compendium|Dyes");
	private static final Pattern HEX_PATTERN = Pattern.compile("Hex (?<hex>#[A-Fa-f0-9]{6})");

	public TrueHexDyeScreenDisplay() {
		super(DYE_SCREEN_NAMES, Integer.MIN_VALUE);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		String name = stack.getHoverName().getString();

		if (stack.is(Items.PLAYER_HEAD) && name.endsWith("Dye")) {
			for (Component line : lines) {
				Matcher matcher = HEX_PATTERN.matcher(line.getString());

				if (matcher.matches()) {
					String hex = matcher.group("hex");
					List<Component> siblings = line.getSiblings();

					siblings.clear();
					siblings.add(Component.literal("Hex ").withStyle(ChatFormatting.DARK_GRAY));
					siblings.add(Component.literal(hex).withColor(Integer.decode(hex)));

					return;
				}
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
