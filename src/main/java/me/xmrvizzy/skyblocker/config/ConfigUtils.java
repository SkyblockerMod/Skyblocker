package me.xmrvizzy.skyblocker.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;

public class ConfigUtils {
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> EnumControllerBuilder<E> createEnumCyclingListController(Option<E> opt) {
		return EnumControllerBuilder.create(opt).enumClass((Class<E>) opt.binding().defaultValue().getClass());
	}
	
	public static BooleanControllerBuilder createBooleanController(Option<Boolean> opt) {
		return BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true);
	}

	/**
	 * Searches through enum constants in {@code enumClass} for one whose {@link Enum#toString()} result equals {@code expectedValue}
	 * 
	 * @return The enum constant associated with the {@code expectedValue}
	 * @throws IllegalStateException Thrown when a constant couldn't be found
	 * 
	 * @implNote The return value of {@link Enum#toString()} on each enum constant should be unique in order to ensure accuracy
	 */
	public static <E extends Enum<E>> E enumConstantFromToString(Class<E> enumClass, String expectedValue) {
		for (E constant : enumClass.getEnumConstants()) {
			if (constant.toString().equals(expectedValue))
				return constant;
		}

		throw new IllegalStateException("Didn't find an enum constant matching: " + expectedValue);
	}

	public static <E extends Enum<E>> DropdownStringControllerBuilder createDropdownControllerFromEnum(Option<String> opt, Class<E> enumClass) {
		List<String> stringifiedConstants = Arrays.stream(enumClass.getEnumConstants()).map(Enum::toString).collect(Collectors.toList());

		return DropdownStringControllerBuilder.create(opt).allowAnyValue(false).allowEmptyValue(false).values(stringifiedConstants);
	}
}
