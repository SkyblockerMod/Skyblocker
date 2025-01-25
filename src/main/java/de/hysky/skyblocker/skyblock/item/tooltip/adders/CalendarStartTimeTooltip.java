package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalendarStartTimeTooltip extends SimpleTooltipAdder {

	public static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

	private static final Pattern TIMER_PATTERN = Pattern.compile(".*(Starts in: |\\()((?<days>\\d+)d)? ?((?<hours>\\d+)h)? ?((?<minutes>\\d+)m)? ?((?<seconds>\\d+)s)?\\)?");

	public CalendarStartTimeTooltip(int priority) {
		super("(Calendar and Events|.*?, Year \\d+.*)", priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i).getString();
			Matcher matcher = TIMER_PATTERN.matcher(line);
			if (matcher.matches()) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.set(Calendar.SECOND, 0);

				String s = matcher.group("days");
				calendar.add(Calendar.DAY_OF_MONTH, s != null ? Integer.parseInt(s) : 0);
				s = matcher.group("hours");
				calendar.add(Calendar.HOUR_OF_DAY, s != null ? Integer.parseInt(s) : 0);
				s = matcher.group("minutes");
				calendar.add(Calendar.MINUTE, s != null ? Integer.parseInt(s) : 0);

				int minute = calendar.get(Calendar.MINUTE);
				minute = (int) (Math.round(minute / 5.d) * 5); // round to nearest 5.
				calendar.set(Calendar.MINUTE, minute);

				lines.add(i+1, Text.literal(FORMATTER.format(calendar.toInstant())).formatted(Formatting.ITALIC, Formatting.DARK_GRAY));
				i++;
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemTooltip.enableCalendarStartTime;
	}
}
