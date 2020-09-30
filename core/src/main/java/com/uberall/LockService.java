package com.uberall;

import com.uberall.models.Lock;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public interface LockService {

    /**
     * Get a possible Lock for the specified lock name.
     * Optional can be empty if there is no lock for this name
     * @param name of the lock
     * @return maybe a Lock
     */
    Optional<Lock> get(String name);

    /**
     * Saves the given Lock object in the underlying database.
     * @param lock to be saved
     */
    void save(Lock lock);

    /**
     * Deletes the given Lock from the underlying database.
     * Deleting an non existing Lock should be avoided
     * @param lock to be deleted
     */
    void delete(Lock lock);

    default boolean isAutoCleanCapable() {
        return false;
    }

    /**
     * DO NOT USE THIS METHOD IN PRODUCTION.
     * This is for Tests only!
     */
    default void clear() {
        //NOOP
    }

}
