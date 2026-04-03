package de.hysky.skyblocker.compatibility;

import ca.weblite.objc.Client;

/// Platform helper for macOS.
public class MacCompatibility {

	/// Determines whether to use the 12 or 24 hour clock for formatting time.
	///
	/// This reads the preference for the system clock's time format which accounts for whether a user chooses 12 or 24
	/// hour time in the System Settings.
	///
	/// @see <a href="https://developer.apple.com/documentation/foundation/nsdateformatter/1408112-dateformatfromtemplate?language=objc">NSDateFormatter</a>
	/// @see <a href="https://www.unicode.org/reports/tr35/tr35-31/tr35-dates.html#Date_Field_Symbol_Table">Unicode Locale Data Markup Language (LDML)</a>
	public static boolean is12HourClock() {
		// The j formatting template returns the preferred formatting for the time
		// If the format contains a (am/pm pattern) then the preference is to use the 12 hour clock, otherwise its the 24 hour clock
		Object locale = Client.getInstance().send("NSLocale", "currentLocale");
		String timeFormat = (String) Client.getInstance().send("NSDateFormatter", "dateFormatFromTemplate:options:locale:", "j", 0, locale);

		return timeFormat.contains("a");
	}
}
