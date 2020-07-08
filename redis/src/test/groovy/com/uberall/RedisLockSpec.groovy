package com.uberall

import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class RedisLockSpec extends Specification {

    @Inject RedisLockService redisLockService
    @Inject Example example

    void 'Redis locking is working as expected'() {
        when:
        10.times {
            example.foo()
        }

        then:
        example.counter == 1

        and:
        redisLockService.clear()
        example.foo()

        then:
        example.counter == 2

        when:
        Thread.sleep(5000)

        and:
        example.foo()

        then:
        example.counter == 3
    }

}
