package com.uberall

import com.uberall.models.Lock

import javax.inject.Singleton

@Singleton
class LockServiceImpl implements LockService {

    static final synchronized Map<String, Lock> LOCK_REPOSITORY = [:]

    @Override
    void create(Lock lock) {
        LOCK_REPOSITORY[lock.name] = lock
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
