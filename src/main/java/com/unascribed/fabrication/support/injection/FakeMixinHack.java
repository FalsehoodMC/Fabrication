package com.unascribed.fabrication.support.injection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface FakeMixinHack {
	//all classes specified must exclusively contain public static methods
	Class<?>[] value();
}
