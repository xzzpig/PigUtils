package com.xzzpig.pigutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标记数组大小<br/>
 * 无实际作用
 */
@Documented
@Retention(RUNTIME)
public @interface ArraySize {
	int[] value();
}