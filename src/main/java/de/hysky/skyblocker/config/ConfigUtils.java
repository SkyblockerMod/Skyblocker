package de.hysky.skyblocker.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import de.hysky.skyblocker.config.controllers.EnumDropdownControllerBuilder;

public class ConfigUtils {
	public static BooleanControllerBuilder createBooleanController(Option<Boolean> opt) {
		return BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> EnumControllerBuilder<E> createEnumCyclingListController(Option<E> opt) {
		return EnumControllerBuilder.create(opt).enumClass((Class<E>) opt.binding().defaultValue().getClass());
	}

	public static <E extends Enum<E>> EnumDropdownControllerBuilder<E> createEnumDropdownController(Option<E> opt) {
		return EnumDropdownControllerBuilder.create(opt);
	}
}
