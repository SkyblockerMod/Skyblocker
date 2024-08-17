package de.hysky.skyblocker.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In order for a method to be considered an initializer method it must be public, have no args,
 * and have a return type of {@code void}.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Init {
	/**
	 * The priority of the initializer method.
	 * The higher the number, the later the method will be called.
	 * Use this to ensure that your initializer method is called after another initializer method if it depends on it.
	 */
	int priority() default 0;
	//TODO: Actually implement this
}
