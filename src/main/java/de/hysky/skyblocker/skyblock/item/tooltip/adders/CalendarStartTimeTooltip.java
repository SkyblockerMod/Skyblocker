package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.RegexUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalendarStartTimeTooltip extends SimpleTooltipAdder {
	private static final Pattern TIMER_PATTERN = Pattern.compile(".*(Starts in: |\\()((?<days>\\d+)d)? ?((?<hours>\\d+)h)? ?((?<minutes>\\d+)m)? ?((?<seconds>\\d+)s)?\\)?");

	public CalendarStartTimeTooltip(int priority) {
		super("(Calendar and Events|.*?, Year \\d+.*)", priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		for (int i = 1; i < lines.size(); i++) {
			Matcher matcher = TIMER_PATTERN.matcher(lines.get(i).getString());
			if (matcher.matches()) {
				Instant instant = Instant.now()
						.truncatedTo(ChronoUnit.MINUTES)
						.plus(RegexUtils.parseIntFromMatcher(matcher, "days"), ChronoUnit.DAYS)
						.plus(RegexUtils.parseIntFromMatcher(matcher, "hours"), ChronoUnit.HOURS)
						.plus(RegexUtils.parseIntFromMatcher(matcher, "minutes"), ChronoUnit.MINUTES);

				lines.add(++i, Text.literal(Formatters.DATE_FORMATTER.format(instant)).formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemTooltip.enableCalendarStartTime;
	}
}
