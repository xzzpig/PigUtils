package com.xzzpig.pigutils.annotation;

/**
 * 表示此类/方法已经过测试
 * 是否通过由value决定
 */
public @interface TestPass {
    /**
     * @return 此类/方法是否通过测试
     */
    boolean value() default true;
}
