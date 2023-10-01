package me.xmrvizzy.skyblocker.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;

public class ConfigUtils {
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> EnumControllerBuilder<E> createEnumCyclingListController(Option<E> opt) {
        return EnumControllerBuilder.create(opt).enumClass((Class<E>) opt.binding().defaultValue().getClass());
    }
}
