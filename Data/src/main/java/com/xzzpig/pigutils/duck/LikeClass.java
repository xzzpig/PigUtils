package com.xzzpig.pigutils.duck;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标识DuckObject的封装Object需要像某Class<br/>
 * {@link DuckObject#isLike(Class, boolean, boolean)}检验
 */
@Retention(RUNTIME)
public @interface LikeClass {
    boolean checkField() default true;

    boolean checkMethod() default true;

    Class<?>[] value();
}
