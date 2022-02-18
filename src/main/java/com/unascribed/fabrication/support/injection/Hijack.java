package com.unascribed.fabrication.support.injection;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// if targeting void, return bool otherwise Optional<>
@Retention(RUNTIME)
@Target(METHOD)
public @interface Hijack {
	String[] method();
	String[] target();
}
