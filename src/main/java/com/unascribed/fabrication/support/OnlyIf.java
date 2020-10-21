package com.unascribed.fabrication.support;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Prevents a Mixin from being applied under certain conditions.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface OnlyIf {
	/**
	 * Only apply this if the given config key is true.
	 */
	String config() default "";
	/**
	 * Only apply this in the given environment.
	 */
	Env env() default Env.ANY;
	/**
	 * Only apply this if all of the listed mod IDs are present.
	 */
	String[] dependencies() default {};
}
