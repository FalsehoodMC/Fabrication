package com.unascribed.fabrication.support.injection;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Target(TYPE)
public @interface FakeMixin {
	Class<?>[] value() default { };
	String[] targets() default { };
}
