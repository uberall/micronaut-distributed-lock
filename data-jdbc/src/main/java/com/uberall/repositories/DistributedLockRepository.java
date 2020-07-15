package com.uberall.repositories;

import com.uberall.DistributedLock;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

public interface DistributedLockRepository extends CrudRepository<DistributedLock, Long> {

    Optional<DistributedLock> findByName(String name);

}
