package com.uberall;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uberall.exceptions.DistributedLockCreationException;
import com.uberall.models.Lock;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.sync.RedisServerCommands;
import io.micronaut.context.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class RedisLockService implements LockService {

    private static final Logger LOG = LoggerFactory.getLogger(RedisLockService.class);
    private final StatefulRedisConnection<String, String> connection;
    private final ObjectMapper objectMapper;

    @Value("${micronaut.distributed.lock.redis.prefix:lock.}") private String prefix;

    @Inject
    public RedisLockService(StatefulRedisConnection<String, String> connection, ObjectMapper objectMapper) {
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Lock> get(String name) {
        Optional<Lock> result = Optional.empty();
        String json = fromRedis(redis -> redis.get(getKey(name)));

        if (json == null) {
            return Optional.empty();
        }

        try {
            result = Optional.ofNullable(objectMapper.readValue(json, Lock.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void save(Lock lock) {
        synchronized (this) {
            try {
                final String json = objectMapper.writeValueAsString(lock);

                final String key = getKey(lock.getName());
                withRedis(redis -> {
                    String result = redis.getset(key, json);

                    if (result != null) {
                        redis.set(key, result);
                        throw new DistributedLockCreationException(lock.getName(), null);
                    }
                });
            } catch (JsonProcessingException e) {
                LOG.error("creating lock json failed", e);
            }
        }
    }

    @Override
    public void delete(Lock lock) {
        withRedis(redis -> redis.del(getKey(lock.getName())));
    }

    @Override
    public void clear() {
        withRedis(RedisServerCommands::flushall);
    }

    private String getKey(String name) {
        return prefix + name;
    }

    private String fromRedis(Function<RedisCommands<String, String>, String> function) {
        RedisCommands<String, String> redis = connection.sync();
        return function.apply(redis);
    }

    private void withRedis(Consumer<RedisCommands<String, String>> consumer) {
        consumer.accept(connection.sync());
    }

}
