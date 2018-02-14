package com.xzzpig.pigutils.dao.annotation;

import java.lang.annotation.*;

/**
 * 标记此类成员映射为数据库数组(无需配合DBField使用)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DBArray {
    /**
     * @return 中间表的名称, 默认("")两表名_连接
     */
    String arrayTable() default "";

    /**
     * @return 是否自动填充其他字段值
     */
    boolean autoFill() default false;

    /**
     * @return 是否自动插入或更新
     */
    boolean autoIU() default true;
}
