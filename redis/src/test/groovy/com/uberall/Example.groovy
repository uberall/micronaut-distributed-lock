package com.uberall

import com.uberall.annotations.DistributedLock

import javax.inject.Singleton

@Singleton
class Example {

    static int counter = 0

    @SuppressWarnings('GrMethodMayBeStatic')
    @DistributedLock(ttl = "3s")
    void foo() {
        counter++
    }
}
