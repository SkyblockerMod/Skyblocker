package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		String name = stack.getName().getString();

		if (stack.isOf(Items.PLAYER_HEAD) && name.endsWith("Dye")) {
			for (Text line : lines) {
				Matcher matcher = HEX_PATTERN.matcher(line.getString());

				if (matcher.matches()) {
					String hex = matcher.group("hex");
					List<Text> siblings = line.getSiblings();

					siblings.clear();
					siblings.add(Text.literal("Hex ").formatted(Formatting.DARK_GRAY));
					siblings.add(Text.literal(hex).withColor(Integer.decode(hex)));

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
