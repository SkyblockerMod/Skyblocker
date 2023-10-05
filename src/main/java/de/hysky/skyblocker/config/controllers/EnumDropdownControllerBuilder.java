package de.hysky.skyblocker.config.controllers;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;

public interface EnumDropdownControllerBuilder<E extends Enum<E>> extends ControllerBuilder<E> {
    static <E extends Enum<E>> EnumDropdownControllerBuilder<E> create(Option<E> option) {
        return new EnumDropdownControllerBuilderImpl<>(option);
    }
}
