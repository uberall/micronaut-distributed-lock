package com.uberall

import com.uberall.interceptors.DistributedLockInterceptor
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject

@MicronautTest
@Unroll
class CoreSpec extends Specification {

    @Inject Example example

    @Inject LockServiceImpl lockService

    @Inject DistributedLockInterceptor distributedLockInterceptor

    void 'DistributedLock annotation is working as expected'() {
        when: "we run a locked function 10 times"
        10.times {
            example.foo()
        }

        then: "we only executed it once"
        example.counter == 1

        when: "we clean the repository"
        lockService.clear()

        and: "execute the locked method again"
        example.foo()

        then: "the method was executed"
        example.counter == 2

        when: "we wait for the lock to expire"
        Thread.sleep(5000)

        and: "we call the method again"
        example.foo()

        then: "the method was executed"
        example.counter == 3
    }

    void 'DistributedLock annotation is ignored when disabled'() {
        given: "locking disabled"
        distributedLockInterceptor.enabled = false

        and: "a clear lock database"
        lockService.clear()
        example.counter = 0

        when: "we call foo 10 times"
        10.times {
            example.foo()
        }

        then:
        example.counter == 10

    }

}
