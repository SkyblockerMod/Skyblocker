package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.RegexUtils;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DateCalculatorTooltip extends SimpleTooltipAdder {
	//((?<days>\d+)d)? ?((?<hours>\d+)h)? ?((?<minutes>\d+)m)? ?((?<seconds>\d+)s)?
	private static final Pattern TIMER_PATTERN = Pattern.compile("((?<days>\\d+)d)? ?((?<hours>\\d+)h)? ?((?<minutes>\\d+)m)? ?((?<seconds>\\d+)s)?");
	private Timer currentTimer;

	public DateCalculatorTooltip(int priority) {
		super(priority);
	}

	@Override
	public boolean test(Screen screen) {
		for (Timer timer : Timer.values()) {
			Matcher matcher = timer.titlePattern.matcher(screen.getTitle().getString());

			if (matcher.matches()) {
				currentTimer = timer;
				return true;
			}
		}

		currentTimer = null;
		return false;
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		if (currentTimer == null) return;

		for (int i = 1; i < lines.size(); i++) {
			String text = lines.get(i).getString();

			//Only attempt to look for a timer if the line contains the qualifying text
			if (!currentTimer.qualifier.test(text)) continue;

			MatchResult result = TIMER_PATTERN.matcher(text).results()
					.filter(DateCalculatorTooltip::hasAnyGroup) //Look for the first match that has what we're looking for
					.findFirst()
					.orElse(null);

			if (result != null) {
				Instant instant = Instant.now()
						.plus(RegexUtils.parseOptionalIntFromMatcher(result, "days").orElse(0), ChronoUnit.DAYS)
						.plus(RegexUtils.parseOptionalIntFromMatcher(result, "hours").orElse(0), ChronoUnit.HOURS)
						.plus(RegexUtils.parseOptionalIntFromMatcher(result, "minutes").orElse(0), ChronoUnit.MINUTES)
						.plusSeconds(RegexUtils.parseOptionalIntFromMatcher(result, "seconds").orElse(0))
						.plusSeconds(30) // Add 30 seconds to round to the nearest minute
						.truncatedTo(ChronoUnit.MINUTES);

				lines.add(++i, Component.literal(Formatters.DATE_FORMATTER.format(instant)).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
			}
		}
	}

	private static boolean hasAnyGroup(MatchResult result) {
		for (String group : result.namedGroups().keySet()) {
			if (result.group(group) != null) return true;
		}

		return false;
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.enableDateCalculator;
	}

	private enum Timer {
		CALENDAR("(Calendar and Events|.*?, Year \\d+.*)", l -> l.contains("Starts in:") || l.contains(" (")); //Calendar start time

		private final Pattern titlePattern;
		private final Predicate<String> qualifier;

		Timer(String title, Predicate<String> qualifier) {
			this.titlePattern = Pattern.compile(title);
			this.qualifier = qualifier;
		}
	}
}
