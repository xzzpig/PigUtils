package com.github.xzzpig.pigutils.dao.annotation;

import java.lang.annotation.*;

/**
 * 标记此类映射为数据库某张表
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DBTable {
    /**
     * @return 数据库表的名称, 默认(空字符串)为类名
     */
    String name() default "";

    /**
     * 在没有主键时必须设置为false
     *
     * @return 是否缓存对象
     */
    boolean cache() default true;
}
