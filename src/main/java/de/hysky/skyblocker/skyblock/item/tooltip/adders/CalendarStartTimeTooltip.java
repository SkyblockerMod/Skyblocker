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
						.plus(RegexUtils.parseOptionalIntFromMatcher(matcher, "days").orElse(0), ChronoUnit.DAYS)
						.plus(RegexUtils.parseOptionalIntFromMatcher(matcher, "hours").orElse(0), ChronoUnit.HOURS)
						.plus(RegexUtils.parseOptionalIntFromMatcher(matcher, "minutes").orElse(0), ChronoUnit.MINUTES)
						.plusSeconds(RegexUtils.parseOptionalIntFromMatcher(matcher, "seconds").orElse(0))
						.plusSeconds(30) // Add 30 seconds to round to the nearest minute
						.truncatedTo(ChronoUnit.MINUTES);

				lines.add(++i, Text.literal(Formatters.DATE_FORMATTER.format(instant)).formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.enableDateCalculator;
	}
}
