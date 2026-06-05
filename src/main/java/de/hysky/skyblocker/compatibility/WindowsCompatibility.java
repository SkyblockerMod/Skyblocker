package de.hysky.skyblocker.compatibility;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

/// Platform helper for Windows.
public class WindowsCompatibility {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int LOCALE_STIMEFORMAT = 0x00001003;

	/// Determines whether to use the 12 or 24 hour clock for formatting time.
	///
	/// This reads the preference for the system clock's time format which accounts for whether a user chooses 12 or 24
	/// hour time in the System Settings.
	///
	/// @see <a href="https://learn.microsoft.com/en-ca/windows/win32/api/winnls/nf-winnls-getlocaleinfoex">getLocaleInfoEx</a>
	/// @see <a href="https://learn.microsoft.com/en-ca/windows/win32/intl/locale-stime-constants">LOCALE_STIME* Constants</a>
	public static boolean is12HourClock() {
		try (Arena arena = Arena.ofConfined()) {
			SymbolLookup kernel32Lookup = SymbolLookup.libraryLookup("Kernel32", arena);
			MethodHandle getLocaleInfoEx = Linker.nativeLinker().downcallHandle(
					kernel32Lookup.findOrThrow("GetLocaleInfoEx"),
					FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
					);

			// Get the necessary segment size for storing the time format
			int segmentSize = (int) getLocaleInfoEx.invokeExact(MemorySegment.NULL, LOCALE_STIMEFORMAT, MemorySegment.NULL, 0);
			MemorySegment segment = arena.allocate(segmentSize * ValueLayout.JAVA_CHAR.byteSize());

			// Store the time format into the segment
			getLocaleInfoEx.invoke(MemorySegment.NULL, LOCALE_STIMEFORMAT, segment, segmentSize);
			String timeFormat = segment.getString(0);

			// The time format contains lower case h's when its 12 hour, and upper case ones when its 24 hour
			return timeFormat.contains("h");
		} catch (Throwable t) {
			LOGGER.error("[Skyblocker Windows Compatibility] Failed to check for 12/24 hours clock.", t);
		}

		return false;
	}
}
