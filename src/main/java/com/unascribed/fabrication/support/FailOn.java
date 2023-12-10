package com.unascribed.fabrication.support;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Fail a Mixin or Feature due to compatability.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface FailOn {
	/**
	 * Only apply this if all of the listed mod IDs are loaded.
	 */
	String[] modLoaded() default {};
	/**
	 * Only apply this if none of the listed mod IDs are loaded.
	 */
	String[] modNotLoaded() default {};
	/**
	 * Only apply this if all of the listed classes are present.
	 */
	String[] classPresent() default {};
	/**
	 * Only apply this if none of the listed classes are present.
	 */
	String[] classNotPresent() default {};
	/**
	 * Only apply this if none of the given special eligibility conditions are met.
	 */
	SpecialEligibility[] invertedSpecialConditions() default {};
}
