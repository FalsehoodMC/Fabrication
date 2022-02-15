package com.unascribed.fabrication.support.injection;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//TODO figure out how to write to mixin refmap
@Retention(RUNTIME)
@Target(METHOD)
public @interface ModifyReturn {
	String[] method();
	String[] target();
}
