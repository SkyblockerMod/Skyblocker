package de.hysky.skyblocker.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotating a method with this value will result in the method being replaced with a default implementation
 * for the {@link Object#hashCode()} method. The hash code calculation includes all non-transient instance fields of a class and may
 * optionally include the hash code of the superclass. Methods with this annotation must take in no
 * parameters and return an integer value but do not need to necessarily override the default {@code hashCode} method; furthermore
 * implementations should either create a stub body that always throws or mark the method as {@code native}.
 *
 * @implNote The exact behaviour for the generated {@code hashCode} method follows that of {@link Record#hashCode()}.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface GenHashCode {
	/**
	 * Whether the hash code of the superclass should be included as part of the generated {@code hashCode} method.
	 * Combines the two hash codes using the standard Java hash code calculation formula.
	 */
	boolean includeSuper() default false;
}
