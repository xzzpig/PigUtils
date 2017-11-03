package com.github.xzzpig.pigutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标记基于某package<br />
 * 无实际作用
 */
@Documented
@Retention(RUNTIME)
public @interface BaseOnPackage {
	String[] value();
}
