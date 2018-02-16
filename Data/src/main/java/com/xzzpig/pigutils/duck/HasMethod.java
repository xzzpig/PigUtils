package com.xzzpig.pigutils.duck;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标识DuckObject的封装Object需要有指定名称的Method<br/>
 */
@Retention(RUNTIME)
public @interface HasMethod {
    String[] value();
}
