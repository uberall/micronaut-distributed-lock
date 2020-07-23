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
    @DistributedLock(ttl = "3s", cleanup = false)
    @Scheduled(initialDelay = "3d", fixedDelay = "10h")
    void instantRunningNoCleanup() {
        log.info("instantRunningNoCleanup: start")
        counter++
        log.info("instantRunningNoCleanup: done")
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @DistributedLock(ttl = "5m")
    @Scheduled(initialDelay = "3d", fixedDelay = "10h")
    void normalRuntimeWithCleanup() {
        log.info("normalRuntimeWithCleanup: start")
        counter++
        sleep(1000)
        log.info("normalRuntimeWithCleanup: done")
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @DistributedLock(ttl = "5m", appendParams = true, cleanup = false)
    @Scheduled(initialDelay = "3d", fixedDelay = "10h")
    void longRunningWithCleanup(def i) {
        log.info("longRunningWithCleanup $i: start")
        counter++
        sleep(5000)
        log.info("longRunningWithCleanup $i: done")
    }
}
