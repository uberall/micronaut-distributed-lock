package com.uberall;

import com.uberall.model.Lock;
import io.micronaut.core.annotation.Internal;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public interface LockService {

    Optional<Lock> get(String name);

    void create(Lock lock);

    void delete(Lock lock);

    default boolean isAutoCleanCapable() {
        return false;
    }

    /**
     * DO NOT USE THIS METHOD IN PRODUCTION!
     * This is for Tests only!
     */
    default void clear() {
        //NOOP
    }

}
