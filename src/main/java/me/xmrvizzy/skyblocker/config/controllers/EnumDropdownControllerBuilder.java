package me.xmrvizzy.skyblocker.config.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;

import java.util.function.Function;

public interface EnumDropdownControllerBuilder<E extends Enum<E>> extends ControllerBuilder<E> {
    EnumDropdownControllerBuilder<E> toString(Function<E, String> toString);

    static <E extends Enum<E>> EnumDropdownControllerBuilder<E> create(Option<E> option) {
        return new EnumDropdownControllerBuilderImpl<>(option);
    }

    /**
     * Creates a factory for {@link EnumDropdownControllerBuilder}s with the given function for converting enum constants to strings.
     * Use this if a custom toString function for an enum is needed.
     * Use it like this:
     * <pre>{@code Option.<MyEnum>createBuilder().controller(createEnumDropdownControllerBuilder.getFactory(MY_CUSTOM_ENUM_TO_STRING_FUNCTION))}</pre>
     * @param toString The function used to convert enum constants to strings used for display, suggestion, and validation
     * @return a factory for {@link EnumDropdownControllerBuilder}s
     * @param <E> the enum type
     */
    static <E extends Enum<E>> Function<Option<E>, ControllerBuilder<E>> getFactory(Function<E, String> toString) {
        return opt -> EnumDropdownControllerBuilder.create(opt).toString(toString);
    }
}
