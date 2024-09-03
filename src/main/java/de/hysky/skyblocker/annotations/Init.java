package de.hysky.skyblocker.annotations;

import de.hysky.skyblocker.SkyblockerMod;

import java.lang.annotation.*;

/**
 * <p>
 * Marks a method to be called upon mod initialization, performing any initialization logic for the class.
 * <b>In order for a method to be considered an initializer method, it must be public & static while having no arguments and a void return type.</b>
 * </p>
 * Example usage:
 * <pre>
 * {@code
 * @Init
 * public static void init() {
 *     //do stuff
 * }
 * }
 * </pre>
 * <p>
 * A call to the method annotated with this annotation will be added to the {@link SkyblockerMod#init} method at compile-time.
 * </p>
 * <p>
 * If your method depends on another initializer method, you can use the {@link #priority()} field to ensure that it is called after the other method.
 * </p>
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
}
