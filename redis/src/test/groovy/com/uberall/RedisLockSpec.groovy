package com.uberall

import groovy.util.logging.Slf4j
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.spock.Testcontainers

@MicronautTest
@Slf4j
@Testcontainers
class RedisLockSpec extends ImplementationSpec<RedisLockService> implements TestPropertyProvider {

    public static final String REDIS_IMAGE = "redis:alpine"

    @Lazy
    static GenericContainer redisContainer = {
        GenericContainer redis = new GenericContainer<>(REDIS_IMAGE)
                .withExposedPorts(6379)
                .waitingFor(new HostPortWaitStrategy())
        redis.start()

        return redis
    }()

    void cleanupSpec() {
        redisContainer.stop()
    }

    @Override
    Map<String, String> getProperties() {
        ["redis.uri": "redis://localhost:$redisContainer.firstMappedPort"]
    }
}
