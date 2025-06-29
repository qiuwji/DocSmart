package com.qiu.backend.common.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 锁的key，可以使用SpEL表达式，如"#user.id"
     */
    String key();

    /**
     * 等待锁的时间
     */
    long waitTime() default 5;

    /**
     * 锁持有的时间
     */
    long leaseTime() default 10;
}
