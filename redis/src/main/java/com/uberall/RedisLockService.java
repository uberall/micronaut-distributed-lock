package com.uberall;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uberall.models.Lock;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.sync.RedisServerCommands;
import io.micronaut.context.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Function;

public class RedisLockService implements LockService {

    private static final Logger LOG = LoggerFactory.getLogger(RedisLockService.class);
    private final StatefulRedisConnection<String, String> connection;
    private final ObjectMapper objectMapper;
    @Value("${micronaut.distributed.lock.redis.prefix:lock.}") String prefix;

    @Inject
    public RedisLockService(StatefulRedisConnection<String, String> connection, ObjectMapper objectMapper) {
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Lock> get(String name) {
        Optional<Lock> result = Optional.empty();
        String json = (String) withRedis(redis -> redis.get(getKey(name)));

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
    public void create(Lock lock) {
        try {
            String json = objectMapper.writeValueAsString(lock);
            withRedis(redis -> {
                long until = lock.getUntil().atZone(ZoneId.systemDefault()).toEpochSecond();
                long ttl = until - (System.currentTimeMillis() / 1000);

                redis.setex(getKey(lock.getName()), ttl, json);
                return null;
            });
        } catch (JsonProcessingException e) {
            LOG.error("creating lock json failed", e);
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

    @Override
    public boolean isAutoCleanCapable() {
        return true;
    }

    private String getKey(String name) {
        return prefix + name;
    }

    private Object withRedis(Function<RedisCommands<String, String>, Object> function) {
        RedisCommands<String, String> redis = connection.sync();
        return function.apply(redis);
    }

}
