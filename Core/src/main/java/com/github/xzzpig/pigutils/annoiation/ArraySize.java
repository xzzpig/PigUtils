package com.github.xzzpig.pigutils.annoiation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

/**
 * 标记数组大小<br/>
 * 无实际作用
 */
@Documented
@Retention(RUNTIME)
public @interface ArraySize {
	int[] value();
}
