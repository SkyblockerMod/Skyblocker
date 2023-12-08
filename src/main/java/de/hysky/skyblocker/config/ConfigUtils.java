package de.hysky.skyblocker.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

public class ConfigUtils {
	public static final ValueFormatter<Formatting> FORMATTING_FORMATTER = formatting -> Text.literal(StringUtils.capitalize(formatting.getName().replaceAll("_", " ")));
	public static final ValueFormatter<Float> FLOAT_TWO_FORMATTER = value -> Text.literal(String.format("%,.2f", value).replaceAll("[\u00a0\u202F]", " "));

	public static BooleanControllerBuilder createBooleanController(Option<Boolean> opt) {
		return BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> EnumControllerBuilder<E> createEnumCyclingListController(Option<E> opt) {
		return EnumControllerBuilder.create(opt).enumClass((Class<E>) opt.binding().defaultValue().getClass());
	}

	/**
	 * Creates a factory for {@link EnumDropdownControllerBuilder}s with the given function for converting enum constants to texts.
	 * Use this if a custom formatter function for an enum is needed.
	 * Use it like this:
	 * <pre>{@code Option.<MyEnum>createBuilder().controller(ConfigUtils.getEnumDropdownControllerFactory(MY_CUSTOM_ENUM_TO_TEXT_FUNCTION))}</pre>
	 *
	 * @param formatter The function used to convert enum constants to texts used for display, suggestion, and validation
	 * @param <E>       the enum type
	 * @return a factory for {@link EnumDropdownControllerBuilder}s
	 */
	public static <E extends Enum<E>> Function<Option<E>, ControllerBuilder<E>> getEnumDropdownControllerFactory(ValueFormatter<E> formatter) {
		return opt -> EnumDropdownControllerBuilder.create(opt).formatValue(formatter);
	}
}
