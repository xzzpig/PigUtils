package com.xzzpig.pigutils.duck;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import com.xzzpig.pigutils.reflect.MethodUtils;

/**
 * 标识DuckObject的封装Object需要有指定名称的Field<br/>
 * 可用 {@link MethodUtils#checkArgs(Object...)} 检验
 */
@Retention(RUNTIME)
public @interface HasField {
	String[] value();
}
