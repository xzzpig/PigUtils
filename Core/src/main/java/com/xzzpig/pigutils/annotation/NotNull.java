package com.xzzpig.pigutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 标记非空<br />
 * 对于方法参数可用 {@link com.xzzpig.pigutils.reflect.MethodUtils#checkThisArgs(Object...)}检查
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface NotNull {

}
