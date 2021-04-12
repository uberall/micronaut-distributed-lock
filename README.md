# micronaut-distributed-lock
A Micronaut library to lock method executions in a distributed environment

## Usage

### Prerequisites

Add our Github Packages repository to your pom.xml or build.gradle

build.gradle
```
repositories {
    maven {
        url  "https://maven.pkg.github.com/uberall/micronaut-distributed-lock" 
    }
}
```

### Installation

Add a dependency to micronaut-distributed-lock-core

```
implementation "com.uberall:micronaut-distributed-lock-core:1.2.1"
```

and one runtime implementation to your pom.xml/build.gradle

```
runtime "com.uberall:micronaut-distributed-lock-data-jdbc:1.2.1"
```

now you can annotate e.g. your @Scheduled methods with @com.uberall.annotations.DistributedLock

```groovy
import com.uberall.annotations.DistributedLock
import io.micronaut.scheduling.annotation.Scheduled

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FooJob {

    @Inject
    FooService fooService

    @DistributedLock(ttl = "1m")
    @Scheduled(fixedRate = '5m')
    void perform() {
        // some long running asynchornous task
    }
}
```

The result is that no matter how often you call this method, **as long as the first invocation is still running** no further invocations will be executed,
no matter who is trying to execute. 

### Annotation Parameters

| name | default | description |
| --- |  --- | ---  |
| ttl  | "1m" | The lock duration. **If cleanup is set to false this prohibits any further execution for the given duration.** Otherwise this is a mere "hint" when to allow execution again because e.g. the first execution was interrupted and the lock has never been cleaned up |  
| name  | {methodName} |  The name of the lock, this is used to identify the lock in the repository and should be unique across you codebase. If not set the method name is used | 
| appendParameters | false  |  Whether or not to append parameters to the lock name. If set to true a key/value map of the parameters will be appeneded to the lock name. This is helpful in environments where you only want to prohibit e.g. processing of the same database entitiy on 2 servers |   
| cleanup | true | Whether or not to cleanup the lock after method execution |

### TTL and Cleanup
There can be some confusion with TTL and Cleanup so here is use cases of when to set cleanup and what to set ttl to in other cases.

#### Cleanup = true (default)
The Interceptor will always clean up the lock, no matter whether the method execution was successful or not, after method execution.
You could see the TTL as a "hint" on execution time. If the lock is not cleaned up (e.g. server is shutdown during method execution) the next round is still running.

The typical use case is a distributed application where you want a scheduled job to only run once every X minutes even if you are running your micronaut application on 20 servers.
TTL should be set to not block the next scheduled execution. E.g.:

```groovy
import com.uberall.annotations.DistributedLock
import io.micronaut.scheduling.annotation.Scheduled

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FooJob {

    @Inject
    FooService fooService

    @DistributedLock(ttl = "4m50s")
    @Scheduled(fixedRate = '5m')
    void perform() {
        fooService.runTheThingThatIsSupposedToHappenEveryFiveMinutes()
    }
}
```

### Cleanup = false
In this scenario the lock will not be released after method execution and no more invocations of the method are actually executed for the duration of the lock lifetime.

A typical example is a "rate limited" method e.g. You are doing some heavy analytics every 10 minutes and if the analytics haven't resulted in something unusual, you only want the result to be sent out once a day.

```groovy
import com.uberall.annotations.DistributedLock
import io.micronaut.scheduling.annotation.Scheduled

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FooJob {

    @Inject FooService fooService

    @Scheduled(fixedRate = '10m')
    void perform() {
        def result = fooService.getResult()
        if (result.bad) {
            fooService.sendResult(result) // will always happen
        } else {
            sendResult(result) // is only executed once a day
        }
    }
    
    @DistributedLock(ttl = "1d", cleanup = false)
    void sendResult(def result) {
        fooService.sendResult(result)
    }

}
```

### Configuration 

To disable the library completely (recommended in tests) you can set `micronaut.distributed-lock-enabled` to `false` in application.groovy

````yaml
micronaut:
    distributed-lock:
      enabled: false
````

## Implementations

### Redis
A simple implementation using [micronaut-redis](https://micronaut-projects.github.io/micronaut-redis/latest/guide/)

#### Usage

```
runtime "com.uberall:micronaut-distributed-lock-data-redis:1.2.1"
```

#### Configuration
Check the [documentation](https://micronaut-projects.github.io/micronaut-redis/latest/guide/) how to make sure lettuce has a redis server connection.

### Micronaut Data JDBC
An implementation using [micronaut-data-jdbc](https://micronaut-projects.github.io/micronaut-data/latest/guide/#jdbcQuickStart).

#### Usage

```
runtime "com.uberall:micronaut-distributed-lock-data-jdbc:1.2.1"
```

After adding the runtime dependency into build.gradle you'll have to tell the library which sql dialect to use

```yaml
micronaut:
  distributed-lock:
    dialect: mysql # OR postgres
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
If you want to use a non-default datasource you can set the lock database name by setting `micronaut.distributed.lock.datasource-name` and defining a database with that name
E.g.:

```yaml
micronaut:
  distributed-lock:
    datasource-name: lock
    dialect: mysql
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

## Development
No special setup is needed to start development. Just hack ahead and create a MR.

## Publishing

You'll need to be in the uberall organisation in Github to be able to publish the latest version.
also `GITHUB_USER` and `GITHUB_TOKEN` environment variables need to be set.

Simply run `./gradlew :publish --no-daemon` to upload all artifacts to Github.
