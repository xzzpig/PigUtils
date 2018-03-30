package com.xzzpig.pigutils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 此方法是否会关闭资源
 */
@Target(ElementType.METHOD)
public @interface ResourceClose {
    boolean value() default true;

    String[] resourceName() default {"ALL"};
}
