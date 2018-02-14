package com.xzzpig.pigutils.dao.annotation;

import java.lang.annotation.*;
import java.sql.JDBCType;

/**
 * 标记此类成员映射为数据库某张表的某字段
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DBField {
    /**
     * @return 字段名称, 默认(空字符串)为Field名称
     */
    String name() default "";

    /**
     * @return 字段类型
     */
    JDBCType type() default JDBCType.VARCHAR;

    /**
     * @return 字段默认值, 默认(空字符串)为不设置
     */
    String defaultValue() default "";

    String check() default "";

    boolean autoIncrement() default false;

    boolean primaryKey() default false;

    boolean unique() default false;

    boolean notNull() default false;

    /**
     * @return 字段长度，默认(-1)为不限制
     */
    int size() default -1;
}
