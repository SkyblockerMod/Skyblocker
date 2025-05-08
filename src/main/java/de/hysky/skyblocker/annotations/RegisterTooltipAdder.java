package de.hysky.skyblocker.annotations;

import java.lang.annotation.*;

/**
 * This annotation can either go on a class with a {@code public} parameterless constructor, or a {@code public static finale} field
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface RegisterTooltipAdder {

	/**
	 * Lowest runs first.
	 */
	int priority() default 0;
}
