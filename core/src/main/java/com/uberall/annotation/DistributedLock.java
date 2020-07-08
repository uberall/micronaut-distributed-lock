package com.uberall.annotation;

import com.uberall.interceptor.DistributedLockInterceptor;
import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Executable
@Around
@Type(DistributedLockInterceptor.class)
public @interface DistributedLock {

    /**
     * A Closure that might be used to get the name, defaults to the classname
     * If defined, the closure is called with the methods parameters in a Map
     */
    String name() default "";

    /**
     * The time to life for the lock in seconds. defaults to 60
     */
    int ttl() default 60;


    /**
     * Whether or not method parameters should be appended to the name.
     * If set to true a hash of parameters and their values is appended to the name
     * Should only be set to true if parameter values are implementing a proper toString method
     */
    boolean appendParams() default false;
}
