package de.hysky.skyblocker.utils;

import ca.weblite.objc.Client;
import com.ibm.icu.text.DateTimePatternGenerator;
import de.hysky.skyblocker.debug.Debug;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Provides useful constants for formatting numbers and dates. If you need to make slight tweaks to a formatter
 * then {@link NumberFormat#clone()} the object and modify it as needed.
 */
public class Formatters {
	/**
	 * Formats numbers as integers with commas.
	 * <p>
	 * Example: 100,000,000
	 */
	public static final NumberFormat INTEGER_NUMBERS = NumberFormat.getIntegerInstance(Locale.US);
	/**
	 * Formats numbers as floats with up to two digits of precision.
	 * This is closest to the default number format from {@link NumberFormat#getInstance()}.
	 * <p>
	 * Example: 100,000.15
	 */
	public static final NumberFormat DOUBLE_NUMBERS = Util.make(NumberFormat.getInstance(Locale.US), nf -> nf.setMaximumFractionDigits(2));
	/**
	 * Formats numbers as floats with up to one digit of precision.
	 * <p>
	 * Example: 100,000.1
	 */
	public static final NumberFormat FLOAT_NUMBERS = Util.make(NumberFormat.getInstance(Locale.US), nf -> nf.setMaximumFractionDigits(1));
	/**
	 * Formats integer numbers in a short format.
	 * <p>
	 * Examples: 10B, 1M, and 5K.
	 */
	public static final NumberFormat SHORT_INTEGER_NUMBERS = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
	/**
	 * Formats float numbers in a short format.
	 * <p>
	 * Examples: 17.3B, 1.5M, and 10.8K.
	 */
	public static final NumberFormat SHORT_FLOAT_NUMBERS = Util.make(NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT), nf -> nf.setMinimumFractionDigits(1));
	/**
	 * Formats numbers with a plus sign for positive numbers.
	 * <p>
	 * Examples: +100, -100.123
	 */
	public static final NumberFormat DIFF_NUMBERS = Util.make((DecimalFormat) NumberFormat.getNumberInstance(Locale.US), nf -> nf.setPositivePrefix("+"));
	/**
	 * Formats dates to a standard format.
	 * <p>
	 * Examples: Thu Jan 30 2025 2:00:10 PM, Thu Jan 30 2025 14:00:10
	 */
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("E MMM d yyyy " + getTimeFormat(), Locale.US).withZone(getTimeZone());

	/**
	 * Parses a number from a string.
	 * <p>
	 * Allows commas as thousands separators, periods as decimal points, and abbreviations.
	 */
	public static Number parseNumber(String number) throws NumberFormatException {
		try {
			return SHORT_INTEGER_NUMBERS.parse(number.replace(",", ""));
		} catch (ParseException e) {
			throw new NumberFormatException("For input string: \"" + number + "\"");
		}
	}

	/**
	 * Returns the formatting for the time, always returns 12 hour in test environments.
	 */
	private static String getTimeFormat() {
		return is12HourClock() || Debug.isTestEnvironment() ? "h:mm:ss a" : "HH:mm:ss";
	}

	/**
	 * Returns the time zone to be used for date formatting, always returns UTC in test environments.
	 */
	private static ZoneId getTimeZone() {
		return Debug.isTestEnvironment() ? ZoneId.of("UTC") : ZoneId.systemDefault();
	}

	/**
	 * Determines whether to use the 12 or 24 hour clock for formatting time.
	 * <p>
	 * On macOS this reads the preference for the system clock's time format which accounts for whether a user
	 * chooses 12 or 24 hour time in the System Settings.
	 * <p>
	 * On other platforms, the time format follows the default for the user's current locale.
	 *
	 * @see <a href="https://developer.apple.com/documentation/foundation/nsdateformatter/1408112-dateformatfromtemplate?language=objc">NSDateFormatter</a>
	 * @see <a href="https://www.unicode.org/reports/tr35/tr35-31/tr35-dates.html#Date_Field_Symbol_Table">Unicode Locale Data Markup Language (LDML)</a>
	 */
	private static boolean is12HourClock() {
		//The j formatting template returns the preferred formatting for the time
		//If the format contains a (am/pm pattern) then the preference is to use the 12 hour clock, otherwise its the 24 hour clock
		if (MinecraftClient.IS_SYSTEM_MAC) {
			Object locale = Client.getInstance().send("NSLocale", "currentLocale");
			String timeFormat = (String) Client.getInstance().send("NSDateFormatter", "dateFormatFromTemplate:options:locale:", "j", 0, locale);

			return timeFormat.contains("a");
		} else {
			return DateTimePatternGenerator.getInstance(Locale.getDefault()).getBestPattern("j").contains("a");
		}
	}
}
