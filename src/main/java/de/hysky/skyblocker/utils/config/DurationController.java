package de.hysky.skyblocker.utils.config;

import de.hysky.skyblocker.utils.SkyblockTime;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import net.azureaaron.dandelion.api.Option;
import net.azureaaron.dandelion.deps.moulconfig.gui.GuiOptionEditor;
import net.azureaaron.dandelion.deps.moulconfig.processor.ProcessedOption;
import net.azureaaron.dandelion.impl.controllers.IntegerControllerImpl;
import net.azureaaron.dandelion.impl.moulconfig.MoulConfigDefinition;
import net.azureaaron.dandelion.impl.moulconfig.editor.DandelionNumberFieldEditor;
import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationController extends IntegerControllerImpl {
	private static final Pattern secondsPattern = Pattern.compile("(^|\\s)(\\d+)s(\\s|$)");
	private static final Pattern minutesPattern = Pattern.compile("(^|\\s)(\\d+)m(\\s|$)");
	private static final Pattern hoursPattern = Pattern.compile("(^|\\s)(\\d+)h(\\s|$)");

	public DurationController() {
		super(0, Integer.MAX_VALUE, 1, false);
	}

	private static String toString(int duration) {
		return SkyblockTime.formatTime(duration).getString();
	}

	private static int fromString(String value) {
		Matcher hoursMatcher = hoursPattern.matcher(value);
		Matcher minutesMatcher = minutesPattern.matcher(value);
		Matcher secondsMatcher = secondsPattern.matcher(value);

		int result = 0;
		if (hoursMatcher.find()) {
			result += Integer.parseInt(hoursMatcher.group(2)) * 3600;
		}
		if (minutesMatcher.find()) {
			result += Integer.parseInt(minutesMatcher.group(2)) * 60;
		}
		if (secondsMatcher.find()) {
			result += Integer.parseInt(secondsMatcher.group(2));
		}
		return result;
	}

	private static boolean isValid(String s) {
		Matcher hoursMatcher = hoursPattern.matcher(s);
		Matcher minutesMatcher = minutesPattern.matcher(s);
		Matcher secondsMatcher = secondsPattern.matcher(s);

		int hoursCount = 0;
		while (hoursMatcher.find()) hoursCount++;
		int minutesCount = 0;
		while (minutesMatcher.find()) minutesCount++;
		int secondsCount = 0;
		while (secondsMatcher.find()) secondsCount++;

		if (hoursCount == 0 && minutesCount == 0 && secondsCount == 0) return false;
		if (hoursCount > 1 || minutesCount > 1 || secondsCount > 1) return false;
		s = s.replaceAll(hoursPattern.pattern(), "");
		s = s.replaceAll(minutesPattern.pattern(), "");
		s = s.replaceAll(secondsPattern.pattern(), "");
		return s.isBlank();
	}

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public @Nullable GuiOptionEditor controllerMoulConfig(Option<Integer> option, ProcessedOption moulConfigOption, MoulConfigDefinition configDefinition) {
		return new DurationControllerMoul(moulConfigOption, min(), max(), option.binding().defaultValue());
	}

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public ControllerBuilder<Integer> controllerYACL(dev.isxander.yacl3.api.Option<Integer> yaclOption, Class<Integer> type) {
		return () -> new DurationControllerYACL(yaclOption);
	}

	public static class DurationControllerMoul extends DandelionNumberFieldEditor {
		public DurationControllerMoul(ProcessedOption option, float minValue, float maxValue, float defaultValue) {
			super(option, minValue, maxValue, defaultValue);
		}

		@SuppressWarnings("UnstableApiUsage")
		@Override
		protected String toString(Float floatValue) {
			return DurationController.toString(floatValue.intValue());
		}

		@SuppressWarnings("UnstableApiUsage")
		@Override
		protected float parseNumber(String input, float minValue, float maxValue, float defaultValue) {
			return DurationController.fromString(input);
		}
	}

	public record DurationControllerYACL(dev.isxander.yacl3.api.Option<Integer> option) implements IStringController<Integer> {
		@Override
		public String getString() {
			return DurationController.toString(option.pendingValue());
		}

		@Override
		public void setFromString(String value) {
			option.requestSet(DurationController.fromString(value));
		}

		@Override
		public boolean isInputValid(String s) {
			return DurationController.isValid(s);
		}

		@Override
		public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
			return new DurationControllerWidget(this, screen, widgetDimension);
		}
	}
}
