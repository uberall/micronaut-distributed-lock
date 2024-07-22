package com.uberall

import com.uberall.interceptors.DistributedLockInterceptor
import io.micronaut.scheduling.TaskScheduler
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.IgnoreRest
import spock.lang.Specification
import spock.lang.Unroll

import jakarta.inject.Inject
import java.time.Duration

@MicronautTest
@Unroll
abstract class ImplementationSpec extends Specification {

    @Inject DistributedLockInterceptor distributedLockInterceptor

    @Inject TaskScheduler taskScheduler

    @Inject Example example

    void setup() {
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
        example.counter.get() == 1

        when: "we clean the repository"
        lockService.clear()

        and: "execute the locked method again"
        example.instantRunningNoCleanup()

        then: "the method was executed"
        example.counter.get() == 2

        when: "we wait for the lock to expire"
        Thread.sleep(5000)

        and: "we call the method again"
        example.instantRunningNoCleanup()

        then: "the method was executed"
        example.counter.get() == 3

        cleanup:
        example.reset()
    }

    void 'DistributedLock annotation is working as expected when cleanup is enabled'() {
        when: "we run a locked function 10 times"
        3.times {
            taskScheduler.schedule(Duration.ofMillis(it * 100), example.&normalRuntimeWithCleanup)
        }

        and: "we wait for them to be at least started"
        Thread.sleep(1000)

        then: "we only executed it once"
        example.counter.get() == 1

        when: "we wait a little longer for the one running invocation to finish"
        Thread.sleep(5000)

        and: "execute the method once on the main thread"
        example.normalRuntimeWithCleanup()

        then: "it was executed again although the lock ttl isn't reached"
        example.counter.get() == 2

        cleanup:
        example.reset()
    }

    void 'instant parallel invocation does not lead to multiple executions'() {
        when: "we execute our test method 100 times in parallel"
        5.times { c ->
            taskScheduler.schedule(Duration.ofMillis(1), { example.noCleanupNoParams(c) })
        }

        and: "we wait a couple seconds"
        sleep(2000)

        then:
        example.counter.get() == 1

        cleanup:
        example.reset()
    }

    void 'instant parallel invocation does lead to multiple executions when parameters are appended'() {
        when: "we execute our test method 5 times in parallel"
        5.times { c ->
            taskScheduler.schedule(Duration.ofMillis(5), { example.cleanupWithParameters(c) })
        }

        and: "we wait a couple seconds"
        sleep(2000)

        then:
        example.counter.get() == 5

        cleanup:
        example.reset()
    }

    void 'DistributedLock annotation is ignored when disabled'() {
        given: "locking disabled"
        distributedLockInterceptor.enabled = false

        and: "a clear lock database"
        lockService.clear()

        when: "we call foo 10 times"
        10.times {
            example.instantRunningNoCleanup()
        }

        then:
        example.counter.get() == 10

        cleanup:
        example.reset()
    }

    abstract LockService getLockService()
}
