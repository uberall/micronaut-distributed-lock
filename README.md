# micronaut-distributed-lock
A Micronaut library to lock method executions in a distributed environment

## Usage

### Prerequisites

Add our bintray repository to your pom.xml or build.gradle

build.gradle
```
repositories {
    maven {
        url  "https://dl.bintray.com/uberall/micronaut" 
    }
}
```

### Installation

Add a dependency to micronaut-distributed-lock-core

```
implementation "com.uberall:micronaut-distributed-lock-core:1.0.1"
```

and one runtime implementation to your pom.xml/build.gradle

```
runtime "com.uberall:micronaut-distributed-lock-data-jdbc:1.0.1"
```

now you can annotate e.g. your @Scheduled methods with @com.uberall.annotation.DistrubtedLock

```groovy
package com.uberall

import com.uberall.annotation.DistributedLock
import io.micronaut.scheduling.annotation.Scheduled

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FooJob {

    @Inject
    FooService fooService

    @DistributedLock(ttl = 60)
    @Scheduled(fixedRate = '5m')
    void perform() {

    }
}
```

## Implementations

### Redis
A simple implementation using [micronaut-redis](https://micronaut-projects.github.io/micronaut-redis/latest/guide/)

#### Usage

```
runtime "com.uberall:micronaut-distributed-lock-data-redis:1.0.1"
```

#### Configuration
Check the [documentation](https://micronaut-projects.github.io/micronaut-redis/latest/guide/) how to make sure lettuce has a redis server connection.

### Micronaut Data JDBC
An implementation using [micronaut-data-jdbc](https://micronaut-projects.github.io/micronaut-data/latest/guide/#jdbcQuickStart).

#### Usage

```
runtime "com.uberall:micronaut-distributed-lock-data-jdbc:1.0.1"
```

Additionally, You will need to add a table to your datasource that has 3 columns: 

| column |  type | description |
|--------|--------|--------|
|  id |  BIGINT |  The unique id for each lock |
| name | VARCHAR | The name of the lock, length depends on your usage, 255 should be safe |
|  until |  DATETIME |  The datetime until this lock is valid |

For MySQL or MariaDB a compatible create statement would be:

```mysql
CREATE TABLE IF NOT EXISTS `distributed_lock`
(
    `id`    bigint(20)          NOT NULL AUTO_INCREMENT,
    `name`  varchar(255) UNIQUE NOT NULL,
    `until` datetime            NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
```

#### Configuration
If you want to use a non-default datasource just configure one named `lock`.
E.g.:

```yaml
datasources:
  default:
    url: jdbc:mysql://production-server/all-the-important-data
    driverClassName: com.mysql.cj.jdbc.Driver
    username: user
    password: pass
    schema-generate: none
    dialect: MYSQL
    pooled: true
  lock:
    url: jdbc:mysql://lock-server/the-lock-schema
    driverClassName: com.mysql.cj.jdbc.Driver
    username: resu
    password: ssap
    dialect: MYSQL
    pooled: true
```

This implementation is fully compatible and tested with Postgres and should simply work with any other halfway ANSI compatible SQL Database. 
