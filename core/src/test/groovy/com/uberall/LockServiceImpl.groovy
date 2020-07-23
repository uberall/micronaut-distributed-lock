package com.uberall

import com.uberall.exceptions.DistributedLockConfigurationException
import com.uberall.models.Lock
import groovy.transform.Synchronized
import io.micronaut.context.annotation.Secondary

import javax.inject.Singleton

@Singleton
@Secondary
class LockServiceImpl implements LockService {

    static final synchronized Map<String, Lock> LOCK_REPOSITORY = [:]

    @Override
    void save(Lock lock) {
        synchronized (LOCK_REPOSITORY) {
            if (get(lock.name)) {
                throw new DistributedLockConfigurationException()
            }
            LOCK_REPOSITORY[lock.name] = lock
        }
    }

    @Override
    void delete(Lock lock) {
        LOCK_REPOSITORY.remove(lock.name)
    }

    @Override
    Optional<Lock> get(String name) {
        Optional.ofNullable(LOCK_REPOSITORY[name])
    }

    @Override
    void clear() {
        LOCK_REPOSITORY.clear()
    }

}
