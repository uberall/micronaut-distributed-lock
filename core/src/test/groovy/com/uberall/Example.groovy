package com.uberall

import com.uberall.annotations.DistributedLock
import groovy.util.logging.Slf4j

import javax.inject.Singleton

@Singleton
@Slf4j
@SuppressWarnings('GrMethodMayBeStatic')
class Example {

    static int counter = 0

    @DistributedLock(ttl = "3s", cleanup = false)
    void instantRunningNoCleanup() {
        log.info("instantRunningNoCleanup: start")
        counter++
        log.info("instantRunningNoCleanup: done")
    }

    @DistributedLock(ttl = "5m")
    void normalRuntimeWithCleanup() {
        log.info("normalRuntimeWithCleanup: start")
        counter++
        expensiveBusinessLogic(1000)
        log.info("normalRuntimeWithCleanup: done")
    }

    @DistributedLock(ttl = "5m", appendParameters = false, cleanup = false)
    void noCleanupNoParams(def i) {
        log.info("longRunningNoCleanupNoParams $i: start")
        counter++
        expensiveBusinessLogic(5000)
        log.info("longRunningNoCleanupNoParams $i: done")
    }

    @DistributedLock(ttl = "5s", appendParameters = true)
    void cleanupWithParameters(def i) {
        log.info("cleanupWithParameters $i: start")
        counter++
        expensiveBusinessLogic(1000)
        log.info("cleanupWithParameters $i: done")
    }

    private void expensiveBusinessLogic(int magicBusinessValue) {
        sleep(magicBusinessValue)
    }
}
