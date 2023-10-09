package de.hysky.skyblocker.config.controllers;

import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownControllerElement;

import java.util.List;

public class EnumDropdownControllerElement<E extends Enum<E>> extends AbstractDropdownControllerElement<E, String> {
    private final EnumDropdownController<E> controller;

    public EnumDropdownControllerElement(EnumDropdownController<E> control, YACLScreen screen, Dimension<Integer> dim) {
        super(control, screen, dim);
        this.controller = control;
    }

    @Override
    public List<String> computeMatchingValues() {
        return controller.getValidEnumConstants(inputField).toList();
    }

    @Override
    public String getString(String object) {
        return object;
    }
}
