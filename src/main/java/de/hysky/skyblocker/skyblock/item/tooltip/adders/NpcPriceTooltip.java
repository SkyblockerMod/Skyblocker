package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.RegexUtils;
import de.hysky.skyblocker.utils.TextUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NpcPriceTooltip extends SimpleTooltipAdder {
	private static final Pattern STORED_PATTERN = Pattern.compile("Stored: ([\\d,]+)/\\S+");
	private static final Logger LOGGER = LoggerFactory.getLogger(NpcPriceTooltip.class);
	private static final short LOG_INTERVAL = 1000;
	private static long lastLog = Util.getMeasuringTimeMs();

	public NpcPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.NPC.isTooltipEnabled();
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		// NPC prices seem to use the Skyblock item id, not the Skyblock api id.
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.NPC.getData() == null) {
			ItemTooltip.nullWarning();
			return;
		}
		double price = TooltipInfoType.NPC.getData().getOrDefault(internalID, -1); // The original default return value of 0 can be an actual price, so we use a value that can't be a price
		if (price < 0) return;

		int amount = parseAmount(stack, lines);
		lines.add(Text.literal(String.format("%-21s", "NPC Sell Price:"))
					  .formatted(Formatting.YELLOW)
					  .append(ItemTooltip.getCoinsMessage(price, amount)));
	}

	private int parseAmount(ItemStack stack, List<Text> lines) {
		if (lines.size() >= 2 && lines.get(1).getString().endsWith("Sack")) {
			//Example line: empty[style={color=dark_purple,!italic}, siblings=[literal{Stored: }[style={color=gray}], literal{0}[style={color=dark_gray}], literal{/20k}[style={color=gray}]]
			Matcher matcher = TextUtils.matchInList(lines, STORED_PATTERN);
			if (matcher == null) {
				// Log a warning every second if the amount couldn't be found, to prevent spamming the logs every frame (which can be hundreds of times per second)
				if (Util.getMeasuringTimeMs() - lastLog > LOG_INTERVAL) {
					LOGGER.warn("Failed to find stored amount in sack tooltip for item `{}`", Debug.DumpFormat.JSON.format(stack).getString()); // This is a very unintended way of serializing the item stack, but it's so much cleaner than actually using the codec
					lastLog = Util.getMeasuringTimeMs();
				}
				return stack.getCount();
			} else {
				OptionalInt amount = RegexUtils.findIntFromMatcher(matcher);
				return amount.isPresent() ? amount.getAsInt() : stack.getCount();
			}
		}
		return stack.getCount();
	}
}
