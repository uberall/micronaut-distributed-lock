package com.uberall;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@JdbcRepository
@Repository("lock")
interface DistributedLockRepository extends CrudRepository<DistributedLock, Long> {

    Optional<DistributedLock> findByName(String name);

}
