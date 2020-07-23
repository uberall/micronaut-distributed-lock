package com.uberall.repositories;

import io.micronaut.context.annotation.Requires;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;

@JdbcRepository(dialect = Dialect.MYSQL)
@Repository("${micronaut.distributed-lock.datasource-name:default}")
@Requires(property = "micronaut.distributed-lock.dialect", value = "mysql")
public abstract  class MySQLDistributedLockRepository implements DistributedLockRepository {
}
