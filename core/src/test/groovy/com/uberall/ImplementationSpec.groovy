package com.uberall

import com.uberall.interceptors.DistributedLockInterceptor
import io.micronaut.scheduling.TaskScheduler
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject
import java.time.Duration

@MicronautTest
@Unroll
abstract class ImplementationSpec<T extends LockService> extends Specification {

    @Inject Example example

    @Inject T lockService

    @Inject DistributedLockInterceptor distributedLockInterceptor

    @Inject TaskScheduler taskScheduler

    void setup() {
        example.counter = 0
        lockService.clear()
    }

    void 'DistributedLock annotation is working as expected when cleanup is disabled'() {
        when: "we run a locked function 10 times"
        10.times {
            taskScheduler.schedule(Duration.ofMillis(it * 100), example.&instantRunningNoCleanup)
        }

        and: "we wait for them to be started"
        sleep(1500)

        then: "we only executed it once"
        example.counter == 1

        when: "we clean the repository"
        lockService.clear()

        and: "execute the locked method again"
        example.instantRunningNoCleanup()

        then: "the method was executed"
        example.counter == 2

        when: "we wait for the lock to expire"
        Thread.sleep(5000)

        and: "we call the method again"
        example.instantRunningNoCleanup()

        then: "the method was executed"
        example.counter == 3

    }

    void 'DistributedLock annotation is working as expected when cleanup is enabled'() {
        when: "we run a locked function 10 times"
        3.times {
            taskScheduler.schedule(Duration.ofMillis(it * 100), example.&normalRuntimeWithCleanup)
        }

        and: "we wait for them to be at least started"
        Thread.sleep(1000)

        then: "we only executed it once"
        example.counter == 1

        when: "we wait a little longer for the one running invocation to finish"
        Thread.sleep(5000)

        and: "execute the method once on the main thread"
        example.normalRuntimeWithCleanup()

        then: "it was executed again although the lock ttl isn't reached"
        example.counter == 2
    }

    void 'instant parallel invocation does not lead to multiple executions'() {
        when: "we execute our test method 100 times in parallel"
        10.times { c ->
            taskScheduler.schedule(Duration.ofMillis(1), { example.noCleanupNoParams(c) })
        }

        and: "we wait a couple seconds"
        sleep(2000)

        then:
        example.counter == 1
    }

    void 'instant parallel invocation does lead to multiple executions when parameters are appended'() {
        when: "we execute our test method 10 times in parallel"
        10.times { c ->
            taskScheduler.schedule(Duration.ofMillis(1), { example.cleanupWithParameters(c) })
        }

        and: "we wait a couple seconds"
        sleep(2000)

        then:
        example.counter == 10
    }

    void 'DistributedLock annotation is ignored when disabled'() {
        given: "locking disabled"
        distributedLockInterceptor.enabled = false

        and: "a clear lock database"
        lockService.clear()
        example.counter = 0

        when: "we call foo 10 times"
        10.times {
            example.instantRunningNoCleanup()
        }

        then:
        example.counter == 10
    }
}
