package com.unascribed.fabrication.support;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Prevents a Mixin or Feature from being applied under certain conditions.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface EligibleIf {
	/**
	 * Only apply this if the given config key is true.
	 */
	String configEnabled() default "";
	/**
	 * Only apply this if any of the given config keys are true.
	 */
	String[] anyConfigEnabled() default "";
	/**
	 * Only apply this if the given config key is false.
	 */
	String configDisabled() default "";
	/**
	 * Only apply this in the given environment.
	 */
	Env envMatches() default Env.ANY;
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
	 * Only apply this if all of the given special eligibility conditions are met.
	 */
	SpecialEligibility[] specialConditions() default {};
}
