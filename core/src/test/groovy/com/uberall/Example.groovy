package com.uberall

import com.uberall.annotations.DistributedLock
import groovy.util.logging.Slf4j
import io.micronaut.scheduling.annotation.Scheduled

import javax.inject.Singleton

@Singleton
@Slf4j
class Example {

    static int counter = 0

    @SuppressWarnings('GrMethodMayBeStatic')
    @DistributedLock(name = "test", ttl = "3s", cleanup = false)
    @Scheduled(initialDelay = "3d", fixedDelay = "10h")
    void foo() {
        log.info("foo: start")
        counter++
        log.info("foo: done")
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @DistributedLock(name = "test", ttl = "5m")
    @Scheduled(initialDelay = "3d", fixedDelay = "10h")
    void bar() {
        log.info("bar: start")
        counter++
        sleep(1000)
        log.info("bar: done")
    }
}
