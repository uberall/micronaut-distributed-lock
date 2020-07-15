package com.uberall.repositories;

import io.micronaut.context.annotation.Requires;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;

@JdbcRepository(dialect = Dialect.POSTGRES)
@Repository("${micronaut.distributed-lock.datasource-name:default}")
@Requires(property = "micronaut.distributed-lock.dialect", value = "postgres")
public interface PostgreSQLDistributedLockRepository extends DistributedLockRepository {
}
