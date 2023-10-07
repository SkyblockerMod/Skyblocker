package me.xmrvizzy.skyblocker.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.ValueFormatter;
import me.xmrvizzy.skyblocker.config.controllers.EnumDropdownControllerBuilder;
import net.minecraft.text.Text;

public class ConfigUtils {
    public static final ValueFormatter<Float> FLOAT_TWO_FORMATTER = value -> Text.literal(String.format("%,.2f", value).replaceAll("[\u00a0\u202F]", " "));

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
