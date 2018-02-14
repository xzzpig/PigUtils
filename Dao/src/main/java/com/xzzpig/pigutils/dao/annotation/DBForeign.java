package com.xzzpig.pigutils.dao.annotation;

import java.lang.annotation.*;

/**
 * 标记此类成员映射为数据库某张表的外键(需配合DBField使用)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DBForeign {
    /**
     * @return 外键表名称, 默认(Object.class)表示此类成员类型
     */
    Class<?> table() default Object.class;

    /**
     * @return 外键字段名称, 默认("")表示外键表主键名称
     */
    String field() default "";

    /**
     * @return 是否自动填充其他字段值
     */
    boolean autoFill() default false;

    /**
     * @return 是否自动插入或更新
     */
    boolean autoIU() default true;
}
