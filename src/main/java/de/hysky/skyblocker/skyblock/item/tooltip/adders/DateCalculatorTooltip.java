package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.RegexUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.utils.time.SkyblockTime;
import de.hysky.skyblocker.utils.time.SkyblockTimeField;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;

public class DateCalculatorTooltip extends SimpleTooltipAdder {
	//((?<days>\d+)d)? ?((?<hours>\d+)h)? ?((?<minutes>\d+)m)? ?((?<seconds>\d+)s)?
	private static final Pattern TIMER_PATTERN = Pattern.compile("((?<days>\\d+)d)? ?((?<hours>\\d+)h)? ?((?<minutes>\\d+)m)? ?((?<seconds>\\d+)s)?");
	private static final TimeProvider[] PROVIDERS = new TimeProvider[] {new Events(), new Calendar()};
	private @Nullable TimeProvider currentTimer;

	public DateCalculatorTooltip(int priority) {
		super(priority);
	}

	@Override
	public boolean test(Screen screen) {
		String screenTitle = screen.getTitle().getString();
		for (TimeProvider timer : PROVIDERS) {
			if (timer.test(screenTitle)) {
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
			if (!currentTimer.qualifier().test(text)) continue;

			Instant instant = currentTimer.getStartTime(stack, text);

			if (instant != null) {

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

	private interface TimeProvider {
		boolean test(String screenTitle);
		default Predicate<String> qualifier() {
			return l -> l.contains("Starts in:") || l.contains(" (");
		}
		@Nullable Instant getStartTime(ItemStack stack, String qualifiedLine);
	}

	private static class Events implements TimeProvider {
		@Override
		public boolean test(String screenTitle) {
			return screenTitle.equals("Calendar and Events");
		}

		@Override
		public @Nullable Instant getStartTime(ItemStack stack, String qualifiedLine) {
			MatchResult result = TIMER_PATTERN.matcher(qualifiedLine).results()
					.filter(DateCalculatorTooltip::hasAnyGroup) //Look for the first match that has what we're looking for
					.findFirst()
					.orElse(null);

			if (result != null) {
				return Instant.now()
					.plus(RegexUtils.parseOptionalIntFromMatcher(result, "days").orElse(0), ChronoUnit.DAYS)
					.plus(RegexUtils.parseOptionalIntFromMatcher(result, "hours").orElse(0), ChronoUnit.HOURS)
					.plus(RegexUtils.parseOptionalIntFromMatcher(result, "minutes").orElse(0), ChronoUnit.MINUTES)
					.plusSeconds(RegexUtils.parseOptionalIntFromMatcher(result, "seconds").orElse(0))
					.plusSeconds(30) // Add 30 seconds to round to the nearest minute
					.truncatedTo(ChronoUnit.MINUTES);
			}
			return null;
		}
	}

	private static class Calendar implements TimeProvider {
		private static final Pattern PATTERN = Pattern.compile("(?<month>.+), Year (?<year>\\d+)");
		private SkyblockTime.Month month = SkyblockTime.Month.EARLY_SPRING;
		private int year = 1;

		@Override
		public boolean test(String screenTitle) {
			Matcher matcher = PATTERN.matcher(screenTitle);
			if (matcher.matches()) {
				SkyblockTime.Month maybeMonth = SkyblockTime.Month.of(matcher.group("month"));
				if (maybeMonth == null) return false;
				month = maybeMonth;
				int maybeYear = NumberUtils.toInt(matcher.group("year"));
				if (maybeYear == 0) return false;
				year = maybeYear;
				return true;
			}
			return false;
		}

		@Override
		public @Nullable Instant getStartTime(ItemStack stack, String qualifiedLine) {
			if (stack.count() > 31) return null;
			return SkyblockTime.SKYBLOCK_EPOCH
					.with(SkyblockTimeField.YEAR, year)
					.with(SkyblockTimeField.MONTH_OF_YEAR, month.ordinal() + 1)
					.with(SkyblockTimeField.DAY_OF_MONTH, stack.count());
		}
	}
}
