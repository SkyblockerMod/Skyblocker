package me.xmrvizzy.skyblocker.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.CyclingListControllerBuilder;
import net.minecraft.text.Text;

public class ConfigUtils {

	@SuppressWarnings("unchecked")
	public static <E extends Enum<?>> CyclingListControllerBuilder<E> createCyclingListController4Enum(Option<E> opt) {
		E[] constants = (E[]) opt.binding().defaultValue().getClass().getEnumConstants();
		
		return CyclingListControllerBuilder.create(opt).values(constants).formatValue(formatter -> Text.of(formatter.toString()));
	}
}
