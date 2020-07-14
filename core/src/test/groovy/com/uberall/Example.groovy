package com.uberall

import com.uberall.annotations.DistributedLock
import io.micronaut.scheduling.annotation.Scheduled

import javax.inject.Singleton

@Singleton
class Example {

    static int counter = 0

    @SuppressWarnings('GrMethodMayBeStatic')
    @DistributedLock(name = "test", ttl = "3s")
    @Scheduled(initialDelay = "3d", fixedDelay = "10h")
    void foo() {
        counter++
    }
}
