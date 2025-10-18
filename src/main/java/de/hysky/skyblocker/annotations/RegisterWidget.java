package de.hysky.skyblocker.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface RegisterWidget {
	/**
	 * The priority of the widget.
	 * The higher the number, the later the widget will be instantiated.
	 * Use this to ensure that your widget is instantiated after widget method if it depends on it.
	 */
	int priority() default 0;
}
