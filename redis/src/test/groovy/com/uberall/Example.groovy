package com.uberall

import com.uberall.annotation.DistributedLock

import javax.inject.Singleton

@Singleton
class Example {

    static int counter = 0;

    @DistributedLock(ttl = 3)
    void foo() {
        counter++
    }
}
