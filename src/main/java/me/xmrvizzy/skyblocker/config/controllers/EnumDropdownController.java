package me.xmrvizzy.skyblocker.config.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownController;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

public class EnumDropdownController<E extends Enum<E>> extends AbstractDropdownController<E> {
    /**
     * The function used to convert enum constants to strings used for display, suggestion, and validation. Defaults to {@link Enum#toString}.
     */
    protected final Function<E, String> toString;

    protected EnumDropdownController(Option<E> option, Function<E, String> toString) {
        super(option);
        this.toString = toString;
    }

    @Override
    public String getString() {
        return toString.apply(option().pendingValue());
    }

    @Override
    public void setFromString(String value) {
        option().requestSet(getEnumFromString(value));
    }

    /**
     * Searches through enum constants for one whose {@link #toString} result equals {@code value}
     *
     * @return The enum constant associated with the {@code value} or the pending value if none are found
     * @implNote The return value of {@link #toString} on each enum constant should be unique in order to ensure accuracy
     */
    private E getEnumFromString(String value) {
        value = value.toLowerCase();
        for (E constant : option().pendingValue().getDeclaringClass().getEnumConstants()) {
            if (toString.apply(constant).toLowerCase().equals(value)) return constant;
        }

        return option().pendingValue();
    }

    @Override
    public boolean isValueValid(String value) {
        value = value.toLowerCase();
        for (E constant : option().pendingValue().getDeclaringClass().getEnumConstants()) {
            if (toString.apply(constant).equals(value)) return true;
        }

        return false;
    }

    @Override
    protected String getValidValue(String value, int offset) {
        return getValidEnumConstants(value)
                .skip(offset)
                .findFirst()
                .orElseGet(this::getString);
    }

    /**
     * Filters and sorts through enum constants for those whose {@link #toString} result equals {@code value}
     *
     * @return a sorted stream containing enum constants associated with the {@code value}
     * @implNote The return value of {@link #toString} on each enum constant should be unique in order to ensure accuracy
     */
    @NotNull
    protected Stream<String> getValidEnumConstants(String value) {
        String valueLowerCase = value.toLowerCase();
        return Arrays.stream(option().pendingValue().getDeclaringClass().getEnumConstants())
                .map(this.toString)
                .filter(constant -> constant.toLowerCase().contains(valueLowerCase))
                .sorted((s1, s2) -> {
                    String s1LowerCase = s1.toLowerCase();
                    String s2LowerCase = s2.toLowerCase();
                    if (s1LowerCase.startsWith(valueLowerCase) && !s2LowerCase.startsWith(valueLowerCase)) return -1;
                    if (!s1LowerCase.startsWith(valueLowerCase) && s2LowerCase.startsWith(valueLowerCase)) return 1;
                    return s1.compareTo(s2);
                });
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new EnumDropdownControllerElement<>(this, screen, widgetDimension);
    }
}
