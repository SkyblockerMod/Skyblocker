package me.xmrvizzy.skyblocker.config.controllers;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl;

public class EnumDropdownControllerBuilderImpl<E extends Enum<E>> extends AbstractControllerBuilderImpl<E> implements EnumDropdownControllerBuilder<E> {
    public EnumDropdownControllerBuilderImpl(Option<E> option) {
        super(option);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Controller<E> build() {
        return new EnumDropdownController<>(option);
    }
}
