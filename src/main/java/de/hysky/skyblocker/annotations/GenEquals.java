package de.hysky.skyblocker.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotating a method with this value will result in the method being replaced with a default implementation
 * for the {@link Object#equals(Object)} method. The equals check includes all non-transient instance fields of a class and may
 * optionally include the return value of the superclass' {@code equals} method. Methods with this annotation must take in a single
 * {@link Object} parameter and return a boolean value but do not need to necessarily override the default {@code equals} method; furthermore
 * implementations should either create a stub body that always throws or mark the method as {@code native}.
 *
 * @implNote The exact behaviour for the generated {@code equals} method follows that of {@link Record#equals(Object)}.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface GenEquals {
	/**
	 * Whether the {@code equals} result of the superclass should be considered as part of the generated equals method.
	 * Performs a Logical AND on the result of the generated equals method and the equals method of the superclass.
	 */
	boolean includeSuper() default false;
}
