package de.hysky.skyblocker.utils.config;

import de.hysky.skyblocker.utils.SkyblockTime;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.string.IStringController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record DurationController(Option<Integer> option) implements IStringController<Integer> {

    private static final Pattern secondsPattern = Pattern.compile("(^|\\s)(\\d+)s(\\s|$)");
    private static final Pattern minutesPattern = Pattern.compile("(^|\\s)(\\d+)m(\\s|$)");
    private static final Pattern hoursPattern = Pattern.compile("(^|\\s)(\\d+)h(\\s|$)");

    @Override
    public String getString() {
		return SkyblockTime.formatTime(option.pendingValue()).getString();
    }


    @Override
    public void setFromString(String value) {
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
        option.requestSet(result);
    }


    @Override
    public boolean isInputValid(String s) {
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

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new DurationControllerWidget(this, screen, widgetDimension);
    }
}
