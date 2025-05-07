package de.hysky.skyblocker.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface RegisterSlotTextAdder {

	int priority() default 0;
}
