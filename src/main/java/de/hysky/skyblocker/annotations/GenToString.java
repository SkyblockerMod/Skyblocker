package de.hysky.skyblocker.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotating a method with this value will result in the method being replaced with a default implementation
 * for the {@link Object#toString()} method. The {@code toString} value includes all non-transient instance fields of a class.
 * Methods with this annotation must take in a no parameters and return a {@link String} value but do not need to
 * necessarily override the default {@code toString} method; furthermore implementations should either create a
 * stub body that always throws or mark the method as {@code native}.
 *
 * @implNote The exact behaviour for the generated {@code toString} method follows that of {@link Record#toString()}.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface GenToString {
}
