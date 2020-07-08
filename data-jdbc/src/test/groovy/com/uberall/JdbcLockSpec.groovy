package com.uberall

import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class JdbcLockSpec extends Specification {

    @Inject MicronautDataJdbcLockService lockService
    @Inject Example example

    void 'JDBC locking is working as expected'() {
        when:
        10.times {
            example.foo()
        }

        then:
        example.counter == 1

        and:
        lockService.clear()
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
